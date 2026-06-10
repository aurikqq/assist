package com.aurikqq.assistbasic

import android.annotation.SuppressLint
import android.app.Activity
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.edit
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.aurikqq.assistbasic.commands.MusicHandler
import com.aurikqq.assistbasic.commands.SoundHandler
import java.time.LocalTime

val general = General()

@RequiresApi(Build.VERSION_CODES.O)
fun executeCommand(command: String, activity: Activity) {
    val context = activity.applicationContext
    val sharedPreferences = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)
    val musicHandler = MusicHandler.getInstance(context.applicationContext)
    val soundHandler = SoundHandler(context)
    //val alwaysListeningServiceIntent = Intent(context, ForegroundRecognition::class.java)

    when {
        musicPlayKeywords.any { keyword -> command.split("\\s+".toRegex()).contains(keyword) } -> {
            musicHandler.play()
        }
        musicPauseKeywords.any { keyword -> command.split("\\s+".toRegex()).contains(keyword) } -> {
            musicHandler.pause()
        }
        musicNextKeywords.any { keyword -> command.split("\\s+".toRegex()).contains(keyword) } -> {
            musicHandler.next()
        }
        musicPrevKeywords.any { keyword -> command.split("\\s+".toRegex()).contains(keyword) } -> {
            musicHandler.prev()
        }
        musicShuffleOnKeywords.any { keyword -> command.split("\\s+".toRegex()).contains(keyword) } -> {
            musicHandler.shuffle_on()
        }
        musicShuffleOffKeywords.any { keyword -> command.split("\\s+".toRegex()).contains(keyword) } -> {
            musicHandler.shuffle_off()
        }
        musicRepeatAllKeywords.any { keyword -> command.split("\\s+".toRegex()).contains(keyword) } -> {
            musicHandler.repeat_all()
        }
        musicRepeatOneKeywords.any { keyword -> command.split("\\s+".toRegex()).contains(keyword) } -> {
            musicHandler.repeat_one()
        }
        musicRepeatOffKeywords.any { keyword -> command.split("\\s+".toRegex()).contains(keyword) } -> {
            musicHandler.repeat_off()
        }
        alwaysListeningOnKeywords.any { keyword -> command.split("\\s+".toRegex()).contains(keyword) } -> {
            sharedPreferences.edit { putBoolean(IS_ALWAYS_LISTENING_ENABLED, true) }
            sendBroadcast(context, ACTION_UPDATE_FOREGROUND_RECOGNIZER)
        }
        alwaysListeningOffKeywords.any { keyword -> command.split("\\s+".toRegex()).contains(keyword) } -> {
            sharedPreferences.edit { putBoolean(IS_ALWAYS_LISTENING_ENABLED, false) }

            sendBroadcast(context, ACTION_UPDATE_FOREGROUND_RECOGNIZER)
        }
        lowerMusicVolume.any { keyword -> command.split("\\s+".toRegex()).contains(keyword) } -> {
            soundHandler.lowerMediaVolume()
        }
        raiseMusicVolume.any { keyword -> command.split("\\s+".toRegex()).contains(keyword) } -> {
            soundHandler.raiseMediaVolume()
        }
        setMusicVolume.any { keyword -> command.split("\\s+".toRegex()).contains(keyword) } -> {
            val volume = general.stringToNumber(command.removePrefix("громкость").removePrefix("звук"))
                ?: -1
            if (volume >= 0) soundHandler.setMediaVolume(volume.coerceIn(0, 16))
        }
        sayTime.any { keyword -> command.split("\\s+".toRegex()).contains(keyword) } -> {
            if (soundHandler.getMediaVolume() == 0) {
                soundHandler.setMediaVolume(5)
                TTS.say("сейчас ${LocalTime.now().hour} ${if (LocalTime.now().minute < 10) "ноль" else ""} ${LocalTime.now().minute}")
                do {} while (TTS.isSpeaking())
                soundHandler.setMediaVolume(0)
            }
            else {
                TTS.say("сейчас ${LocalTime.now().hour} ${if (LocalTime.now().minute < 10) "ноль" else ""} ${LocalTime.now().minute}")
            }
        }
    }
    commandSignal(context)
}

/* TODO */ // set own sound signal
@SuppressLint("MissingPermission")
private fun commandSignal(context: Context) {
    if (general.isScreenOff(context)) {
        try {
            val notificationIntent = Intent()
            val pendingIntentFlags =
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            val pendingIntent = PendingIntent.getActivity(
                context,
                0,
                notificationIntent,
                pendingIntentFlags
            )

            val notification = NotificationCompat.Builder(context, "command_signal_notification")
                .setContentTitle("Command is done!")
                .setContentText("Just a notification to inform you that Assist successfully executed your command while the screen was off")
                .setSmallIcon(R.drawable.white_foreground_icon)
                .setContentIntent(pendingIntent)
                .setTimeoutAfter(100)
                .build()
            with(NotificationManagerCompat.from(context)) {
                notify(1, notification)
            }
        } catch (ex: Exception) {
            Log.e("commandSignalNotification", "Error: ${ex.message}")
        }
    }
//    else {
//        try {
//            val notificationUri: Uri? = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
//            if (notificationUri != null) {
//                val ringtone : Ringtone = RingtoneManager.getRingtone(context, notificationUri)
//
//                ringtone.audioAttributes = AudioAttributes.Builder()
//                    .setLegacyStreamType(AudioManager.STREAM_NOTIFICATION)
//                    .build()
//                ringtone.play()
//            }
//        } catch (ex: Exception) {
//            Log.e("commandSignalSound", "Error: ${ex.message}")
//        }
//    }
}

private fun sendBroadcast(context: Context, action: String) {
    LocalBroadcastManager.getInstance(context)
        .sendBroadcast(Intent(action))
}