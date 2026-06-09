package com.aurikqq.assistbasic.commands

import android.content.Context
import android.media.AudioManager
import android.provider.MediaStore

class SoundHandler(context: Context) {
    val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager

    fun lowerMediaVolume() {
        audioManager.adjustStreamVolume(
            AudioManager.STREAM_MUSIC,
            AudioManager.ADJUST_LOWER,
            AudioManager.FLAG_SHOW_UI
        )
    }

    fun raiseMediaVolume() {
        audioManager.adjustStreamVolume(
            AudioManager.STREAM_MUSIC,
            AudioManager.ADJUST_RAISE,
            AudioManager.FLAG_SHOW_UI
        )
    }

    fun setMediaVolume(value: Int) {
        audioManager.setStreamVolume(
            AudioManager.STREAM_MUSIC,
            value,
            AudioManager.FLAG_SHOW_UI
        )
    }

    fun getMediaVolume(): Int {
        return audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
    }
}