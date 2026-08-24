package com.aurikqq.assist.commands

import android.app.Notification
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.media.session.MediaSession
import android.media.session.MediaSessionManager
import android.os.Binder
import android.os.IBinder
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.support.v4.media.session.PlaybackStateCompat.REPEAT_MODE_ALL
import android.support.v4.media.session.PlaybackStateCompat.REPEAT_MODE_NONE
import android.support.v4.media.session.PlaybackStateCompat.REPEAT_MODE_ONE
import android.support.v4.media.session.PlaybackStateCompat.SHUFFLE_MODE_ALL
import android.support.v4.media.session.PlaybackStateCompat.SHUFFLE_MODE_NONE
import android.util.Log
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat.getSystemService
import androidx.core.content.getSystemService

class MusicHandler private constructor(private val context: Context) {
    private var mediaController: MediaControllerCompat? = null
    private var currentToken: MediaSessionCompat.Token? = null

    init {
        if (!isNotificationListenerEnabled()) {
            Log.w("MusicHandler", "Notification listener is not enabled")
        }
    }

    private fun isNotificationListenerEnabled(): Boolean {
        val enabledListeners = NotificationManagerCompat.getEnabledListenerPackages(context)
        return enabledListeners.contains(context.packageName)
    }

    fun connectToMediaSessionFromToken(token: MediaSessionCompat.Token) {
        try {
            mediaController = MediaControllerCompat(context, token)
            mediaController?.registerCallback(mediaControllerCallback)
            this.currentToken = token

            val playbackState = mediaController?.playbackState
            val metadata = mediaController?.metadata
            Log.d("MusicHandler", "Playback state: $playbackState, metadata: $metadata")

            if (playbackState?.state == PlaybackStateCompat.STATE_PLAYING ||
                playbackState?.state == PlaybackStateCompat.STATE_PAUSED) {
                Log.d("MusicHandler", "Connected to media session: ${mediaController?.packageName}")
            }
            else {
                Log.w("MusicHandler", "Connected session is not currently playing or paused")
            }
        } catch (ex: SecurityException) {
            Log.e("MusicHandler", "SecurityException when connecting with token: $ex")
            //mediaController = null
            //this.currentToken = null
        }
        catch (ex: Exception) {
            Log.e("MusicHandler", "Error connecting to media session with token: $ex")
            //mediaController = null
            //this.currentToken = null
        }
    }

