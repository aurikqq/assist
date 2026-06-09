package com.aurikqq.assistbasic

import android.Manifest
import android.annotation.SuppressLint
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
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import androidx.core.content.PermissionChecker
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.vosk.Model
import org.vosk.Recognizer
import org.vosk.android.StorageService
import kotlin.time.Duration.Companion.milliseconds

class ForegroundRecognition : Service() {
    lateinit var sharedPreferences: SharedPreferences
    var model: Model? = null
    lateinit var recognizer: Recognizer
    var isListening = false
    var isWaked = false
    var isAlwaysListeningEnabled = false
    val intent = Intent("com.aurikqq.assistbasic.ALWAYS_LISTENING")

    lateinit var receiver: BroadcastReceiver

    override fun onCreate() {
        super.onCreate()

        sharedPreferences = getSharedPreferences(PREFERENCES_NAME, MODE_PRIVATE)
        isAlwaysListeningEnabled = sharedPreferences.getBoolean(IS_ALWAYS_LISTENING_ENABLED, false)

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
                        updateRecognizer()
                    }
                }
            }
        }
        val filter = IntentFilter().apply {
            addAction(ACTION_UPDATE_FOREGROUND_RECOGNIZER)
        }
        LocalBroadcastManager.getInstance(this).registerReceiver(receiver, filter)
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

            if (!isListening) {
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
                Log.e("ForegroundListening", "Foreground service start not allowed")
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun startListening() {
        Thread {
            try {
                StorageService.unpack(this, "vosk-model-small-ru-0.22", "model",
                    { model ->
                        this.model = model
                    },
                    { ex ->
                        Log.e("WakeWordService", "Unable to load model: $ex")
                        stopSelf()
                    })
                updateRecognizer()

                val bufferSize = AudioRecord.getMinBufferSize(
                    16000,
                    AudioFormat.CHANNEL_IN_MONO,
                    AudioFormat.ENCODING_PCM_16BIT
                )
                val recorder = AudioRecord(
                    MediaRecorder.AudioSource.MIC,
                    16000,
                    AudioFormat.CHANNEL_IN_MONO,
                    AudioFormat.ENCODING_PCM_16BIT,
                    bufferSize
                )

                val buffer = ByteArray(bufferSize)
                recorder.startRecording()
                isListening = true

                while (isListening) {
                    val nread = recorder.read(buffer, 0, bufferSize)
                    if (nread > 0) {
                        if (recognizer.acceptWaveForm(buffer, nread)) {
                            val result = recognizer.result
                            Log.d("ForegroundListening", "Result: $result")

                            if (isWaked || isAlwaysListeningEnabled) {
                                println("if1")
                                commandsList.forEach { command ->
                                    if (result.contains(command)) {
                                        intent.putExtra("command", command)
                                        LocalBroadcastManager.getInstance(this)
                                            .sendBroadcast(intent)

                                        updateRecognizer()
                                    }
                                }
                            }
                            else {
                                println("if2")
                                if (result.contains(ASSISTANT_NAME)) {
                                    commandsList.forEach { command ->
                                        if (result.contains(command)) {
                                            intent.putExtra("command", command)
                                            LocalBroadcastManager.getInstance(this)
                                                .sendBroadcast(intent)

                                            updateRecognizer()
                                        }
                                    }
                                    isWaked = true
                                    updateRecognizer()
                                    val scope = CoroutineScope(Dispatchers.Main)
                                    scope.launch {
                                        delay(10000L.milliseconds)
                                        isWaked = false

                                        updateRecognizer()
                                    }
                                }
                            }
                        }
                    }
                }
            }
            catch (ex: Exception) {
                Log.e("ForegroundListening", "Error: ${ex.message}")
            }
        }.start()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun updateRecognizer() {
        val commandsJsonArray =
            commandsList.joinToString(separator = ", ", prefix = "[", postfix = "]") { "\"$it\"" }

        recognizer = if (isWaked || isAlwaysListeningEnabled)
            Recognizer(model, 16000.0f, commandsJsonArray)
        else
            Recognizer(model, 16000.0f, "[\"$ASSISTANT_NAME\"]$commandsJsonArray")

        Log.d("ForegroundListening", "Now recognizing for: ${if (isWaked || isAlwaysListeningEnabled) "commands" else "wake-word"}")
        Log.d("ForegroundListening", "$isWaked $isAlwaysListeningEnabled")
    }
}