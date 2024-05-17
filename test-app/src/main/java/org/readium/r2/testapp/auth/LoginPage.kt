package org.readium.r2.testapp.auth
import android.content.Intent
import androidx.compose.foundation.layout.*

import androidx.compose.material.*
import androidx.compose.runtime.*

import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

import androidx.compose.foundation.background



import androidx.compose.foundation.shape.RoundedCornerShape

import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation

import androidx.compose.ui.unit.sp
import androidx.activity.ComponentActivity
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.ui.platform.LocalContext
import org.readium.r2.testapp.MainActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import org.readium.r2.testapp.utils.SpHelper


class LoginActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
//            val isFishTasty: false /*MutableState<Boolean> =  IsUserLoginOrNot()*/
//            if(isFishTasty){
//                val intent = Intent(this, MainActivity::class.java)
//                this.startActivity(intent)
//                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
//            }else{
//
//            }
            MaterialTheme {
                LoginScreen ()

//                Scaffold(
//                    topBar = { },
//                    content = { it
//                        Column(
//                            modifier = Modifier
//                                .fillMaxSize()
//                                .padding(16.dp),
//                            horizontalAlignment = Alignment.CenterHorizontally
//
//
//                        ){
//
//
//                        }
//                    })
            }


        }


    }


}



@Composable
fun MainScreen() {
    val context = LocalContext.current
    Button(
        onClick = {
            val intent = Intent(context, MainActivity::class.java)
            context.startActivity(intent)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK

    }) {
        Text("Login", color = Color.White)
    }
}


@Composable
fun LoginScreen(/*onLoginClicked: @Composable (String, String) -> Unit*/) {
//
//
    var email by remember { mutableStateOf("hello@reallygreatesite.com") }
    var password by remember { mutableStateOf("**********") }
    //    val gradientColors = listOf(
//        Color(0xFFff4135), // Start color
//        Color(0xFFff7a3f),
//        Color(0xFFff7a3f),
//        Color(0xFFff7a3f)
//        // End color
//    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFF7043)), // Background color
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(40.dp))
            Text(
                text = "Chai",
                fontSize = 48.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Text(
                text = "Reader",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "ebooks,",
                fontSize = 20.sp,
                color = Color.White
            )
            Text(
                text = "audiobooks",
                fontSize = 20.sp,
                color = Color.White
            )
            Text(
                text = "chatbooks and ",
                fontSize = 20.sp,
                color = Color.White
            )
            Text(
                text = "translations",
                fontSize = 20.sp,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(48.dp))
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                elevation = 8.dp,
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Login",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Gray
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Email") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Password") },
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    MainScreen()
//                    Button(
//                        onClick = {
//                                val context = LocalContext.current
//                   context.startActivity(Intent(context, MainActivity::class.java))
//                        },
//                        modifier = Modifier.fillMaxWidth(),
//                        colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFFFF4081))
//                    ) {
//                        Text("Login", color = Color.White)
//                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Forget Password ?",
                        color = Color.Gray,
                        //modifier = Modifier.clickable { /* Handle forget password logic */ }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Don't have an account? Sign Up",
                        color = Color.Gray,
                       // modifier = Modifier.clickable { /* Handle sign up logic */ }
                    )
                }
            }
        }
    }
}

@Composable
fun IsUserLoginOrNot(): MutableState<Boolean> {
    val context = LocalContext.current
    val preferencesManager = remember { SpHelper(context) }
    val data = remember { mutableStateOf(preferencesManager.getBool("UserLogin",false)) }
    return data;
}




@Preview(showBackground = true)
@Composable
fun PreviewLoginScreen() {
    Surface(color = Color.White) {
        LoginScreen(/*onLoginClicked = { _, _ -> }*/)
    }
}