    companion object {
        @Volatile
        private var INSTANCE: MusicHandler? = null

        fun getInstance(context: Context): MusicHandler {
            val context = context.applicationContext
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: MusicHandler(context).also {
                    INSTANCE = it
                    Log.d("MusicHandler", "MusicHandler instance created")
                }
            }
        }
    }

    fun updateActiveMediaSessionToken(newToken: MediaSessionCompat.Token?) {
        Log.d("MusicHandler", "Instance ${System.identityHashCode(this)} updating token: $newToken")
        if (newToken == null) {
            Log.d("MusicHandler", "Received null token")
            if (mediaController != null) {
                mediaController?.unregisterCallback(mediaControllerCallback)
                mediaController = null
            }
            currentToken = null
            return
        }

        if (newToken == currentToken && mediaController != null) {
            Log.d("MusicHandler", "Received same token")
            return
        }

        Log.d("MusicHandler", "Received new token: $newToken")
        mediaController?.unregisterCallback(mediaControllerCallback)
        mediaController = null

        connectToMediaSessionFromToken(newToken)
    }

    private val mediaControllerCallback = object : MediaControllerCompat.Callback() {
        override fun onPlaybackStateChanged(state: PlaybackStateCompat?) {
            super.onPlaybackStateChanged(state)
            Log.d("MusicHandler", "Playback state changed: $state")
        }

        override fun onMetadataChanged(metadata: MediaMetadataCompat?) {
            super.onMetadataChanged(metadata)
            Log.d("MusicHandler", "Metadata changed: $metadata")
        }

        override fun onSessionDestroyed() {
            super.onSessionDestroyed()
            Log.d("MusicHandler", "Media session destroyed")
            mediaController?.unregisterCallback(this)
            mediaController = null
            currentToken = null
        }
    }

    fun play() {
        if (mediaController == null) {
            Log.w("MusicHandler", "MediaController is null. Cannot play")
            return
        }
        mediaController?.transportControls?.play()
    }

    fun pause() {
        if (mediaController == null) {
            Log.w("MusicHandler", "MediaController is null. Cannot pause")
            return
        }
        mediaController?.transportControls?.pause()
    }

    fun next() {
        if (mediaController == null) {
            Log.w("MusicHandler", "MediaController is null. Cannot skip to next")
            return
        }
        mediaController?.transportControls?.skipToNext()
    }

    fun prev() {
        if (mediaController == null) {
            Log.w("MusicHandler", "MediaController is null. Cannot skip to previous")
            return
        }
        mediaController?.transportControls?.skipToPrevious()
    }

    fun shuffle_on() {
        if (mediaController == null) {
            Log.w("MusicHandler", "MediaController is null. Cannot shuffle")
            return
        }
        mediaController?.transportControls?.setShuffleMode(SHUFFLE_MODE_ALL)
    }

    fun shuffle_off() {
        if (mediaController == null) {
            Log.w("MusicHandler", "MediaController is null. Cannot turn shuffle off")
            return
        }
        mediaController?.transportControls?.setShuffleMode(SHUFFLE_MODE_NONE)
    }

    fun repeat_all() {
        if (mediaController == null) {
            Log.w("MusicHandler", "MediaController is null. Cannot repeat all")
            return
        }
        mediaController?.transportControls?.setRepeatMode(REPEAT_MODE_ALL)
    }

    fun repeat_one() {
        if (mediaController == null) {
            Log.w("MusicHandler", "MediaController is null. Cannot repeat one")
            return
        }
        mediaController?.transportControls?.setRepeatMode(REPEAT_MODE_ONE)
    }

    fun repeat_off() {
        if (mediaController == null) {
            Log.w("MusicHandler", "MediaController is null. Cannot turn repeat off")
            return
        }
        mediaController?.transportControls?.setRepeatMode(REPEAT_MODE_NONE)
        //mediaController?.transportControls?.sendCustomAction("Add to collection", null)
        //Action:mName='Add to collection, mIcon=2131233066, mExtras=Bundle[mParcelledData.dataSize=144]
    }
}

data object Notifications {
    var notifications = arrayOf<StatusBarNotification>()
}

fun isMediaPlaying(context: Context): Boolean {
    val manager = context.getSystemService<MediaSessionManager>()!!
    val component = ComponentName(context, NotificationListener::class.java)
    val sessions = manager.getActiveSessions(component)

    sessions.removeIf { it.packageName == "com.nothing.hearthstone" }

    return sessions.isNotEmpty()
}

class NotificationListener : NotificationListenerService() {
    private val TAG = "NotificationListener"

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "NotificationListenerService created.")
    }

    override fun onListenerConnected() {
        super.onListenerConnected()
        Log.d(TAG, "NotificationListenerService connected.")
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        super.onNotificationPosted(sbn)
        Notifications.notifications = activeNotifications
        if (sbn != null) {
            Log.d(TAG, "Notification posted: ${sbn.packageName}, ID: ${sbn.id}")
            val extras = sbn.notification.extras
            val parcelableToken = extras.getParcelable<MediaSession.Token>(Notification.EXTRA_MEDIA_SESSION)

            if (parcelableToken != null) {
                try {
                    val token = MediaSessionCompat.Token.fromToken(parcelableToken)
                    Log.d(TAG, "Media session token: $token")
                    val musicHandler = MusicHandler.getInstance(applicationContext)
                    musicHandler.updateActiveMediaSessionToken(token)
                }
                catch (ex: Exception) {
                    Log.e(TAG, "Error converting media session token: $ex")
                }
            }
            else {
                Log.d(TAG, "Media session token is null in notification from ${sbn.packageName}")
            }
        }
        else {
            Log.d(TAG, "Notification is null")
        }
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification?) {
        super.onNotificationRemoved(sbn)
        Notifications.notifications = activeNotifications
        if (sbn != null) {
            Log.d(TAG, "Notification removed: ${sbn.packageName}, ID: ${sbn.id}")
        }
    }

    override fun onListenerDisconnected() {
        super.onListenerDisconnected()

        Log.d(TAG, "NotificationListenerService disconnected.")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "NotificationListenerService destroyed.")
    }
}