package com.example

import android.content.Context
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.PdfDatabase
import com.example.data.PdfRepository
import com.example.ui.IiucPdfApp
import com.example.ui.PdfViewModel
import com.example.ui.theme.MyApplicationTheme
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = RobolectricDeviceQualifiers.Pixel8, sdk = [36])
class GreetingScreenshotTest {

  @get:Rule val composeTestRule = createComposeRule()

  @Test
  fun greeting_screenshot() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    
    // In-memory database setup for testing UI visually
    val db = Room.inMemoryDatabaseBuilder(context, PdfDatabase::class.java)
        .allowMainThreadQueries()
        .build()
        
    val dao = db.pdfDao()
    val repository = PdfRepository(dao)
    val viewModel = PdfViewModel(repository)

    composeTestRule.setContent {
      MyApplicationTheme {
        IiucPdfApp(viewModel = viewModel)
      }
    }

    composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/greeting.png")
    
    db.close()
  }
}
