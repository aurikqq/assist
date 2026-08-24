package com.aurikqq.assist.dock

import android.Manifest
import android.app.Notification
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.provider.Settings
import android.text.TextUtils
import android.util.Log
import android.view.MotionEvent
import androidx.annotation.RequiresApi
import androidx.annotation.RequiresPermission
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.AnimationVector2D
import androidx.compose.animation.core.EaseInQuart
import androidx.compose.animation.core.EaseOutQuart
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Adb
import androidx.compose.material.icons.filled.Autorenew
import androidx.compose.material.icons.filled.BatterySaver
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.Brightness4
import androidx.compose.material.icons.filled.BrightnessAuto
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Nfc
import androidx.compose.material.icons.filled.SignalCellularAlt
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.filled.WifiTethering
import androidx.compose.material3.Card
import androidx.compose.material3.CardColors
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderColors
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.aurikqq.assist.General
import com.aurikqq.assist.Root
import com.aurikqq.assist.commands.Notifications
import com.aurikqq.assist.commands.SoundHandler
import com.aurikqq.assist.commands.isMediaPlaying
import com.aurikqq.assist.ui.theme.NothingTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.exp
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.random.Random
import kotlin.time.Duration.Companion.milliseconds

@OptIn(ExperimentalMaterial3Api::class)
@RequiresApi(Build.VERSION_CODES.Q)
@RequiresPermission(Manifest.permission.READ_PHONE_STATE)
@Composable
fun Dock(
    context: Context,
    onActionDown: (rawX: Float, rawY: Float) -> Unit,
    onActionMove: (rawX: Float, rawY: Float) -> Unit,
    onActionUp: () -> Unit = {},
    onRequestResize: (widthPx: Int, heightPx: Int) -> Unit
) {
    val viewModel = viewModel<ConnectionsViewModel> {
        ConnectionsViewModel(Connections((context)))
    }
    val scope = rememberCoroutineScope()

    var isDockOpened by remember { mutableStateOf(false) }
    val connections = Connections(context)
    val pkgManager = context.packageManager
    val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    val soundHandler = SoundHandler(context)
    val general = General()
    val buttonState = remember { DockButtonState() }

    val density = LocalDensity.current
    val buttonSizePx = with(LocalDensity.current) { 56.dp.toPx() }
    val width = with (LocalDensity.current) { LocalConfiguration.current.screenWidthDp.dp.toPx() }
    var buttonOffsetX by remember { mutableFloatStateOf(width - buttonSizePx - 16f) }
    var buttonOffsetY by remember { mutableFloatStateOf(300f) }
    var currentPanelX by remember { mutableFloatStateOf(0f) }
    val buttonWindowSizePx = with(density) { 72.dp.roundToPx() } // должно совпадать с OverlayService
    val panelWidthPx = with(density) { 240.dp.roundToPx() }
    val margin = with(density) { 12.dp.roundToPx() }
    val panelHeightPx = with(density) { 320.dp.roundToPx() } // с запасом под содержимое панели

//    LaunchedEffect(Unit) {
//        val intent = Intent(Settings.Panel.ACTION_INTERNET_CONNECTIVITY).apply {
//            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
//        }
//        context.startActivity(intent)
//    }

    fun openDock() {
        onRequestResize(
            buttonWindowSizePx + margin + panelWidthPx,
            maxOf(buttonWindowSizePx, panelHeightPx)
        )
        isDockOpened = true
    }

    fun closeDock() {
        isDockOpened = false
        scope.launch {
            delay(220.milliseconds)
            onRequestResize(buttonWindowSizePx, buttonWindowSizePx)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        AnimatedVisibility(
            visible = isDockOpened,
            enter = fadeIn(tween(200)) + slideInHorizontally(tween(250)) { it / 3 } ,
            exit = fadeOut(tween(150)) + slideOutHorizontally(tween(200)) { it / 3 },
            modifier = Modifier
                .align(Alignment.CenterStart)
        ) {
            val isConnectedWifi by viewModel.isConnectedToWifi.collectAsState()
            val isConnectedCellular by viewModel.isConnectedToCellular.collectAsState()

            var isBluetoothEnabled by remember { mutableStateOf(connections.isBluetoothEnabled(context)) }
            var isLocationEnabled by remember { mutableStateOf(connections.isLocationEnabled(context)) }
            var isHotspotEnabled by remember { mutableStateOf(connections.isHotspotEnabled(context)) }
            var isNfcEnabled by remember { mutableStateOf(connections.isNfcEnabled(context)) }
            var isAdbEnabled by remember { mutableStateOf(connections.isAdbEnabled(context)) }
            var isSaverEnabled by remember { mutableStateOf(connections.isSaverEnabled(context)) }
            var isAutorotateEnabled by remember { mutableStateOf(connections.isAutorotateEnabled(context)) }
            var soundMode by remember { mutableStateOf(connections.soundMode(context)) }

            var volumeLevel by remember { mutableFloatStateOf(soundHandler.getMediaVolume().toFloat()) }
            var brightness by remember { mutableFloatStateOf(general.getBrightness(context).toFloat()) }
            var autoBrightnessButtonIcon by remember { mutableStateOf(
                if (general.isAutoBrightnessEnabled(context)) Icons.Default.BrightnessAuto
                else Icons.Default.Brightness4
            ) }

            Column {
                Card(
                    colors = CardColors(
                        containerColor = NothingTheme.colors.background,
                        contentColor = NothingTheme.colors.primary,
                        disabledContainerColor = NothingTheme.colors.secondary,
                        disabledContentColor = NothingTheme.colors.secondary
                    ),
                    modifier = Modifier
                        .size(240.dp, 48.dp)
                ) {
                    Row(Modifier
                        .fillMaxSize()
                        .padding(start = 0.dp, top = 8.dp, end = 8.dp, bottom = 8.dp)) {
                        IconButton(onClick = {
                            general.switchAutoBrightness(context)
                            autoBrightnessButtonIcon = if (general.isAutoBrightnessEnabled(context)) Icons.Default.BrightnessAuto else Icons.Default.Brightness4
                        }
                        ) {
                            Icon(
                                autoBrightnessButtonIcon,
                                null,
                                tint = NothingTheme.colors.primary
                            )
                        }

                        Slider(
                            value = brightness,
                            onValueChange = {
                                general.setBrightness(context, it.roundToInt(), true)
                                brightness = it
                            },
                            onValueChangeFinished = { autoBrightnessButtonIcon = Icons.Default.Brightness4 },
                            valueRange = 0f..255f,
                            colors = SliderColors(
                                thumbColor = NothingTheme.colors.primary,
                                activeTrackColor = NothingTheme.colors.primary,
                                activeTickColor = NothingTheme.colors.primary,
                                inactiveTrackColor = NothingTheme.colors.surfaceHigh,
                                inactiveTickColor = NothingTheme.colors.surfaceHigh,
                                disabledThumbColor = NothingTheme.colors.secondary,
                                disabledActiveTrackColor = NothingTheme.colors.secondary,
                                disabledActiveTickColor = NothingTheme.colors.secondary,
                                disabledInactiveTrackColor = NothingTheme.colors.surfaceHigh,
                                disabledInactiveTickColor = NothingTheme.colors.surfaceHigh
                            ),
                            modifier = Modifier.height(32.dp)
                        )
                    }
                }

                Spacer(Modifier.size(8.dp))

                Card(
                    colors = CardColors(
                        containerColor = NothingTheme.colors.background,
                        contentColor = NothingTheme.colors.primary,
                        disabledContainerColor = NothingTheme.colors.secondary,
                        disabledContentColor = NothingTheme.colors.secondary
                    ),
                    modifier = Modifier.size(240.dp, 48.dp)
                ) {
                    Box(Modifier
                        .fillMaxSize()
                        .padding(8.dp)) {
                        Slider(
                            value = volumeLevel,
                            onValueChange = {
                                soundHandler.setMediaVolume(it.roundToInt())
                                volumeLevel = it
                            },
                            steps = 16,
                            valueRange = 0f..16f,
                            colors = SliderColors(
                                thumbColor = NothingTheme.colors.primary,
                                activeTrackColor = NothingTheme.colors.primary,
                                activeTickColor = NothingTheme.colors.primary,
                                inactiveTrackColor = NothingTheme.colors.surfaceHigh,
                                inactiveTickColor = NothingTheme.colors.surfaceHigh,
                                disabledThumbColor = NothingTheme.colors.secondary,
                                disabledActiveTrackColor = NothingTheme.colors.secondary,
                                disabledActiveTickColor = NothingTheme.colors.secondary,
                                disabledInactiveTrackColor = NothingTheme.colors.surfaceHigh,
                                disabledInactiveTickColor = NothingTheme.colors.surfaceHigh
                            ),
                            modifier = Modifier.height(32.dp)
                        )
                    }
                }

                Spacer(Modifier.size(8.dp))

                Card(
                    colors = CardColors(
                        containerColor = NothingTheme.colors.background,
                        contentColor = NothingTheme.colors.primary,
                        disabledContainerColor = NothingTheme.colors.secondary,
                        disabledContentColor = NothingTheme.colors.secondary
                    ),
                    modifier = Modifier.width(240.dp)
                ) {
                    LazyRow(contentPadding = PaddingValues(horizontal = 4.dp)) {
                        item {
                            IconButton(
                                onClick = {
                                    if (isConnectedWifi) {
                                        Root.execute("svc wifi disable")
                                    } else {
                                        Root.execute("svc wifi enable")
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Wifi,
                                    contentDescription = null,
                                    tint = if (isConnectedWifi) NothingTheme.colors.primary
                                    else NothingTheme.colors.secondary
                                )
                            }
                        }

                        item {
                            IconButton(
                                onClick = {
                                    if (isConnectedCellular) {
                                        Root.execute("svc data disable")
                                    } else {
                                        Root.execute("svc data enable")
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.SignalCellularAlt,
                                    contentDescription = null,
                                    tint = if (isConnectedCellular) NothingTheme.colors.primary
                                    else NothingTheme.colors.secondary
                                )
                            }
                        }

                        item {
                            IconButton(
                                onClick = {
                                    if (isBluetoothEnabled) {
                                        Root.execute("svc bluetooth disable")
                                        isBluetoothEnabled = false
                                    } else {
                                        Root.execute("svc bluetooth enable")
                                        isBluetoothEnabled = true
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Bluetooth,
                                    contentDescription = null,
                                    tint = if (isBluetoothEnabled) NothingTheme.colors.primary
                                    else NothingTheme.colors.secondary
                                )
                            }
                        }

                        item {
                            IconButton(
                                onClick = {
                                    if (isLocationEnabled) {
                                        Root.execute("settings put secure location_mode 0")
                                        isLocationEnabled = false
                                    } else {
                                        Root.execute("settings put secure location_mode 3")
                                        isLocationEnabled = true
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.LocationOn,
                                    contentDescription = null,
                                    tint = if (isLocationEnabled) NothingTheme.colors.primary
                                    else NothingTheme.colors.secondary
                                )
                            }
                        }

                        item {
                            IconButton(
                                onClick = {
                                    if (isHotspotEnabled) {
                                        Root.execute("cmd connectivity tether stop-tethering")
                                        isHotspotEnabled = false
                                    } else {
                                        Root.execute("cmd connectivity tether start-tethering wifi")
                                        isHotspotEnabled = true
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.WifiTethering,
                                    contentDescription = null,
                                    tint = if (isHotspotEnabled) NothingTheme.colors.primary
                                    else NothingTheme.colors.secondary
                                )
                            }
                        }

                        item {
                            IconButton(
                                onClick = {
                                    if (isAdbEnabled) {
                                        Root.execute("settings put global adb_wifi_enabled 0")
                                        isAdbEnabled = false
                                    } else {
                                        Root.execute("settings put global adb_wifi_enabled 1")
                                        isAdbEnabled = true
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Adb,
                                    contentDescription = null,
                                    tint = if (isAdbEnabled) NothingTheme.colors.primary
                                    else NothingTheme.colors.secondary
                                )
                            }
                        }

                        item {
                            IconButton(
                                onClick = {
                                    if (isNfcEnabled) {
                                        Root.execute("svc nfc disable")
                                        isNfcEnabled = false
                                    } else {
                                        Root.execute("svc nfc enable")
                                        isNfcEnabled = true
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Nfc,
                                    contentDescription = null,
                                    tint = if (isNfcEnabled) NothingTheme.colors.primary
                                    else NothingTheme.colors.secondary
                                )
                            }
                        }

                        item {
                            IconButton(
                                onClick = {
                                    if (isSaverEnabled) {
                                        Root.execute("cmd power set-mode 0")
                                        isSaverEnabled = false
                                    } else {
                                        Root.execute("cmd power set-mode 1")
                                        isSaverEnabled = true
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.BatterySaver,
                                    contentDescription = null,
                                    tint = if (isSaverEnabled) NothingTheme.colors.primary
                                    else NothingTheme.colors.secondary
                                )
                            }
                        }

                        item {
                            IconButton(
                                onClick = {
                                    if (isAutorotateEnabled) {
                                        Root.execute("settings put system accelerometer_rotation 0")
                                    } else {
                                        Root.execute("settings put system user_rotation 0 && settings put system accelerometer_rotation 1")
                                        isAutorotateEnabled = true
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Autorenew,
                                    contentDescription = null,
                                    tint = if (isAutorotateEnabled) NothingTheme.colors.primary
                                    else NothingTheme.colors.secondary
                                )
                            }
                        }

                        item {
                            IconButton(
                                onClick = {
                                    when (soundMode) {
                                        SoundModes.NORMAL -> Root.executeSingle("cmd audio set-ringer-mode SILENT")
                                        SoundModes.VIBRATE -> Root.executeSingle("cmd audio set-ringer-mode NORMAL")
                                        SoundModes.SILENT -> Root.executeSingle("cmd audio set-ringer-mode VIBRATE")
                                    }
                                    soundMode = connections.soundMode(context)
                                }
                            ) {
                                Icon(
                                    imageVector = when (soundMode) {
                                        SoundModes.NORMAL -> Icons.AutoMirrored.Filled.VolumeUp
                                        SoundModes.VIBRATE -> Icons.Default.Vibration
                                        SoundModes.SILENT -> Icons.AutoMirrored.Filled.VolumeOff
                                    },
                                    contentDescription = null,
                                    tint = NothingTheme.colors.primary
                                )
                            }
                        }
                    }
                }

                Spacer(Modifier.size(8.dp))

                Card(
                    colors = CardColors(
                        containerColor = NothingTheme.colors.background,
                        contentColor = NothingTheme.colors.primary,
                        disabledContainerColor = NothingTheme.colors.secondary,
                        disabledContentColor = NothingTheme.colors.secondary
                    ),
                    modifier = Modifier
                        .width(240.dp)
                        .heightIn(max = 200.dp)
                ) {
                    LazyColumn(contentPadding = PaddingValues(start = 8.dp, top = 12.dp, end = 8.dp, bottom = 4.dp)) {
                        items(Notifications.notifications) { statusBarNotification ->
                            val notification = statusBarNotification.notification
                            val text = notification.extras.getString(Notification.EXTRA_TEXT)
                            val title = notification.extras.getString(Notification.EXTRA_TITLE)

                            if (!text.isNullOrEmpty() || !title.isNullOrEmpty()) {
                                Card(
                                    colors = CardColors(
                                        containerColor = NothingTheme.colors.surfaceHigh,
                                        contentColor = NothingTheme.colors.primary,
                                        disabledContainerColor = NothingTheme.colors.secondary,
                                        disabledContentColor = NothingTheme.colors.secondary
                                    ),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .combinedClickable(
                                            enabled = true,
                                            onClick = { notification.contentIntent.send() },
                                            onLongClick = {
                                                notificationManager.cancel(
                                                    statusBarNotification.tag,
                                                    statusBarNotification.id
                                                )
                                            },
                                        )
                                ) {
                                    Column(Modifier.padding(4.dp), verticalArrangement = Arrangement.Center) {
                                        Text(
                                            "${
                                                pkgManager.getApplicationLabel(
                                                    pkgManager.getApplicationInfo(
                                                        statusBarNotification.packageName,
                                                        0
                                                    )
                                                )
                                            }",
                                            color = NothingTheme.colors.secondary,
                                            fontSize = 10.sp
                                        )
                                        if (!title.isNullOrEmpty()) {
                                            Text(
                                                title,
                                                fontWeight = FontWeight.SemiBold,
                                                fontSize = 14.sp
                                            )
                                            Spacer(Modifier.size(4.dp))
                                        }
                                        if (!text.isNullOrEmpty()) {
                                            Text(text, fontSize = 13.sp)
                                        }
                                    }
                                }

                                Spacer(Modifier.size(8.dp))
                            }
                        }
                    }
                }
            }
        }

        DockButton(
            buttonState,
            { isMediaPlaying(context) },
            width,
            {
                scope.launch { buttonState.play(ButtonAnimation.Appear) }
                if (isDockOpened) closeDock() else openDock()
                //isAccessibilityServiceRunning(context)
            },
            onActionDown,
            onActionMove,
            onActionUp,
            Modifier.align(Alignment.CenterEnd)
        )
    }
}

fun isAccessibilityServiceRunning(context: Context) : Boolean {
    var accessibilityEnabled = 0
    val service: String = context.packageName + "/" + EssentialKeyService::class.java.canonicalName

    try {
        accessibilityEnabled = Settings.Secure.getInt(
            context.applicationContext.contentResolver,
            Settings.Secure.ACCESSIBILITY_ENABLED
        )
        Log.v("Accessibility", "accessibilityEnabled = $accessibilityEnabled")
    } catch (e: Settings.SettingNotFoundException) {
        Log.e("Accessibility", "default accessibility not found: " + e.message)
    }

    val stringColonSplitter = TextUtils.SimpleStringSplitter(':')
    if (accessibilityEnabled == 1) {
        Log.v("Accessibility", "accessibility is enabled")
        val settingValue: String = Settings.Secure.getString(
            context.applicationContext.contentResolver,
            Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        )
        stringColonSplitter.setString(settingValue)
        while (stringColonSplitter.hasNext()) {
            val accessibilityService = stringColonSplitter.next()
            Log.v("Accessibility", "AccessibilityService :: $accessibilityService $service")
            if (accessibilityService.equals(service, ignoreCase = true)) {
                Log.v("Accessibility", "accessibility is on")
                return true
            }
        }
    }
    else {
        Log.v("Accessibility", "accessibility is disabled")
    }
    return false
}

enum class DotRing { OUTER_WHITE, INNER_RED, CENTER }
data class Dot(
    val ring: DotRing,
    val index: Int,
    val angleDeg: Float,
    val baseDistance: Float,
    val baseRadius: Float
)

data class DotKey(val ring: DotRing, val index: Int)

fun buildDots(): List<Dot> = buildList {
    repeat(16) { i -> add(Dot(
        DotRing.OUTER_WHITE, i, i * 22.5f - 90f, 56f, 7f)) }
    repeat(8) { i ->
        val radius = if (i % 2 == 0) 11f else 9f
        add(Dot(DotRing.INNER_RED, i, i * 45f - 90f, 33f, radius))
    }
    add(Dot(DotRing.CENTER, 0, 0f, 0f, 16f))
}

fun Dot.polarOffset(distance: Float = baseDistance): Offset {
    val rad = Math.toRadians(angleDeg.toDouble())
    return Offset(cos(rad).toFloat() * distance, sin(rad).toFloat() * distance)
}

fun computeDockOpenPositions(
    buttonX: Float,
    panelWidthPx: Float,
    marginPx: Float
) : Pair<Float, Float> {
    val desiredPanelX = buttonX - panelWidthPx - marginPx
    return if (desiredPanelX < 0f) {
        val shift = -desiredPanelX
        (buttonX + shift) to 0f
    } else {
        buttonX to desiredPanelX
    }
}

@Composable
fun DockButton(
        state: DockButtonState,
        isMusicPlaying: () -> Boolean,
        screenWidthPx: Float,
        onClick: (Offset) -> Unit,
        onActionDown: (rawX: Float, rawY: Float) -> Unit,
        onActionMove: (rawX: Float, rawY: Float) -> Unit,
        onActionUp: () -> Unit = {},
        modifier: Modifier
) {
    val clickThreshold = 10
    var isDragging = false

    val buttonSizePx = with(LocalDensity.current) { 56.dp.toPx() }

    val width = with (LocalDensity.current) { LocalConfiguration.current.screenWidthDp.dp.toPx() }
    val height = with (LocalDensity.current) { LocalConfiguration.current.screenHeightDp.dp.toPx() }

    var buttonOffsetX by remember { mutableFloatStateOf(width - buttonSizePx - 16f) }
    var buttonOffsetY by remember { mutableFloatStateOf(300f) }

    var dragStartTouchX = 0f
    var dragStartTouchY = 0f
    var dragStartButtonX = 0f
    var dragStartButtonY = 0f

    val scope = rememberCoroutineScope()
    val dots = remember { buildDots() }

    LaunchedEffect(Unit) {
        state.startIdleBreathing(scope, dots)
        state.startMusicReactiveLoop(scope, isMusicPlaying)
        state.runFrameClock()
    }

    LaunchedEffect(Unit) {
        state.playWindAppear(dots, ScreenEdge.RIGHT, screenWidthPx)
        state.play(ButtonAnimation.Appear)
    }

    DisposableEffect(Unit) {
        onDispose { state.stopBackgroundLoops() }
    }

    DottedButton(
        dots,
        state,
        modifier = modifier
            .pointerInteropFilter { event ->
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        dragStartTouchX = event.rawX
                        dragStartTouchY = event.rawY
                        dragStartButtonX = buttonOffsetX
                        dragStartButtonY = buttonOffsetY
                        scope.launch { state.onPressStart() }
                        onActionDown(event.rawX, event.rawY)
                        true
                    }

                    MotionEvent.ACTION_MOVE -> {
                        val delta = Offset(event.rawX - dragStartTouchX, event.rawY - dragStartTouchY)
                        buttonOffsetX = (dragStartButtonX + delta.x).coerceIn(0f, width - buttonSizePx)
                        buttonOffsetY = (dragStartButtonY + delta.y).coerceIn(0f, height - buttonSizePx)
                        scope.launch {
                            if (!isDragging) {
                                isDragging = true
                                state.onPressEnd()
                                state.onDragStart()
                            }
                            state.applyDragDelta(delta)
                        }
                        onActionMove(event.rawX, event.rawY)
                        true
                    }

                    MotionEvent.ACTION_UP -> {
                        isDragging = false

                        val deltaX = abs(event.rawX - dragStartTouchX)
                        val deltaY = abs(event.rawY - dragStartTouchY)
                        if (deltaX < clickThreshold && deltaY < clickThreshold) {
                            onClick.invoke(Offset(0))
                        }
                        //scope.launch { state.play(ButtonAnimation.Appear) }
                        scope.launch {
                            state.onPressEnd()
                            state.onDragEnd()
                        }
                        onActionUp()
                        true
                    }

                    MotionEvent.ACTION_CANCEL -> {
                        isDragging = false
                        scope.launch {
                            state.onPressEnd()
                            state.onDragEnd()
                        }
                        onActionUp()
                        true
                    }

                    else -> false
                }
            }
    )
}

data class MusicBump(
    val centerAngleDeg: Float,
    val arcWidthDeg: Float,
    val startTime: Long,
    val amplitude: Float
)

data class Pulse(
    val waveSpeed: Float = 400f,
    val frequencyHz: Float = 1f,
    val decayRate: Float = 1.5f,
    val amplitude: Float = 1.5f
) {
    fun radiusAt(time: Float, distance: Float, baseRadius: Float): Float {
        val localTime = time - distance / waveSpeed
        if (localTime < 0f) return baseRadius
        val omega = 2f * PI.toFloat() * frequencyHz
        return baseRadius + amplitude * exp(-decayRate * localTime) * sin(omega * localTime)
    }
}

enum class ScreenEdge { LEFT, RIGHT }

fun getDistanceOffset(time: Float): Float = when {
    time < 0.3f -> {
        val t = time / 0.3f
        4f * sin(t * PI.toFloat() / 2f)
    }

    time < 0.8f -> {
        val t = (time - 0.3f) / 0.5f
        4f + (-5f) * (t * t * (3f - 2f * t))
    }

    time < 1f -> {
        val t = (time - 0.8f) / 0.2f
        -1f + sin(t * PI.toFloat() / 2f)
    }

    else -> { 0f }
}

sealed interface ButtonAnimation {
    val durationMs: Int
    fun distanceOffset(time: Float): Float
    fun pulse(): Pulse

    object Appear : ButtonAnimation {
        override val durationMs = 3000
        override fun distanceOffset(time: Float) = getDistanceOffset(time)
        override fun pulse() = Pulse(waveSpeed = 300f, decayRate = 4f, amplitude = 2f)
    }
}

fun DrawScope.drawStreakDot(
    center: Offset,
    baseRadius: Float,
    color: Color,
    alpha: Float,
    velocity: Offset
) {
    val speed = velocity.getDistance()
    if (speed < 60f || baseRadius <= 0f) {
        drawCircle(color = color, radius = baseRadius.coerceAtLeast(0f), center = center, alpha = alpha)
        return
    }

    val stretch = (speed * 0.01f).coerceIn(0f, 160f)
    val angleDeg = Math.toDegrees(atan2(velocity.y, velocity. x).toDouble()).toFloat()

    rotate(degrees = angleDeg, pivot = center) {
        drawOval(
            color = color,
            alpha = alpha,
            topLeft = Offset(center.x - baseRadius - stretch, center.y - baseRadius),
            size = Size(baseRadius * 2 + stretch, baseRadius * 2)
        )
    }
}

class DockButtonState {
    private val anim = Animatable(0f)
    var current by mutableStateOf<ButtonAnimation?>(null)
        private set
    val time get() = anim.value
    var frameTick by mutableStateOf(0L)
        private set

    suspend fun runFrameClock() {
        while(true) {
            withFrameNanos { frameTimeNanos ->
                frameTick = frameTimeNanos
            }
        }
    }

    val pulse = Pulse()
    val pressAmount = Animatable(0f)
    val breathingScale = mutableStateMapOf<DotKey, Animatable<Float, AnimationVector1D>>()
    private var breathingJob: Job? = null
    private val musicBumpsList = mutableListOf<MusicBump>()
    private var musicJob: Job? = null
    val dragLag = Animatable(Offset.Zero, Offset.VectorConverter)

    val windOffset = mutableStateMapOf<DotKey, Animatable<Offset, AnimationVector2D>>()
    val windAlpha = mutableStateMapOf<DotKey, Animatable<Float, AnimationVector1D>>()
    var isContentVisible by mutableStateOf(true)
        private set

    val backgroundAlpha = Animatable(1f)

    suspend fun onDragStart() {
        backgroundAlpha.animateTo(0f, tween(150, easing = FastOutSlowInEasing))
    }
    suspend fun onDragEnd() {
        backgroundAlpha.animateTo(1f, tween(250, easing = FastOutSlowInEasing))
    }

    private fun windPos(key: DotKey) =
        windOffset.getOrPut(key) { Animatable(Offset.Zero, Offset.VectorConverter) }
    private fun windA(key: DotKey) =
        windAlpha.getOrPut(key) { Animatable(1f) }

    suspend fun playWindAppear(
        dots: List<Dot>,
        edge: ScreenEdge,
        travelDistancePx: Float
    ) = coroutineScope {
        isContentVisible = true
        backgroundAlpha.snapTo(0f)
        val baseDirection = if (edge == ScreenEdge.RIGHT) 1f else -1f

        val jobs = dots.map { dot ->
            launch {
                val key = DotKey(dot.ring, dot.index)
                val jitterDeg = Random.nextFloat() * 24f - 12f // may be deleted
                val directionRad = Math.toRadians(jitterDeg.toDouble())
                val distance = travelDistancePx * (0.85f + 0.3 * Random.nextFloat())
                val startOffset = Offset(
                    x = (baseDirection * distance * cos(directionRad)).toFloat(),
                    y = (distance * sin(directionRad)).toFloat()
                )

                val pos = windPos(key)
                val alpha = windA(key)
                pos.snapTo(startOffset)
                alpha.snapTo(0f)

                delay(Random.nextLong(0, 220).milliseconds)

                launch { alpha.animateTo(1f, tween(160)) }
                pos.animateTo(
                    Offset.Zero,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioLowBouncy,
                        stiffness = Spring.StiffnessLow
                    )
                )
            }
        }

        launch {
            delay(200.milliseconds)
            backgroundAlpha.animateTo(1f, tween(400, easing = FastOutSlowInEasing))
        }

        jobs.joinAll()
    }

    suspend fun playWindDisappear(
        dots: List<Dot>,
        edge: ScreenEdge,
        travelDistancePx: Float
    ) = coroutineScope {
        isContentVisible = true
        launch { backgroundAlpha.animateTo(0f, tween(300, easing = FastOutSlowInEasing)) }
        val baseDirection = if (edge == ScreenEdge.RIGHT) 1f else -1f

        val jobs = dots.map { dot ->
            launch {
                val key = DotKey(dot.ring, dot.index)
                val jitterDeg = Random.nextFloat() * 20f - 10f // may be deleted
                val directionRad = Math.toRadians(jitterDeg.toDouble())
                val distance = travelDistancePx * (0.9f + 0.25 * Random.nextFloat())
                val endOffset = Offset(
                    x = (baseDirection * distance * cos(directionRad)).toFloat(),
                    y = (distance * sin(directionRad)).toFloat()
                )

                val pos = windPos(key)
                val alpha = windA(key)

                delay(Random.nextLong(0, 140).milliseconds)

                launch {
                    pos.animateTo(
                        endOffset,
                        tween(
                            durationMillis = Random.nextInt(480, 720),
                            easing = FastOutLinearInEasing
                        )
                    )
                }
                alpha.animateTo(0f, tween(500))
            }
        }
        jobs.joinAll()
        isContentVisible = false
    }

    fun startMusicReactiveLoop(scope: CoroutineScope, isMusicPlaying: () -> Boolean) {
        musicJob?.cancel()
        musicJob = scope.launch {
            launch {
                while (isActive) {
                    if (withContext(Dispatchers.Default) { isMusicPlaying() }) {
                        val bump = MusicBump(
                            centerAngleDeg = Random.nextFloat() * 360f,
                            arcWidthDeg = Random.nextFloat() * 40f + 40f,
                            startTime = System.currentTimeMillis(),
                            amplitude = Random.nextFloat() * 3f + 2f
                        )
                        musicBumpsList.add(bump)
                        musicBumpsList.removeAll { System.currentTimeMillis() - it.startTime > 800 }
                        if (musicBumpsList.size > 12) {
                            musicBumpsList.subList(0, musicBumpsList.size - 12).clear()
                        }
                        delay(Random.nextLong(360, 600).milliseconds)
                    } else {
                        delay(300.milliseconds)

                    }
                }
            }
        }
    }

    fun musicContribution(dot: Dot, nowMs: Long): Float {
        var total = 0f
        for (bump in musicBumpsList) {
            val age = (nowMs - bump.startTime) / 1000f
            if (age !in 0f..0.8f) continue
            val difference = abs(((dot.angleDeg) - bump.centerAngleDeg + 540f) % 360f) - 180f
            if (difference > bump.arcWidthDeg) continue
            val angularFalloff = cos((difference / bump.arcWidthDeg) * (PI / 2)).toFloat()
            val decay = exp(-6f * age)
            val ringScale = if (dot.ring == DotRing.INNER_RED) 0.5f else 1f
            total += bump.amplitude * angularFalloff * decay * ringScale
        }
        return total
    }

    fun startIdleBreathing(scope: CoroutineScope, dots: List<Dot>) {
        breathingJob?.cancel()
        breathingJob = scope.launch {
            while (isActive) {
                delay(Random.nextLong(2500, 5000).milliseconds)
                val picks = dots.filter { it.ring != DotRing.CENTER }
                    .shuffled().take(Random.nextInt(3, 6))
                picks.forEach { dot ->
                    launch {
                        val key = DotKey(dot.ring, dot.index)
                        val anim = breathingScale.getOrPut(DotKey(dot.ring, dot.index)) { Animatable(1f) }
                        anim.animateTo(1.3f, tween(1000, easing = EaseOutQuart))
                        anim.animateTo(1f, tween(1000, easing = EaseInQuart))
                    }
                }
            }
        }
    }

    suspend fun applyDragDelta(delta: Offset) {
        val lagFactor = 0.5f
        val maxLag = 14f

        val proposed = dragLag.value - delta * lagFactor
        val clamped = if (proposed.getDistance() > maxLag) {
            proposed * (maxLag / proposed.getDistance())
        }
        else { proposed }

        dragLag.snapTo(clamped)
        dragLag.animateTo(
            Offset.Zero,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioLowBouncy,
                stiffness = Spring.StiffnessLow
            )
        )
    }

    suspend fun onPressStart() {
        pressAmount.animateTo(
            1f, tween(200, easing = FastOutSlowInEasing))
    }

    suspend fun onPressEnd() {
        pressAmount.animateTo(
            0f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioHighBouncy,
                stiffness = Spring.StiffnessLow
            )
        )
    }
    suspend fun play(animation: ButtonAnimation) {
        current = animation

        anim.snapTo(0f)
        anim.animateTo(
            animation.durationMs / 1000f,
            animationSpec = tween(animation.durationMs, easing = LinearEasing)
        )
    }

    fun distanceOffset(): Float = getDistanceOffset(time)

    fun stopBackgroundLoops() {
        breathingJob?.cancel()
        musicJob?.cancel()
    }
}

@Composable
fun DottedButton(dots: List<Dot>, state: DockButtonState, modifier: Modifier) {
    val white = NothingTheme.colors.primary
    val red = NothingTheme.colors.red
    val background = NothingTheme.colors.background.copy(alpha = 0.4f)

    if (!state.isContentVisible) return

    Canvas(modifier = modifier.size(56.dp)) {
        val center = Offset(size.width / 2f, size.height / 2f)
        val now = System.currentTimeMillis()

        val tick = state.frameTick

        drawCircle(
            color = background,
            radius = 24.dp.toPx(),
            center = center,
            alpha = state.backgroundAlpha.value
        )

        dots.forEach { dot ->
            val key = DotKey(dot.ring, dot.index)

            val pressDistanceScale = 1f - 0.3f * state.pressAmount.value
            val pressRadiusScale = 1f - 0.15f * state.pressAmount.value
            val breathScale = state.breathingScale[key]?.value ?: 1f
            val musicDelta = state.musicContribution(dot, now)
            val ringLagScale = when (dot.ring) {
                DotRing.OUTER_WHITE -> 0.15f; DotRing.INNER_RED -> 0.125f; DotRing.CENTER -> 0.1f
            }

            val windOffset = state.windOffset[key]?.value ?: Offset.Zero
            val windVelocity = state.windOffset[key]?.velocity ?: Offset.Zero
            val windAlpha = state.windAlpha[key]?.value ?: 1f

            val distance = dot.baseDistance * pressDistanceScale
            val position = center + dot.polarOffset(distance) + state.dragLag.value * ringLagScale + windOffset
            val radius = (dot.baseRadius * pressRadiusScale * breathScale) +
                state.pulse.radiusAt(state.time, distance, 0f) + musicDelta

            drawStreakDot(
                center = position,
                baseRadius = radius.coerceAtLeast(0f),
                color = if (dot.ring != DotRing.OUTER_WHITE) red else white,
                alpha = windAlpha,
                velocity = windVelocity
            )
        }
    }
}

@Composable @Preview
fun DottedButtonPreview() {
    NothingTheme(darkTheme = true) {
        DottedButton(buildDots(), DockButtonState(), Modifier.size(56.dp))
    }
}



/* it's buggy but fun, i like it

fun overshoot(t: Float, tension: Float = 2f): Float {
    val x = t - 1f
    return x * x * ((tension + 1f) * x + tension) + 1f
}

val move = when {
    time < 0.3f -> {
        val t = time / 0.3f
        4f * overshoot(t, 1.2f)
    }

    time < 0.8f -> {
        val t = (time - 0.3f) / 0.5f
        4f + (-5f) * t
    }

    else -> {
        val t = (time - 0.8f) / 0.2f
        -1f + 1f * (1f - (1f - t).pow(2))
    }
}
*/