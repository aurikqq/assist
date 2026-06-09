package com.aurikqq.assistbasic

import android.app.Service
import android.content.Intent
import android.content.SharedPreferences
import android.os.IBinder
import android.util.Log
import android.widget.Toast
import androidx.localbroadcastmanager.content.LocalBroadcastManager
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
    lateinit var assistantName: String
    var isAlwaysListeningEnabled = false

    //private val screenshotCommands = sharedPreferences.getString(SCREENSHOTS_COMMANDS, "") ?: com.aurikqq.assistbasic.screenshotCommands
    // i should make screenshotsCommands string (some way) and vice versa
    private var speechService: SpeechService? = null
    var model: Model? = null
    private var isWaked = false

    companion object {
        var isRunning = false
    }

    override fun onCreate() {
        super.onCreate()

        sharedPreferences = getSharedPreferences(PREFERENCES_NAME, MODE_PRIVATE)
        assistantName = sharedPreferences.getString(ASSISTANT_NAME, "ассист") ?: ""
        isAlwaysListeningEnabled = sharedPreferences.getBoolean(IS_ALWAYS_LISTENING_ENABLED, false)

        StorageService.unpack(this, "vosk-model-small-ru-0.22", "model",
            { model ->
                this.model = model
                startListening()
            },
            { ex ->
                Log.e("WakeWordService", "Unable to load model: $ex")
                stopSelf()
            })

        isRunning = true
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
        model?.let {
            speechService = SpeechService(Recognizer(model, 16000.0f), 16000.0f)
            speechService?.startListening(this)
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
                Log.d("WakeWordService", "Partial result: $text")

                if (!isAlwaysListeningEnabled && text.contains("$assistantName ", true)) {
                    isWaked = true
                    Log.d("WakeWordService", "Waked")

                    Timer().schedule(10000L) {
                        isWaked = false
                    }
                    Log.d("WakeWordService", "Unwaked")
                }

                if (isAlwaysListeningEnabled || isWaked) {
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
                else if (text.contains("$assistantName ", true)) {
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
            sendPartial(text)
        }
    }

    override fun onResult(hypothesis: String?) {
        hypothesis?.let {
            val text = JSONObject(it).optString("text")
            Log.d("WakeWordService", "Result: $text")

            if (!isAlwaysListeningEnabled && text.contains("$assistantName ", true)) {
                isWaked = true
                Log.d("WakeWordService", "Waked")

                Timer().schedule(10000L) {
                    isWaked = false
                }
                Log.d("WakeWordService", "Unwaked")
            }

            if (isAlwaysListeningEnabled || isWaked) {
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
            else if (text.contains("$assistantName ", true)) {
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
        speechService?.startListening(this)
    }

    override fun onFinalResult(hypothesis: String?) {
        val result = JSONObject(hypothesis).optString("text")
        Log.d("WakeWordService", "Final result: $result")
        if (isWaked || isAlwaysListeningEnabled)
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
        speechService?.stop()
        speechService?.shutdown()

        isRunning = false

        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}