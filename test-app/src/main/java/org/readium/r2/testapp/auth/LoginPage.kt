package org.readium.r2.testapp.auth
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.Brush
import androidx.compose.foundation.background
import androidx.compose.ui.graphics.Shape

@Composable
fun LoginScreen(onLoginClicked: (String, String) -> Unit) {


    var username by remember { mutableStateOf("hello@reallygreatesite.com") }
    var password by remember { mutableStateOf("**********") }

    val gradientColors = listOf(
        Color(0xFFff4135), // Start color
        Color(0xFFff7a3f),
        Color(0xFFff7a3f),
        Color(0xFFff7a3f)
        // End color
    )

    val gradientBrush = Brush.verticalGradient(gradientColors)
    Box(
        modifier = Modifier.fillMaxSize().background(brush = gradientBrush)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Bottom,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Add other composables if needed
            Text(text = "chai",color = Color.White,modifier = Modifier.padding(bottom = 8.dp))
            Text(text = "Reader",color = Color.White,modifier = Modifier.padding(bottom = 8.dp))
            Text(text = "ebooks,",color = Color.White,modifier = Modifier.padding(bottom = 8.dp))
            Text(text = "audiobooksn",color = Color.White,modifier = Modifier.padding(bottom = 8.dp))
            Text(text = "chatbooks and",color = Color.White,modifier = Modifier.padding(bottom = 8.dp))
            Text(text = "translations",color = Color.White,modifier = Modifier.padding(bottom = 30.dp))

            // Box inside the Column
            Box(
                modifier = Modifier.fillMaxWidth()
                    .size(width = 200.dp, height = 500.dp)
                    .background(Color.White)
            ) {
                      Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

                          Text(text = "Login",color = Color.Black,modifier = Modifier.padding(bottom = 8.dp))
                          Text(text = "Login to continue",color = Color.Black,modifier = Modifier.padding(bottom = 30.dp))
            OutlinedTextField(
                value = username,
                onValueChange = { username = it },
                label = { Text("Username") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                modifier = Modifier
                    .fillMaxWidth() // Fill the available width
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            )

            Spacer(modifier = Modifier.height(15.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                modifier = Modifier
                    .fillMaxWidth() // Fill the available width
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))
                          Button(
                              onClick = { /*onLoginClicked(username, password)*/ },
                              modifier = Modifier
                                  .width(150.dp)
                                  .padding(10.dp) ,
                              colors = ButtonDefaults.buttonColors(
                                  backgroundColor = Color.Blue, // Change the background color
                                  contentColor = Color.White // Change the text color
                              ),
                              // Add some padding for spacing
                          ) {
                              Text(text = "Login")
                          }
                          Spacer(modifier = Modifier.height(10.dp))
                          Text(text = "Forget Password ?",color = Color.Black,modifier = Modifier.padding(bottom = 8.dp))
                          Spacer(modifier = Modifier.height(5.dp))
                          Text(text = "Dont't have an account? Sign Up",color = Color.Black,modifier = Modifier.padding(bottom = 8.dp))


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



