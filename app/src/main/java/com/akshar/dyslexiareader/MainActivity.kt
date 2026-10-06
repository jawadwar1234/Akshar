package com.akshar.dyslexiareader

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.akshar.dyslexiareader.domain.repository.WordMasteryRepository
import com.akshar.dyslexiareader.ui.navigation.AppNavHost
import com.akshar.dyslexiareader.ui.reader.TextToSpeechManager
import com.akshar.dyslexiareader.ui.theme.DyslexiaReaderTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var wordMasteryRepository: WordMasteryRepository

    @Inject
    lateinit var ttsManager: TextToSpeechManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            DyslexiaReaderTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    AppNavHost(
                        wordMasteryRepository = wordMasteryRepository,
                        ttsManager = ttsManager
                    )
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        ttsManager.shutdown()
    }
}
