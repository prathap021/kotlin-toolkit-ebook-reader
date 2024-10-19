package org.readium.r2.testapp



import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import androidx.compose.ui.platform.LocalContext
import android.content.Context

class SplashActivity : ComponentActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val intent = Intent(this, MainActivity::class.java)
        this.startActivity(intent)


//        setContent {
//            val context = LocalContext.current
//            SplashView{
//
//
//                Log.i("Home Screen called","wait for context")
//
//                val intent = Intent(context, MainActivity::class.java)
//                context.startActivity(intent)
//            }
//        }
    }
}






@Composable
fun SplashView(navigateToHome:  () -> Unit) {

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable._44x144_px),
            contentDescription = "Splash Screen Logo",
            modifier = Modifier.size(150.dp)
        )
    }

    // Simulate a delay for splash screen
    LaunchedEffect(Unit) {
        delay(2000) // 2 seconds
        navigateToHome()

    }
}
