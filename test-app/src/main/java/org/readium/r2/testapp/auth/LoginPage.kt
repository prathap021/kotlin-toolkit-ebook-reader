package org.readium.r2.testapp.auth
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




@Composable
fun LoginScreen(onLoginClicked: (String, String) -> Unit) {
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
                text = "ebooks, audiobooks, chatbooks and translations",
                fontSize = 16.sp,
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
                    Button(
                        onClick = { onLoginClicked(email, password)  },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFFFF4081))
                    ) {
                        Text("Login", color = Color.White)
                    }
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



@Preview(showBackground = true)
@Composable
fun PreviewLoginScreen() {
    Surface(color = Color.White) {
        LoginScreen(onLoginClicked = { _, _ -> })
    }
}



