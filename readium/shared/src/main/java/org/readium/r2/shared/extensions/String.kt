/*
 * Module: r2-shared-kotlin
 * Developers: Mickaël Menu
 *
 * Copyright (c) 2020. Readium Foundation. All rights reserved.
 * Use of this source code is governed by a BSD-style license which is detailed in the
 * LICENSE file present in the project repository where this source code is maintained.
 */

package org.readium.r2.shared.extensions

import android.net.Uri
import java.net.URL
import java.security.MessageDigest
import java.util.Date
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.toInstant
import org.json.JSONException
import org.json.JSONObject
import org.readium.r2.shared.InternalReadiumApi

@InternalReadiumApi
public fun String.iso8601ToDate(): Date? {
    val instant = tryOrNull { Instant.parse(this) }
        ?: tryOrNull { LocalDateTime.parse(this).toInstant(TimeZone.UTC) }
        ?: tryOrNull { LocalDate.parse(this).atStartOfDayIn(TimeZone.UTC) }
        ?: return null

    return Date(instant.toEpochMilliseconds())
}

/**
 * If this string starts with the given [prefix], returns this string.
 * Otherwise, returns a copy of this string after adding the [prefix].
 */
@InternalReadiumApi
public fun String.addPrefix(prefix: CharSequence): String {
    if (startsWith(prefix)) {
        return this
    }
    return prefix.toString() + this
}

/**
 * If this string ends with the given [suffix], returns this string.
 * Otherwise, returns a copy of this string after adding the [suffix].
 */
@InternalReadiumApi
public fun String.addSuffix(suffix: CharSequence): String {
    if (endsWith(suffix)) {
        return this
    }
    return this + suffix
}

internal enum class HashAlgorithm(val key: String) {
    MD5("MD5"),
    SHA256("SHA-256")
}

internal fun String.hash(algorithm: HashAlgorithm): String =
    MessageDigest
        .getInstance(algorithm.key)
        .digest(this.toByteArray())
        .fold("") { str, it -> str + "%02x".format(it) }

internal fun String.toUrlOrNull(context: URL? = null) =
    try {
        URL(context, this)
    } catch (e: Exception) {
        null
    }

internal fun String.toJsonOrNull(): JSONObject? =
    try {
        JSONObject(this)
    } catch (e: JSONException) {
        null
    }

/**
 * Percent-encodes an URL path section.
 *
 * Equivalent to Swift's `string.addingPercentEncoding(withAllowedCharacters: .urlPathAllowed)`
 */
internal fun String.percentEncodedPath(): String =
    Uri.encode(this, "$&+,/:=@")

/**
 * Percent-encodes an URL query key or value.
 *
 * Equivalent to Swift's `string.addingPercentEncoding(withAllowedCharacters: .urlQueryAllowed)`
 */
internal fun String.percentEncodedQuery(): String =
    Uri.encode(this, "$+,/?:=@")

/**
 * Returns whether the String receiver contains only printable ASCII characters.
 */
internal fun String.isPrintableAscii(): Boolean =
    all { it.code in 0x20..0x7F }
