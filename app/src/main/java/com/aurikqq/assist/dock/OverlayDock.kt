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
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.aurikqq.assist.General
import com.aurikqq.assist.Root
import com.aurikqq.assist.commands.Notifications
import com.aurikqq.assist.commands.SoundHandler
import com.aurikqq.assist.ui.theme.NothingTheme
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.exp
import kotlin.math.roundToInt
import kotlin.math.sin

@OptIn(ExperimentalMaterial3Api::class)
@RequiresApi(Build.VERSION_CODES.Q)
@RequiresPermission(Manifest.permission.READ_PHONE_STATE)
@Composable
fun Dock(
    context: Context,
    onActionDown: (rawX: Float, rawY: Float) -> Unit,
    onActionMove: (rawX: Float, rawY: Float) -> Unit
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
    val buttonState = DockButtonState()

//    LaunchedEffect(Unit) {
//        val intent = Intent(Settings.Panel.ACTION_INTERNET_CONNECTIVITY).apply {
//            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
//        }
//        context.startActivity(intent)
//    }

    Row(horizontalArrangement = Arrangement.End) {
        if (isDockOpened) {
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

        Spacer(Modifier.size(12.dp))

        Column {
            Spacer(Modifier.height(112.dp))

            DockButton(
                buttonState,
                {
                    scope.launch { buttonState.appear() }
                    isDockOpened = !isDockOpened
                    //isAccessibilityServiceRunning(context)
                },
                onActionDown,
                onActionMove
            )
        }
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

@Composable
fun DockButton(
        state: DockButtonState,
        onClick: (Offset) -> Unit,
        onActionDown: (rawX: Float, rawY: Float) -> Unit,
        onActionMove: (rawX: Float, rawY: Float) -> Unit
    ) {
    var touchX = 0f
    var touchY = 0f
    val clickThreshold = 10

    DottedButton(
        state,
        modifier = Modifier
            .pointerInteropFilter { event ->
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        touchX = event.rawX
                        touchY = event.rawY
                        onActionDown(event.rawX, event.rawY)
                        true
                    }

                    MotionEvent.ACTION_MOVE -> {
                        onActionMove(event.rawX, event.rawY)
                        true
                    }

                    MotionEvent.ACTION_UP -> {
                        val deltaX = abs(event.rawX - touchX)
                        val deltaY = abs(event.rawY - touchY)
                        if (deltaX < clickThreshold && deltaY < clickThreshold) {
                            onClick.invoke(Offset(0))
                        }
                        true
                    }

                    else -> false
                }
            }
    )
}

class DockButtonState() {
    var radiusChange = 0f
    var distanceChange = 0f
    private val anim = Animatable(0f)
    val time get() = anim.value

    val waveSpeed = 400f     // px/сек — скорость распространения импульса
    val frequencyHz = 1f     // частота колебаний в каждой точке
    val decayRate = 1f * time / 3       // скорость затухания (1/сек)
    val amplitude = 1.5f      // максимальное отклонение радиуса

    fun pulse(distance: Float, baseRadius: Float): Float {
        val localTime = time - distance / waveSpeed
        if (localTime < 0f) return baseRadius
        val omega = 2f * PI.toFloat() * frequencyHz
        return baseRadius + amplitude * exp(-decayRate * localTime) * sin(omega * localTime)
    }

    suspend fun appear() {
        anim.snapTo(0f)
        anim.animateTo(
            3f,
            animationSpec = tween(3000, easing = LinearEasing)
        )

        Thread {
            while (time < 3f) {
                distanceChange = when {
                    time < 0.3f -> {
                        val t = time / 0.3f
                        4f * sin(t * PI.toFloat() / 2f)
                    }

                    time < 0.8f -> {
                        val t = (time - 0.3f) / 0.5f
                        4f + (-5f) * (t * t * (3f - 2f * t))
                    }

                    else -> {
                        val t = (time - 0.8f) / 0.2f
                        -1f + sin(t * PI.toFloat() / 2f)
                    }
                }
            }
        }.start()
    }
}

@Composable
fun DottedButton(state: DockButtonState, modifier: Modifier) {
    val white = NothingTheme.colors.primary
    val red = NothingTheme.colors.red
    val background = NothingTheme.colors.background.copy(alpha = 0.4f)

    Canvas(modifier = modifier.size(56.dp)) {
        val center = Offset(size.width / 2f, size.height / 2f)

        drawCircle(
            color = background,
            radius = 24.dp.toPx(),
            center = center
        )

        repeat(16) { index ->
            val angle = Math.toRadians(index * 22.5 - 90)
            val distance = 56f + state.distanceChange
            val x = center.x + cos(angle).toFloat() * distance
            val y = center.y + sin(angle).toFloat() * distance

            drawCircle(
                color = white,
                center = Offset(x, y),
                radius = state.pulse(distance, 7f)
            )
        }

        repeat(8) { index ->
            val angle = Math.toRadians(index * 45.0 - 90)
            val distance = 33f + state.distanceChange
            val x = center.x + cos(angle).toFloat() * distance
            val y = center.y + sin(angle).toFloat() * distance

            val baseRadius = if (index % 2 == 0) 11f else 9f
            drawCircle(
                color = red,
                center = Offset(x, y),
                radius = state.pulse(distance, baseRadius)
            )
        }

        drawCircle(
            color = red,
            center = center,
            radius = state.pulse(0f, 16f)
        )
    }
}

@Composable @Preview
fun DottedButtonPreview() {
    NothingTheme(darkTheme = true) {
        DottedButton(DockButtonState(), Modifier.size(56.dp))
    }
}



/* it's buggy, but fun, i like it

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