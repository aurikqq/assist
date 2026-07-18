package com.aurikqq.assist.dock

import android.accessibilityservice.AccessibilityService
import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.util.Log
import android.view.KeyEvent
import android.view.WindowManager
import android.view.accessibility.AccessibilityEvent
import androidx.annotation.RequiresApi
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.platform.ComposeView
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.lifecycle.setViewTreeViewModelStoreOwner
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import com.aurikqq.assist.ui.theme.AssistTheme
import com.aurikqq.assist.ui.theme.NothingTheme
import kotlinx.coroutines.flow.MutableStateFlow
import java.util.Timer
import kotlin.concurrent.schedule

object dockParams {
    var isDockShown = MutableStateFlow(true)

    fun switchIsDockShown() {
        isDockShown.value = !isDockShown.value
    }
}

class OverlayService : LifecycleService(), ViewModelStoreOwner, SavedStateRegistryOwner {
    lateinit var windowManager: WindowManager
    var composeView: ComposeView? = null

    val _viewModelStore = ViewModelStore()
    override val viewModelStore: ViewModelStore = _viewModelStore

    val savedStateRegistryController = SavedStateRegistryController.create(this)
    override val savedStateRegistry: SavedStateRegistry = savedStateRegistryController.savedStateRegistry

    private var initX = 0
    private var initY = 0
    private var touchX = 0f
    private var touchY = 0f

    @RequiresApi(Build.VERSION_CODES.Q)
    @SuppressLint("MissingPermission")
    override fun onCreate() {
        super.onCreate()

        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        savedStateRegistryController.performRestore(null)

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        )

        composeView = ComposeView(this).apply {
            setViewTreeLifecycleOwner(this@OverlayService)
            setViewTreeViewModelStoreOwner(this@OverlayService)
            setViewTreeSavedStateRegistryOwner(this@OverlayService)

            setContent {
                val isShown = dockParams.isDockShown.collectAsState()

                if (isShown.value) {
                    NothingTheme() {
                        Dock(
                            this@OverlayService,
                            { rawX, rawY ->
                                initX = params.x
                                initY = params.y
                                touchX = rawX
                                touchY = rawY
                            },
                            { rawX, rawY ->
                                val deltaX = (rawX - touchX).toInt()
                                val deltaY = (rawY - touchY).toInt()

                                params.x = initX + deltaX
                                params.y = initY + deltaY

                                windowManager.updateViewLayout(composeView, params)
                            }
                        )
                    }
                }
            }
        }

        windowManager.addView(composeView, params)

//        val dragManager = DragManager(this@OverlayService)
//        dragManager.showView(composeView as ComposeView)
    }

    override fun onDestroy() {
        super.onDestroy()
        composeView?.let {
            windowManager.removeView(it)
        }
    }
}


class EssentialKeyService : AccessibilityService() {
    private val TAG = "EssentialKeyService"

    private val scanCode = 250
    private val pressDuration = 300L
    private val keyCode = 0
    private var pressCount = 0

    override fun onAccessibilityEvent(p0: AccessibilityEvent?) {

    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onKeyEvent(event: KeyEvent): Boolean {
        Log.d(TAG, "keycode ${event.keyCode}, scancode ${event.scanCode}")
        if (event.keyCode == keyCode && event.action == KeyEvent.ACTION_DOWN) {
            Log.d(TAG, "count: $pressCount")
            if (pressCount == 0) {
                Timer().schedule(pressDuration) {
                    if (pressCount == 1) {
                        dockParams.switchIsDockShown()

                        Log.d(TAG, "now is ${dockParams.isDockShown}")
                        Log.d(TAG, "1 press")
                    }
                    else {
                        Log.d(TAG, "2+ press")
                    }
                    pressCount = 0
                }
            }
            pressCount += 1

            Log.d(TAG, "Essential Key is pressed")

            return true
        }
        return false
    }

    override fun onInterrupt() {
        Log.d(TAG, "interrupted")
    }
}

// TODO
class ActionReceiver : BroadcastReceiver() {
    var value = false

    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.action == Intent.ACTION_CLOSE_SYSTEM_DIALOGS) {

        }
    }
}