package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.ViewModelProvider
import com.example.ui.VisionMindApp
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.VisionMindViewModel
import com.example.viewmodel.VisionMindViewModelFactory

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Create the ViewModel with Android application context
        val viewModel = ViewModelProvider(
            this,
            VisionMindViewModelFactory(application)
        )[VisionMindViewModel::class.java]

        setContent {
            MyApplicationTheme {
                VisionMindApp(viewModel = viewModel)
            }
        }
    }
}
