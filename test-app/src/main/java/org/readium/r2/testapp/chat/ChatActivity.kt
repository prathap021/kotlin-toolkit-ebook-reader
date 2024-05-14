package org.readium.r2.testapp.chat

import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import org.readium.r2.testapp.chat.ui.theme.ReadiumTheme
import android.app.Activity
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch
import org.readium.r2.testapp.chat.ui.theme.ReadiumTheme

data class ChatItem(
    var message: String,
    var isQuestion: Boolean,
)


class ChatActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ReadiumTheme {
                ChatScreen()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen() {

    val context = LocalContext.current
    val activity = context as Activity

    /*var listOfChatItems = mutableListOf<ChatItem>(
        ChatItem(message = "Question", isQuestion = true),
        ChatItem(message = "Answer", isQuestion = false),
        ChatItem(message = "Question", isQuestion = true),
        ChatItem(message = "Answer", isQuestion = false),
        ChatItem(message = "Question", isQuestion = true),
        ChatItem(message = "Answer", isQuestion = false),
    )*/
    val viewModel: ChatViewModel = viewModel()

    val listState= rememberLazyListState()
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


                    if(typedMessage.isNotEmpty()) {
                        viewModel.sendMessage(typedMessage, onDone = {
                            scope.launch {
                                listState.scrollToItem(messages.size)
                            }
                        })
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
        contentAlignment = if (chatItem.isQuestion) Alignment.CenterEnd else Alignment.CenterStart
    ) {
        Box(
            modifier = modifier
                .fillMaxSize(0.7f)
                .border(
                    border = BorderStroke(color = Color.Gray, width = 1.dp),
                    shape = RoundedCornerShape(18.dp)
                )
                .background(Color.Transparent)
                .padding(vertical = 8.dp, horizontal = 12.dp),
            contentAlignment = Alignment.CenterStart,

            ) {
            Text(text = chatItem.message, lineHeight = 16.sp)
        }
    }
}


@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    ReadiumTheme {
        ChatScreen()
    }
}