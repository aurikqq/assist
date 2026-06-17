package com.aurikqq.assist.voice

import android.app.Service
import android.content.Intent
import android.content.SharedPreferences
import android.os.IBinder
import android.util.Log
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.aurikqq.assist.ACTION_RECOGNITION_RESULT
import com.aurikqq.assist.ACTION_REQUEST_COMMAND_EXECUTING
import com.aurikqq.assist.ACTION_REQUEST_SCREENSHOT
import com.aurikqq.assist.ASSISTANT_NAME
import com.aurikqq.assist.EXTRA_RECOGNIZED_TEXT
import com.aurikqq.assist.IS_ALWAYS_LISTENING_ENABLED
import com.aurikqq.assist.PREFERENCES_NAME
import com.aurikqq.assist.commandsList
import com.aurikqq.assist.screenshotCommands
import org.json.JSONObject
import org.vosk.Model
import org.vosk.Recognizer
import org.vosk.android.RecognitionListener
import org.vosk.android.SpeechService
import org.vosk.android.StorageService
import java.util.Timer
import kotlin.concurrent.schedule

class WakeWordService : Service(), RecognitionListener {
    private lateinit var sharedPreferences: SharedPreferences

    //private val screenshotCommands = sharedPreferences.getString(SCREENSHOTS_COMMANDS, "") ?: com.aurikqq.assistbasic.screenshotCommands
    // i should make screenshotsCommands string (some way) and vice versa
    object Vosk {
        var model : Model? = null
        var isRunning = false
        var isWaked = false
        var isAlwaysListeningEnabled = false
        var speechService: SpeechService? = null
        lateinit var assistantName: String

    }

    override fun onCreate() {
        super.onCreate()

        sharedPreferences = getSharedPreferences(PREFERENCES_NAME, MODE_PRIVATE)
        Vosk.assistantName = sharedPreferences.getString(ASSISTANT_NAME, "ассист") ?: ""
        Vosk.isAlwaysListeningEnabled = sharedPreferences.getBoolean(IS_ALWAYS_LISTENING_ENABLED, false)

        StorageService.unpack(this, "vosk-model-small-ru-0.22", "model",
            { model ->
                Vosk.model = model
                startListening()
            },
            { ex ->
                Log.e("WakeWordService", "Unable to load model: $ex")
                stopSelf()
            })

        Vosk.isRunning = true
    }

    private fun requestScreenshot() {
        LocalBroadcastManager.getInstance(this)
            .sendBroadcast(Intent(ACTION_REQUEST_SCREENSHOT))
    }
    private fun requestCommandExecuting(command: String) {
        LocalBroadcastManager.getInstance(this)
            .sendBroadcast(Intent(ACTION_REQUEST_COMMAND_EXECUTING).apply {
                putExtra("command", command)
            })
    }

    private fun startListening() {
        Vosk.model?.let {
            Vosk.speechService = SpeechService(Recognizer(Vosk.model, 16000.0f), 16000.0f)
            Vosk.speechService?.startListening(this)
            Log.d("WakeWordService", "Listening started")
        }
    }

    private fun sendPartial(text: String) {
        val intent = Intent("com.aurikqq.assistbasic.PARTIAL_RECEIVER")
        intent.putExtra("text", text)
        sendBroadcast(intent)
    }

    override fun onPartialResult(hypothesis: String?) {
        hypothesis?.let {
            val text = JSONObject(it).optString("partial")
            if (text.isNotBlank()) {
                sendPartial(text)
            }
        }
    }

    override fun onResult(hypothesis: String?) {
        hypothesis?.let {
            val text = JSONObject(it).optString("text")
            Log.d("WakeWordService", "Result: $text")

            if (!Vosk.isAlwaysListeningEnabled && text.contains(Vosk.assistantName, true)) {
                Vosk.isWaked = true
                Log.d("WakeWordService", "Waked")

                Timer().schedule(10000L) {
                    Vosk.isWaked = false
                }
                Log.d("WakeWordService", "Unwaked")
            }

            if (Vosk.isAlwaysListeningEnabled || Vosk.isWaked) {
                screenshotCommands.forEach { command ->
                    if (text.contains(command, true)) {
                        requestScreenshot()
                    }
                }
                commandsList.forEach { command ->
                    if (text.contains(command, true)) {
                        if (command in listOf("звук", "громкость")) {
                            requestCommandExecuting(text)
                        } else {
                            requestCommandExecuting(command)
                        }
                        // TODO:
                    }
                }
            }
            else if (text.contains("$Vosk.assistantName ", true)) {
                screenshotCommands.forEach { command ->
                    if (text.contains(command, true)) {
                        requestScreenshot()
                    }
                }
                commandsList.forEach { command ->
                    if (text.contains(command, true)) {
                        if (command in listOf("звук", "громкость")) {
                            requestCommandExecuting(text)
                        } else {
                            requestCommandExecuting(command)
                        }
                    }
                }
            }
        }
        Vosk.speechService?.startListening(this)
    }

    override fun onFinalResult(hypothesis: String?) {
        val result = JSONObject(hypothesis).optString("text")
        Log.d("WakeWordService", "Final result: $result")
        if (Vosk.isWaked || Vosk.isAlwaysListeningEnabled)
        if (result.isNotBlank()) {
            val intent = Intent(ACTION_RECOGNITION_RESULT)
            intent.putExtra(EXTRA_RECOGNIZED_TEXT, result)
            LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
        }
    }

    override fun onError(exception: Exception?) {
        Log.e("WakeWordService", "Error: $exception")
    }

    override fun onTimeout() {
        Log.e("WakeWordService", "Timeout reached")
    }

    override fun onDestroy() {
        Vosk.speechService?.stop()
        Vosk.speechService?.shutdown()

        Vosk.isRunning = false

        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}