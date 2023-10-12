/*
 * Copyright 2021 Readium Foundation. All rights reserved.
 * Use of this source code is governed by the BSD-style license
 * available in the top-level LICENSE file of the project.
 */

package org.readium.r2.shared.util.http

import java.io.InputStream
import java.nio.charset.Charset
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import org.readium.r2.shared.ExperimentalReadiumApi
import org.readium.r2.shared.util.Try
import org.readium.r2.shared.util.flatMap
import org.readium.r2.shared.util.mediatype.MediaType
import org.readium.r2.shared.util.tryRecover

/**
 * An HTTP client performs HTTP requests.
 *
 * You may provide a custom implementation, or use the [DefaultHttpClient] one which relies on
 * native APIs.
 */
public interface HttpClient {

    /**
     * Streams the resource from the given [request].
     */
    public suspend fun stream(request: HttpRequest): HttpTry<HttpStreamResponse>

    // Declare a companion object to allow reading apps to extend it. For example, by adding a
    // HttpClient.get(Context) constructor.
    public companion object
}

/**
 * Represents a successful HTTP response received from a server.
 *
 * @param request Request associated with the response.
 * @param url Final URL of the response.
 * @param statusCode Response status code.
 * @param headers HTTP response headers, indexed by their name.
 * @param mediaType Media type sniffed from the `Content-Type` header and response body. Falls back
 *        on `application/octet-stream`.
 */
public data class HttpResponse(
    val request: HttpRequest,
    val url: String,
    val statusCode: Int,
    val headers: Map<String, List<String>>,
    val mediaType: MediaType
) {

    private val httpHeaders = HttpHeaders(headers)

    /**
     * Finds the first value of the first header matching the given name.
     * In keeping with the HTTP RFC, HTTP header field names are case-insensitive.
     */
    @Deprecated("Use the header method instead.", level = DeprecationLevel.ERROR)
    @Suppress("Unused_parameter")
    public fun valueForHeader(name: String): String? {
        throw NotImplementedError()
    }

    /**
     * Finds all the values of the first header matching the given name.
     * In keeping with the HTTP RFC, HTTP header field names are case-insensitive.
     */
    @Deprecated("Use the headers method instead.", level = DeprecationLevel.ERROR)
    @Suppress("Unused_parameter")
    public fun valuesForHeader(name: String): List<String> {
        throw NotImplementedError()
    }

    /**
     * Finds the last header matching the given name.
     * In keeping with the HTTP RFC, HTTP header field names are case-insensitive.
     * The returned string can contain a single value or a comma-separated list of values if
     * the field supports it.
     */
    public fun header(name: String): String? = httpHeaders[name]

    /**
     * Finds all the headers matching the given name.
     * In keeping with the HTTP RFC, HTTP header field names are case-insensitive.
     * Each item of the returned list can contain a single value or a comma-separated list of
     * values if the field supports it.
     */
    public fun headers(name: String): List<String> = httpHeaders.getAll(name)

    /**
     * Indicates whether this server supports byte range requests.
     */
    val acceptsByteRanges: Boolean get() = httpHeaders.acceptsByteRanges

    /**
     * The expected content length for this response, when known.
     *
     * Warning: For byte range requests, this will be the length of the chunk, not the full
     * resource.
     */
    val contentLength: Long? get() = httpHeaders.contentLength
}

/**
 * HTTP response with streamable content.
 *
 * You MUST close the [body] to terminate the HTTP connection when you're done.
 */
public class HttpStreamResponse(
    public val response: HttpResponse,
    public val body: InputStream
)

/**
 * Fetches the resource from the given [request].
 */
public suspend fun HttpClient.fetch(request: HttpRequest): HttpTry<HttpFetchResponse> =
    stream(request)
        .flatMap { response ->
            try {
                val body = withContext(Dispatchers.IO) {
                    response.body.use { it.readBytes() }
                }
                Try.success(HttpFetchResponse(response.response, body))
            } catch (e: Exception) {
                Try.failure(HttpException.wrap(e))
            }
        }

/**
 * Fetches the resource from the given [request] before decoding it with the provided [decoder].
 *
 * If the decoder fails, a MalformedResponse error is returned.
 */
public suspend fun <T> HttpClient.fetchWithDecoder(
    request: HttpRequest,
    decoder: (HttpFetchResponse) -> T
): HttpTry<T> =
    fetch(request)
        .flatMap {
            try {
                Try.success(decoder(it))
            } catch (e: Exception) {
                Try.failure(HttpException(kind = HttpException.Kind.MalformedResponse, cause = e))
            }
        }

/**
 * Fetches the resource from the given [request] as a [String].
 */
public suspend fun HttpClient.fetchString(request: HttpRequest, charset: Charset = Charsets.UTF_8): HttpTry<String> =
    fetchWithDecoder(request) { response ->
        String(response.body, charset)
    }

/**
 * Fetches the resource from the given [request] as a [JSONObject].
 */
public suspend fun HttpClient.fetchJSONObject(request: HttpRequest): HttpTry<JSONObject> =
    fetchWithDecoder(request) { response ->
        JSONObject(String(response.body))
    }

/**
 * HTTP response with the whole [body] as a [ByteArray].
 */
public class HttpFetchResponse(
    public val response: HttpResponse,
    public val body: ByteArray
)

/**
 * Performs a HEAD request to retrieve only the response headers.
 *
 * This helpers falls back on a GET request with 0-length byte range if the server doesn't support
 * HEAD requests.
 */
@ExperimentalReadiumApi
public suspend fun HttpClient.head(request: HttpRequest): HttpTry<HttpResponse> {
    suspend fun HttpRequest.response(): HttpTry<HttpResponse> =
        stream(this)
            .map { response ->
                response.body.close()
                response.response
            }

    return request
        .copy { method = HttpRequest.Method.HEAD }
        .response()
        .tryRecover { exception ->
            if (exception.kind != HttpException.Kind.MethodNotAllowed) {
                return@tryRecover Try.failure(exception)
            }

            request
                .copy {
                    method = HttpRequest.Method.GET
                    setRange(0L..0L)
                }
                .response()
        }
}
