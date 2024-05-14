package org.readium.r2.testapp.chat

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.JsonArray
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

import retrofit2.Response
import retrofit2.http.GET

interface JokeApiService {
    @GET("/v1/jokes?limit=1")
    suspend fun getRandomJoke(): Response<List<Joke>>
}


object RetrofitInstance {
    private val retrofit by lazy {


        val httpClient = OkHttpClient.Builder()
            .addInterceptor(Interceptor { chain ->
                val original = chain.request()
                val requestBuilder = original.newBuilder()
                    .header("X-Api-Key", "RwfeBxYxwHXF1/2iiTWEuQ==IedUaxk5HniQ3B09")  // Replace "YOUR_API_KEY" with your actual API key
                    .method(original.method, original.body)
                val request = requestBuilder.build()
                chain.proceed(request)
            })
            .build()


        Retrofit.Builder()
            .baseUrl("https://api.api-ninjas.com")
            .client(httpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

    }

    val api: JokeApiService by lazy {
        retrofit.create(JokeApiService::class.java)
    }
}


@Serializable
data class Joke(
    val joke: String
)


class ChatViewModel : ViewModel() {

    private val _messages = MutableStateFlow<List<ChatItem>>(emptyList())
    val messages = _messages.asStateFlow()

    fun sendMessage(userMessage: String, onDone: ()->Unit) {
        _messages.value = _messages.value + ChatItem(message = userMessage, isQuestion = true)

        viewModelScope.launch {
            val response = RetrofitInstance.api.getRandomJoke()
//            Log.d("response", response.body().let { it?.get(0)?.joke ?: "" })

            if (response.isSuccessful) {
                response.body()?.let {
                    _messages.value += ChatItem(message = it.get(0).joke, isQuestion = false)
                }
                onDone.invoke()
            }
        }
    }

}