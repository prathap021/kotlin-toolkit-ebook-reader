package org.readium.r2.testapp.chat

import android.annotation.SuppressLint
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import org.readium.r2.testapp.chat.ui.theme.ChaiReaderTheme
import android.app.Activity
import android.os.Build.VERSION.SDK_INT
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.ImageLoader
import coil.compose.rememberAsyncImagePainter
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder
import coil.request.ImageRequest
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import kotlinx.coroutines.launch
import org.readium.r2.testapp.R

data class ChatItem(
    var message: String,
    var isQuestion: Boolean,
)


class ChatActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ChaiReaderTheme {
                val bookId: String? = intent.getStringExtra("bookId")
                val viewModel: ChatViewModel = viewModel()
                ChatScreen(bookId = bookId, viewModel = viewModel)
            }
        }
    }
}

@SuppressLint("StateFlowValueCalledInComposition")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(bookId: String?, viewModel: ChatViewModel) {

    val context = LocalContext.current
    LaunchedEffect(key1 = Unit) {
        viewModel.getBookDetails(context, bookId)
    }

    val chatAvailableState by viewModel.chatAvailableState.collectAsState()


    val activity = context as Activity


    val listState = rememberLazyListState()
    val messages by viewModel.messages.collectAsState()
    val scope = rememberCoroutineScope()
    val isLoading by viewModel.isLoading.collectAsState()

    val imageLoader = ImageLoader.Builder(context)
        .components {
            if (SDK_INT >= 28) {
                add(ImageDecoderDecoder.Factory())
            } else {
                add(GifDecoder.Factory())
            }
        }
        .build()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(text = "Chatbooks")
                },
                navigationIcon = {
                    IconButton(onClick = {

                        activity.finish()
                    }) {

                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Go Back")
                    }
                },
            )
        },
        bottomBar = {
            var typedMessage by remember {
                mutableStateOf("")
            }
            if(chatAvailableState is ChatAvailableState.Success)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .padding(horizontal = 24.dp, vertical = 12.dp)
                /*.navigationBarsPadding()*/
            ) {
                OutlinedTextField(
                    modifier = Modifier
                        .heightIn(max = 112.dp)
                        .weight(0.9f),
                    shape = RoundedCornerShape(12.dp),
                    placeholder = {
                        Text("Ask here")
                    },
                    value = typedMessage, onValueChange = {
                        typedMessage = it
                    })
                Spacer(modifier = Modifier.size(8.dp))
                IconButton(onClick = {

                    if (isLoading == false) {
                        if (typedMessage.isNotEmpty()) {
                            viewModel.sendMessage(
                                context = context,
                                typedMessage.trim(),
                                scrollToEnd = {
                                    scope.launch {
                                        listState.scrollToItem(messages.size)
                                    }
                                },
                                bookId = bookId
                            )
                            typedMessage = ""
//                        scope.launch {
//                            listState.scrollToItem(messages.size)
//                        }

                        }
                    }

                }, modifier = Modifier.weight(0.1f)) {
                    Icon(
                        imageVector = if (isLoading) Icons.Default.Stop else Icons.Default.Send,
                        contentDescription = "Send",
                    )
                }
            }
        }
    ) { innerPadding ->


        when (chatAvailableState) {
            is ChatAvailableState.Success -> {
                LazyColumn(
                    modifier = Modifier
                        .padding(innerPadding)
                        .padding(horizontal = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    state = listState
                ) {
                    items(messages) {
                        ChatBubble(chatItem = it, modifier = Modifier)
                    }
                    if (isLoading) {
                        item {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 24.dp, vertical = 6.dp),
                                horizontalArrangement = Arrangement.Start
                            ) {
                                // Typing GIF
                                AnimatedPreloader(modifier = Modifier.size(50.dp))
                            }
                        }
                    }
                }
            }

            is ChatAvailableState.Failed -> {
                Box(
                    modifier = Modifier
                        .padding(innerPadding).padding(24.dp)
                        .fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {

                    Text((chatAvailableState as ChatAvailableState.Failed).message)
                }
            }

            is ChatAvailableState.Exception -> {
                Box(
                    modifier = Modifier
                        .padding(innerPadding).padding(24.dp)
                        .fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text((chatAvailableState as ChatAvailableState.Exception).message)
                }

            }

            is ChatAvailableState.Loading -> {
                Box(
                    modifier = Modifier
                        .padding(innerPadding).padding(24.dp)
                        .fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

        }


    }
}


@Composable
fun ChatBubble(chatItem: ChatItem, modifier: Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.CenterStart
        //if (chatItem.isQuestion) Alignment.CenterEnd else Alignment.CenterStart
    ) {
        Row {


            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        color = if (chatItem.isQuestion) Color(
                            0xFFeaedef
                        ) else Color(0xFFEBF6FF), shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {

                Icon(
                    imageVector = if (chatItem.isQuestion) Icons.Default.Person else Icons.Default.Book,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = Color.DarkGray
                )
            }
            Spacer(modifier = Modifier.size(8.dp))

            Box(
                modifier = modifier
//                    .border(
//                        border = BorderStroke(color = Color.Gray, width = 1.dp),
//                        shape = RoundedCornerShape(18.dp)
//                    )
                    .background(
                        if (chatItem.isQuestion) Color(
                            0xFFeaedef
                        ) else Color(0xFFEBF6FF),
                        shape = RoundedCornerShape(18.dp)
                    )
                    .padding(vertical = 8.dp, horizontal = 12.dp),
                contentAlignment = Alignment.CenterStart,

                ) {
                Text(
                    text = chatItem.message,
                    lineHeight = 18.sp,
                    color = Color.DarkGray,
                    fontFamily = FontFamily.SansSerif
                )
            }
        }
    }
}


@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    ChaiReaderTheme {
        ChatScreen(viewModel = viewModel(), bookId = "")
    }
}

@Composable
fun AnimatedPreloader(modifier: Modifier = Modifier) {
    val preloaderLottieComposition by rememberLottieComposition(
        LottieCompositionSpec.RawRes(
            R.raw.loading
        )
    )

    val preloaderProgress by animateLottieCompositionAsState(
        preloaderLottieComposition,
        iterations = LottieConstants.IterateForever,
        isPlaying = true
    )


    LottieAnimation(
        composition = preloaderLottieComposition,
        progress = preloaderProgress,
        modifier = modifier
    )
}