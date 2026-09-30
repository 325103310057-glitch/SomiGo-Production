package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.di.AppContainer
import com.example.navigation.SomiGoApp
import com.example.ui.theme.SomiGoTheme

class MainActivity : ComponentActivity() {

    private lateinit var appContainer: AppContainer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        appContainer = AppContainer(applicationContext)

        setContent {
            SomiGoTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    SomiGoApp(appContainer = appContainer)
                }
            }
        }
    }
}
