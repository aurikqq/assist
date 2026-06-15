package com.aurikqq.assist

import android.Manifest
import android.app.ForegroundServiceStartNotAllowedException
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.SharedPreferences
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import androidx.core.content.PermissionChecker
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.aurikqq.assist.WakeWordService.Vosk
import org.json.JSONObject
import org.vosk.Recognizer
import org.vosk.android.RecognitionListener
import org.vosk.android.SpeechService
import java.util.Timer
import kotlin.concurrent.schedule

class ForegroundRecognition : Service(), RecognitionListener {
    lateinit var sharedPreferences: SharedPreferences
    lateinit var recognizer: Recognizer
    val intent = Intent("com.aurikqq.assistbasic.ALWAYS_LISTENING")

    lateinit var receiver: BroadcastReceiver

    override fun onCreate() {
        super.onCreate()

        sharedPreferences = getSharedPreferences(PREFERENCES_NAME, MODE_PRIVATE)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                "always_listening_foreground",
                "Foreground Service Channel",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(serviceChannel)
        }

        receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                when (intent?.action) {
                    ACTION_UPDATE_FOREGROUND_RECOGNIZER -> {
                        //updateRecognizer()
                    }
                }
            }
        }
        val filter = IntentFilter().apply {
            addAction(ACTION_UPDATE_FOREGROUND_RECOGNIZER)
        }
        LocalBroadcastManager.getInstance(this).registerReceiver(receiver, filter)
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

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_STOP_FOREGROUND_RECOGNIZER) {
            stopSelf()
            return START_NOT_STICKY
        }
        else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForeground()
            }

            if (!Vosk.isRunning) {
                startListening()
            }

            return START_STICKY
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun startForeground() {
        val micPermission =
            PermissionChecker.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
        if (micPermission != PermissionChecker.PERMISSION_GRANTED) {
            stopSelf()
            return
        }

        try {
            val notificationIntent = Intent(this, MainActivity::class.java)
            val pendingIntentFlags =
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            val pendingIntent = PendingIntent.getActivity(
                this,
                0,
                notificationIntent,
                pendingIntentFlags
            )

            val stopIntent = Intent(
                this,
                ForegroundRecognition::class.java).apply { action = ACTION_STOP_FOREGROUND_RECOGNIZER }
            val stopPendingIntent: PendingIntent =
                PendingIntent.getService(this, 1, stopIntent, PendingIntent.FLAG_IMMUTABLE)

            val notification = NotificationCompat.Builder(this, "always_listening_foreground")
                .setContentTitle("Assist is listening")
                .setSmallIcon(R.drawable.white_foreground_icon)
                .setContentIntent(pendingIntent)
                .setOngoing(true)
                .addAction(R.drawable.white_foreground_icon, "Stop", stopPendingIntent)
                .build()

            ServiceCompat.startForeground(
                this,
                2,
                notification,
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    ServiceInfo.FOREGROUND_SERVICE_TYPE_MICROPHONE
                } else {
                    0
                }
            )
        }
        catch (ex: Exception) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
                && ex is ForegroundServiceStartNotAllowedException
            ) {
                Log.e("Foreground", "Foreground service start not allowed")
            }
        }
    }

//    @SuppressLint("MissingPermission")
//    private fun startListening() {
//        Thread {
//            try {
//                updateRecognizer()
//                Vosk.speechService = SpeechService(Recognizer(Vosk.model, 16000.0f), 16000.0f)
//                Vosk.speechService?.startListening(this)
//                Vosk.isRunning = true
//                Log.d("WakeWordService", "Listening started")
//
//                while (Vosk.isRunning) {
//                    val nread = recorder.read(buffer, 0, bufferSize)
//                    if (nread > 0) {
//                        if (recognizer.acceptWaveForm(buffer, nread)) {
//                            val result = recognizer.result
//                            Log.d("ForegroundListening", "Result: $result")
//
//                            if (Vosk.isWaked || Vosk.isAlwaysListeningEnabled) {
//                                println("if1")
//                                commandsList.forEach { command ->
//                                    if (result.contains(command)) {
//                                        intent.putExtra("command", command)
//                                        LocalBroadcastManager.getInstance(this)
//                                            .sendBroadcast(intent)
//
//                                        updateRecognizer()
//                                    }
//                                }
//                            }
//                            else {
//                                println("if2")
//                                if (result.contains(ASSISTANT_NAME)) {
//                                    commandsList.forEach { command ->
//                                        if (result.contains(command)) {
//                                            intent.putExtra("command", command)
//                                            LocalBroadcastManager.getInstance(this)
//                                                .sendBroadcast(intent)
//
//                                            updateRecognizer()
//                                        }
//                                    }
//                                    Vosk.isWaked = true
//                                    updateRecognizer()
//                                    val scope = CoroutineScope(Dispatchers.Main)
//                                    scope.launch {
//                                        delay(10000L.milliseconds)
//                                        Vosk.isWaked = false
//
//                                        updateRecognizer()
//                                    }
//                                }
//                            }
//                        }
//                    }
//                }
//            }
//            catch (ex: Exception) {
//                Log.e("ForegroundListening", "Error: ${ex.message}")
//            }
//        }.start()

        private fun startListening() {
            Vosk.model?.let {
                Vosk.speechService = SpeechService(Recognizer(Vosk.model, 16000.0f), 16000.0f)
                Vosk.speechService?.startListening(this)
                Vosk.isRunning = true
                Log.d("Foreground", "Listening started")
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
                Log.d("Foreground", "Result: $text")

                if (!Vosk.isAlwaysListeningEnabled && text.contains(Vosk.assistantName, true)) {
                    Vosk.isWaked = true
                    Log.d("Foreground", "Waked")

                    Timer().schedule(10000L) {
                        Vosk.isWaked = false
                    }
                    Log.d("Foreground", "Unwaked")
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
            Log.d("Foreground", "Final result: $result")
            if (Vosk.isWaked || Vosk.isAlwaysListeningEnabled)
                if (result.isNotBlank()) {
                    val intent = Intent(ACTION_RECOGNITION_RESULT)
                    intent.putExtra(EXTRA_RECOGNIZED_TEXT, result)
                    LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
                }
        }

        override fun onError(exception: Exception?) {
            Log.e("Foreground", "Error: $exception")
        }

        override fun onTimeout() {
            Log.e("Foreground", "Timeout reached")
        }

        override fun onDestroy() {
            Vosk.speechService?.stop()
            Vosk.speechService?.shutdown()

            Vosk.isRunning = false

            super.onDestroy()
        }

    override fun onBind(intent: Intent?): IBinder? = null
}

//    private fun updateRecognizer() {
//        val commandsJsonArray =
//            commandsList.joinToString(separator = ", ", prefix = "[", postfix = "]") { "\"$it\"" }
//
//        recognizer = if (Vosk.isWaked || Vosk.isAlwaysListeningEnabled)
//            Recognizer(Vosk.model, 16000.0f, commandsJsonArray)
//        else
//            Recognizer(Vosk.model, 16000.0f, "[\"$ASSISTANT_NAME\"]$commandsJsonArray")
//
//        Log.d("ForegroundListening", "Now recognizing for: ${if (Vosk.isWaked || Vosk.isAlwaysListeningEnabled) "commands" else "wake-word"}")
//        Log.d("ForegroundListening", "$Vosk.isWaked $Vosk.isAlwaysListeningEnabled")
//    }