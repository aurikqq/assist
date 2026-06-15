package com.aurikqq.assist

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.Wallpapers
import com.aurikqq.assist.screens.MainScreen
import com.aurikqq.assist.ui.theme.AssistBasicTheme
import java.util.Locale

var partialText: String = ""

class MainActivity : ComponentActivity() {
    private val partialReceiver = PartialReceiver()
    lateinit var textToSpeech : TextToSpeech

    @SuppressLint("NewApi")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        textToSpeech = TextToSpeech(this) { status ->
            if (status == TextToSpeech.SUCCESS) {
                Log.d("TTS", "Init Success")
                TTS.init(textToSpeech)
                textToSpeech.language = Locale.forLanguageTag("ru")
                if (General().isInternetAvailable(context = applicationContext))
                    textToSpeech.voice = textToSpeech.voices.find { it.name == "ru-ru-x-rud-network" }
                else
                    textToSpeech.voice = textToSpeech.voices.find { it.name == "ru-ru-x-rud-local" }
            } else {
                Log.d("TTS", "Init Failed")
            }
        }

        enableEdgeToEdge()
        setContent {
            AssistBasicTheme {
                MainScreen()
            }
        }
    }

    @SuppressLint("UnspecifiedRegisterReceiverFlag")
    override fun onResume() {
        super.onResume()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(partialReceiver, IntentFilter("com.aurikqq.assistbasic.PARTIAL_RECEIVER"), RECEIVER_EXPORTED)
        }
        else {
            registerReceiver(partialReceiver, IntentFilter("com.aurikqq.assistbasic.PARTIAL_RECEIVER"))
        }
    }

    override fun onPause() {
        super.onPause()

        val intent = Intent(this, ForegroundRecognition::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent)
        }
        else {
            startService(intent)
        }
        startService(Intent(this, OverlayService::class.java))
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(partialReceiver)
        textToSpeech.shutdown()
    }
}

class PartialReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        val text = intent?.getStringExtra("text").toString()
        if (text.isNotBlank()) {
            partialText += ", $text"
        }
    }
}

object TTS {
    var tts : TextToSpeech? = null

    fun init(textToSpeech: TextToSpeech) {
        tts = textToSpeech
    }

    fun say(text: String) {
        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "assist")
    }

    fun isSpeaking(): Boolean {
        return tts?.isSpeaking == true
    }
}

@Preview(showBackground = true, showSystemUi = true,
    wallpaper = Wallpapers.RED_DOMINATED_EXAMPLE,
    uiMode = Configuration.UI_MODE_NIGHT_YES or Configuration.UI_MODE_TYPE_NORMAL
)
@Composable
fun AssistDark() {
    AssistBasicTheme {
        MainScreen()
    }
}

@Preview(showBackground = true, showSystemUi = true,
    wallpaper = Wallpapers.RED_DOMINATED_EXAMPLE,
    uiMode = Configuration.UI_MODE_TYPE_NORMAL
)
@Composable
fun AssistLight() {
    AssistBasicTheme {
        MainScreen()
    }
}

@Preview(showBackground = true, showSystemUi = true,
    wallpaper = Wallpapers.BLUE_DOMINATED_EXAMPLE,
    uiMode = Configuration.UI_MODE_NIGHT_YES or Configuration.UI_MODE_TYPE_NORMAL
)
@Composable
fun AssistDarkBlue() {
    AssistBasicTheme {
        MainScreen()
    }
}

@Preview(showBackground = true, showSystemUi = true,
    wallpaper = Wallpapers.YELLOW_DOMINATED_EXAMPLE,
    uiMode = Configuration.UI_MODE_TYPE_NORMAL
)
@Composable
fun AssistLightYellow() {
    AssistBasicTheme {
        MainScreen()
    }
}

@Preview(showBackground = true, showSystemUi = true,
    wallpaper = Wallpapers.GREEN_DOMINATED_EXAMPLE,
    uiMode = Configuration.UI_MODE_TYPE_NORMAL
)
@Composable
fun AssistLightGreen() {
    AssistBasicTheme {
        MainScreen()
    }
}

// TODO
// make cards clickable and leading to their categories
// make some minimal commands ("start/stop/next/prev music", screenshots, remembering things)