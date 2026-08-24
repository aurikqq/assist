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
import android.view.WindowManager.LayoutParams
import android.view.accessibility.AccessibilityEvent
import androidx.annotation.RequiresApi
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.unit.dp
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
import androidx.core.content.edit
import androidx.core.util.TypedValueCompat.dpToPx

object dockParams {
    var isDockShown = MutableStateFlow(true)

    fun switchIsDockShown() {
        isDockShown.value = !isDockShown.value
    }
}

class OverlayService : LifecycleService(), ViewModelStoreOwner, SavedStateRegistryOwner {
    lateinit var windowManager: WindowManager
    var composeView: ComposeView? = null
    private lateinit var params: WindowManager.LayoutParams

    val _viewModelStore = ViewModelStore()
    override val viewModelStore: ViewModelStore = _viewModelStore

    val savedStateRegistryController = SavedStateRegistryController.create(this)
    override val savedStateRegistry: SavedStateRegistry = savedStateRegistryController.savedStateRegistry

    private var initX = 0
    private var initY = 0
    private var touchX = 0f
    private var touchY = 0f
    val buttonSize = 56
    val totalWidthDp = buttonSize + 240 + 12 // + panel size + space between

    @RequiresApi(Build.VERSION_CODES.Q)
    @SuppressLint("MissingPermission")
    override fun onCreate() {
        super.onCreate()

        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        savedStateRegistryController.performRestore(null)

        val prefs = getSharedPreferences("dock_prefs", MODE_PRIVATE)
        val savedX = prefs.getInt("dock_x", 0)
        val savedY = prefs.getInt("dock_y", 0)

        val density = resources.displayMetrics.density
        val buttonSizePx = (72 * density).toInt() // 56px - button, 12px - space

        params = LayoutParams(
            buttonSizePx,
            buttonSizePx,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        ).apply {
            x = savedX
            y = savedY
        }

        composeView = ComposeView(this).apply {
            setViewTreeLifecycleOwner(this@OverlayService)
            setViewTreeViewModelStoreOwner(this@OverlayService)
            setViewTreeSavedStateRegistryOwner(this@OverlayService)

            setContent {
                val isShown = dockParams.isDockShown.collectAsState()

                if (isShown.value) {
                    NothingTheme {
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
                            },
                            {
                                prefs.edit { putInt("dock_x", params.x).putInt("dock_y", params.y) }
                            },
                            onRequestResize = { newWidthPx, newHeightPx ->
                                resizeWindow(newWidthPx, newHeightPx)
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

    private fun resizeWindow(newWidthPx: Int, newHeightPx: Int) {
        val view = composeView ?: return
        val screenWidthPx = resources.displayMetrics.widthPixels
        val screenHeightPx = resources.displayMetrics.heightPixels

        val oldRightEdge = params.x + params.width
        var newX = oldRightEdge - newWidthPx
        if (newX < 0) newX = 0
        if (newX + newWidthPx > screenWidthPx) newX = screenWidthPx - newWidthPx

        val oldVerticalCenter = params.y + params.height / 2
        var newY = oldVerticalCenter - newHeightPx / 2
        if (newY < 0) newY = 0
        if (newY + newHeightPx > screenHeightPx) newY = screenHeightPx - newHeightPx

        params.x = newX
        params.y = newY
        params.width = newWidthPx
        params.height = newHeightPx
        windowManager.updateViewLayout(view, params)
    }

    override fun onDestroy() {
        super.onDestroy()
        composeView?.let {
            windowManager.removeView(it)
        }
    }
}


@SuppressLint("AccessibilityPolicy")
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