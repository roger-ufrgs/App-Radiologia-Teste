package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.ui.RadApp
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.RadViewModel

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    
    val viewModel = RadViewModel()
    
    setContent {
      MyApplicationTheme {
        RadApp(viewModel = viewModel)
      }
    }
  }
}

