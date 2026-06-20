package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.example.data.FirebaseService
import com.example.data.PdfDatabase
import com.example.data.PdfRepository
import com.example.ui.IiucPdfApp
import com.example.ui.PdfViewModel
import com.example.ui.PdfViewModelFactory
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {

  private val database by lazy { PdfDatabase.getDatabase(applicationContext) }
  private val repository by lazy { PdfRepository(database.pdfDao()) }
  private val viewModel: PdfViewModel by viewModels {
    PdfViewModelFactory(repository, applicationContext)
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    FirebaseService.initialize(applicationContext)
    enableEdgeToEdge()
    setContent {
      val isDarkTheme by viewModel.isDarkMode.collectAsState()
      MyApplicationTheme(darkTheme = isDarkTheme) {
        Surface(
          modifier = Modifier.fillMaxSize(),
          color = MaterialTheme.colorScheme.background
        ) {
          IiucPdfApp(viewModel = viewModel)
        }
      }
    }
  }
}

