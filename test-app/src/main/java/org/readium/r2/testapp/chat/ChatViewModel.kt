package org.readium.r2.testapp.chat

import android.content.Context
import android.widget.Toast
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.readium.r2.testapp.chat.util.NetworkUtils
import org.readium.r2.testapp.data.BookRepository
import org.readium.r2.testapp.data.db.AppDatabase
import org.readium.r2.testapp.data.model.Book
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query


interface JokeApiService {
    @GET("/ai_chat/chat-with-pdf")
    suspend fun getRandomJoke(
        @Query("file_id") bookId: Long?,
        @Query("chat_text") message: String
    ): Response<Answer>
}


object RetrofitInstance {
    private val retrofit by lazy {
        val logging = HttpLoggingInterceptor()
        logging.setLevel(HttpLoggingInterceptor.Level.BASIC)


        val httpClient = OkHttpClient.Builder()
            .addInterceptor(logging)
            .addInterceptor(Interceptor { chain ->
                val original = chain.request()
                val requestBuilder = original.newBuilder()
                    .header(
                        "Authorization",
                        "Api-Key FulsIWQv.ZWLubipTTfIgzYTyURo314FHE23Moe0Z"
                    )  // Replace "YOUR_API_KEY" with your actual API key
                    .method(original.method, original.body)
                val request = requestBuilder.build()
                chain.proceed(request)
            })
            .build()


        Retrofit.Builder()
            .baseUrl("https://api.booktalkstaging.ailaysa.com")
            .client(httpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

    }

    val api: JokeApiService by lazy {
        retrofit.create(JokeApiService::class.java)
    }
}


@Serializable
data class Answer(
    val id: Long? = null,
    val question: String? = null,

    @SerialName("created_at")
    val createdAt: String? = null,

    val answer: String? = null
)

class ChatViewModel : ViewModel() {

    private val _messages = MutableStateFlow<List<ChatItem>>(emptyList())
    val messages = _messages.asStateFlow()


    fun sendMessage(context: Context, userMessage: String, scrollToEnd: () -> Unit, bookId: String?) {


        val bookRepository : BookRepository

        val database = AppDatabase.getDatabase(context)

        bookRepository = BookRepository(database.booksDao())



        viewModelScope.launch {

            val book: Book? = bookId?.toLong()?.let { bookRepository.get(it) }

            val originalBookId:Long? = when(book?.title){
                "Indus Valley Civilization – A Land of the ancient Dravidians"-> {
                    59
                }
                "WHITE NIGHTS" -> {
                    60
                }

                else -> {null}
            }


            try {

                if (NetworkUtils.isNetworkAvailable(context)) {
                    _messages.value = _messages.value + ChatItem(message = userMessage, isQuestion = true)
                    scrollToEnd.invoke()

                    val response = RetrofitInstance.api.getRandomJoke(originalBookId, userMessage)

                    if (response.isSuccessful) {
                        response.body()?.let {
                            _messages.value += ChatItem(message = it.answer ?: "", isQuestion = false)
                        }
                        scrollToEnd.invoke()
                    }

                }else{
                    Toast.makeText(context,"Check your internet connection", Toast.LENGTH_SHORT).show()

                }


            } catch (e: Exception) {
                Toast.makeText(context, e.localizedMessage, Toast.LENGTH_SHORT).show()
            }
        }

    }

}