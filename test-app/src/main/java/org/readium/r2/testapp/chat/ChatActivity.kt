package org.readium.r2.testapp.chat

import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import org.readium.r2.testapp.chat.ui.theme.ReadiumTheme
import android.app.Activity
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
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
import kotlinx.coroutines.launch

data class ChatItem(
    var message: String,
    var isQuestion: Boolean,
)


class ChatActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ReadiumTheme {
                val bookId: String? = intent.getStringExtra("bookId")

                ChatScreen(bookId = bookId)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(bookId: String?) {

    val context = LocalContext.current
    val activity = context as Activity

    val viewModel: ChatViewModel = viewModel()

    val listState = rememberLazyListState()
    val messages by viewModel.messages.collectAsState()
    val scope = rememberCoroutineScope()


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


                    if (typedMessage.isNotEmpty()) {
                        viewModel.sendMessage(context = context, typedMessage.trim(), scrollToEnd = {
                            scope.launch {
                                listState.scrollToItem(messages.size)
                            }
                        }, bookId = bookId)
                        typedMessage = ""
//                        scope.launch {
//                            listState.scrollToItem(messages.size)
//                        }

                    }
                }, modifier = Modifier.weight(0.1f)) {
                    Icon(
                        imageVector = Icons.Default.Send,
                        contentDescription = "Send",
                    )
                }
            }
        }
    ) { innerPadding ->

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
                Text(text = chatItem.message, lineHeight = 18.sp, color = Color.DarkGray, fontFamily = FontFamily.SansSerif)
            }
        }
    }
}


@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    ReadiumTheme {
        ChatScreen(bookId = "")
    }
}