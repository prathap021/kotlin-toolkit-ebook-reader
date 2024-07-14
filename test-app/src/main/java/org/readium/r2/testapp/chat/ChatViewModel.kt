package org.readium.r2.testapp.chat

import android.content.Context
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
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
import retrofit2.http.Path
import retrofit2.http.Query


interface BookApiService {
    @GET("/ai_chat/chat-with-pdf")
    suspend fun getAnswer(
        @Query("file_id") bookId: Long?, @Query("chat_text") message: String
    ): Response<Answer>

    @GET("ai_chat/book-primary/{id}/")
    suspend fun getBookDetails(
        @Path("id") bookId: Long?
    ): Response<BookDetail>

}

object RetrofitInstance {
    private val retrofit by lazy {
        val logging = HttpLoggingInterceptor()
        logging.setLevel(HttpLoggingInterceptor.Level.BASIC)


        val httpClient =
            OkHttpClient.Builder().addInterceptor(logging).addInterceptor(Interceptor { chain ->
                val original = chain.request()
                val requestBuilder = original.newBuilder().header(
                    "Authorization", "Api-Key FulsIWQv.ZWLubipTTfIgzYTyURo314FHE23Moe0Z"
                )  // Replace "YOUR_API_KEY" with your actual API key
                    .method(original.method, original.body)
                val request = requestBuilder.build()
                chain.proceed(request)
            }).build()


        Retrofit.Builder().baseUrl("https://api.booktalkstaging.ailaysa.com").client(httpClient)
            .addConverterFactory(GsonConverterFactory.create()).build()

    }

    val api: BookApiService by lazy {
        retrofit.create(BookApiService::class.java)
    }
}

@Serializable
data class Answer(
    val id: Long? = null, val question: String? = null,

    @SerialName("created_at") val createdAt: String? = null,

    val answer: String? = null
)

@Serializable
data class BookDetail(
    val id: Long? = null,
    val uid: String? = null,
    val user: Long? = null,

    @SerialName("book_title")
    val bookTitle: String? = null,

    @SerialName("book_subtitle")
    val bookSubtitle: String? = null,

    val publisher: String? = null,

    @SerialName("publication_date")
    val publicationDate: String? = null,

    val language: Long? = null,
    val author: String? = null,

    @SerialName("isbn_13")
    val isbn13: String? = null,

    @SerialName("isbn_10")
    val isbn10: String? = null,

    val subject: String? = null,

    @SerialName("source_file")
    val sourceFile: String? = null,

    @SerialName("source_file_name")
    val sourceFileName: String? = null,

    @SerialName("cover_image")
    val coverImage: String? = null,

    @SerialName("cover_thumbnail")
    val coverThumbnail: String? = null,

    @SerialName("is_show")
    val isShow: Boolean? = null,

    @SerialName("about_book")
    val aboutBook: String? = null,

    @SerialName("about_author")
    val aboutAuthor: String? = null
)

sealed interface ChatAvailableState {
    object Loading : ChatAvailableState
    data class Success(val message: String = "") : ChatAvailableState
    data class Failed(val message: String = "") : ChatAvailableState
    data class Exception(val message: String = "") : ChatAvailableState
}


class ChatViewModel : ViewModel() {

    private var _messages = MutableStateFlow<List<ChatItem>>(emptyList())
    val messages = _messages.asStateFlow()

    private var _isLoading = MutableStateFlow<Boolean>(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private var _chatAvailableLoading = MutableStateFlow<Boolean>(false)
    val chatAvailableLoading: StateFlow<Boolean> = _chatAvailableLoading.asStateFlow()

    private var _chatAvailableState: MutableStateFlow<ChatAvailableState> =
        MutableStateFlow(ChatAvailableState.Loading)
    val chatAvailableState: StateFlow<ChatAvailableState> = _chatAvailableState


    fun sendMessage(
        context: Context, userMessage: String, scrollToEnd: () -> Unit, bookId: String?
    ) {
        val bookRepository: BookRepository
        val database = AppDatabase.getDatabase(context)
        bookRepository = BookRepository(database.booksDao())
        viewModelScope.launch {

            val book: Book? = bookId?.toLong()?.let { bookRepository.get(it) }

            try {
                val originalBookId: Long? = book?.identifier?.toLong()
                /*
                 when (book?.title) {
                     "Indus Valley Civilization – A Land of the ancient Dravidians" -> {
                         59
                     }

                     "WHITE NIGHTS" -> {
                         60
                     }

                     else -> {
                         null
                     }

                 }*/

                if (NetworkUtils.isNetworkAvailable(context)) {

                    _isLoading.value = true

                    _messages.value =
                        _messages.value + ChatItem(message = userMessage, isQuestion = true)
                    scrollToEnd.invoke()

                    val response = RetrofitInstance.api.getAnswer(originalBookId, userMessage)

                    if (response.isSuccessful) {
                        response.body()?.let {
                            _messages.value += ChatItem(
                                message = it.answer ?: "", isQuestion = false
                            )
                        }
                        scrollToEnd.invoke()
                        _isLoading.value = false
                    }

                } else {
                    Toast.makeText(context, "Check your internet connection", Toast.LENGTH_SHORT)
                        .show()
                    _isLoading.value = false

                }


            } catch (e: Exception) {
                Toast.makeText(context, e.localizedMessage, Toast.LENGTH_SHORT).show()
                _isLoading.value = false

            }
        }

    }


    fun getBookDetails(
        context: Context, bookId: String?
    ) {
        val bookRepository: BookRepository
        val database = AppDatabase.getDatabase(context)
        bookRepository = BookRepository(database.booksDao())
        viewModelScope.launch {

            val book: Book? = bookId?.toLong()?.let { bookRepository.get(it) }
            try {
                val originalBookId: Long? = book?.identifier?.toLong()


                if (NetworkUtils.isNetworkAvailable(context)) {

                    _chatAvailableLoading.value = true


                    val response = RetrofitInstance.api.getBookDetails(originalBookId)

                    if (response.isSuccessful) {
                        _chatAvailableState.value =
                            ChatAvailableState.Success(message = "Chat Available for this book!")

                        _chatAvailableLoading.value = false
                    } else {
                        _chatAvailableState.value =
                            ChatAvailableState.Failed(message = "Chat Not Available for this book!")

                    }

                } else {
                    _chatAvailableState.value =
                        ChatAvailableState.Failed(message = "Check your internet connection")

                    Toast.makeText(context, "Check your internet connection", Toast.LENGTH_SHORT)
                        .show()
                    _chatAvailableLoading.value = false

                }


            } catch (e: Exception) {
                _chatAvailableState.value =
                    ChatAvailableState.Exception(message = "Chat Not Available for this book!")

//                Toast.makeText(context, e.localizedMessage, Toast.LENGTH_SHORT).show()
                _chatAvailableLoading.value = false

            }
        }
    }


}


