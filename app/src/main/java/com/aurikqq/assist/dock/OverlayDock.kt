package com.aurikqq.assist.dock

import android.app.Notification
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import android.text.TextUtils
import android.util.Log
import android.view.MotionEvent
import androidx.annotation.RequiresApi
import androidx.annotation.RequiresPermission
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Adb
import androidx.compose.material.icons.filled.Autorenew
import androidx.compose.material.icons.filled.BatterySaver
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Nfc
import androidx.compose.material.icons.filled.SignalCellularAlt
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.filled.WifiTethering
import androidx.compose.material3.Card
import androidx.compose.material3.CardColors
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.aurikqq.assist.Root
import com.aurikqq.assist.commands.Notifications
import com.aurikqq.assist.commands.SoundHandler
import kotlin.math.abs
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@RequiresApi(Build.VERSION_CODES.Q)
@RequiresPermission(android.Manifest.permission.READ_PHONE_STATE)
@Composable
fun Dock(
    context: Context,
    onActionDown: (rawX: Float, rawY: Float) -> Unit,
    onActionMove: (rawX: Float, rawY: Float) -> Unit
) {
    val viewModel = viewModel<ConnectionsViewModel> {
        ConnectionsViewModel(Connections((context)))
    }

    var isDockOpened by remember { mutableStateOf(false) }
    val connections = Connections(context)
    val pkgManager = context.packageManager
    val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    val soundHandler = SoundHandler(context)

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

            Column {
                Card(Modifier.size(240.dp, 48.dp)) {
                    Box(Modifier.fillMaxSize().padding(8.dp)) {
                        Slider(
                            value = volumeLevel,
                            onValueChange = {
                                soundHandler.setMediaVolume(it.roundToInt())
                                volumeLevel = it
                            },
                            steps = 16,
                            valueRange = 0f..16f,
                            modifier = Modifier.height(32.dp)
                        )
                    }
                }

                Spacer(Modifier.size(8.dp))

                Card(Modifier.width(240.dp)) {
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
                                    tint = if (isConnectedWifi) MaterialTheme.colorScheme.onSurface
                                    else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.25f)
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
                                    tint = if (isConnectedCellular) MaterialTheme.colorScheme.onSurface
                                    else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.25f)
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
                                    tint = if (isBluetoothEnabled) MaterialTheme.colorScheme.onSurface
                                    else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.25f)
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
                                    tint = if (isLocationEnabled) MaterialTheme.colorScheme.onSurface
                                    else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.25f)
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
                                    tint = if (isHotspotEnabled) MaterialTheme.colorScheme.onSurface
                                    else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.25f)
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
                                    tint = if (isAdbEnabled) MaterialTheme.colorScheme.onSurface
                                    else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.25f)
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
                                    tint = if (isNfcEnabled) MaterialTheme.colorScheme.onSurface
                                    else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.25f)
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
                                    tint = if (isSaverEnabled) MaterialTheme.colorScheme.onSurface
                                    else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.25f)
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
                                    tint = if (isAutorotateEnabled) MaterialTheme.colorScheme.onSurface
                                    else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.25f)
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
                                    contentDescription = null
                                )
                            }
                        }
                    }
                }

                Spacer(Modifier.size(8.dp))

                Card(modifier = Modifier
                    .width(240.dp)
                    .heightIn(max = 200.dp)
                ) {
                    LazyColumn(contentPadding = PaddingValues(start = 8.dp, top = 12.dp, end = 8.dp, bottom = 8.dp)) {
                        items(Notifications.notifications) { statusBarNotification ->
                            val notification = statusBarNotification.notification
                            val text = notification.extras.getString(Notification.EXTRA_TEXT)
                            val title = notification.extras.getString(Notification.EXTRA_TITLE)

                            if (!text.isNullOrEmpty() || !title.isNullOrEmpty()) {
                                Card(
                                    colors = CardColors(
                                        containerColor = MaterialTheme.colorScheme.surfaceDim,
                                        contentColor = CardDefaults.cardColors().contentColor,
                                        disabledContainerColor = CardDefaults.cardColors().disabledContainerColor,
                                        disabledContentColor = CardDefaults.cardColors().disabledContentColor
                                    ),
                                    modifier = Modifier
                                        .fillMaxWidth()
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
                                            color = MaterialTheme.colorScheme.primary,
                                            fontSize = 10.sp
                                        )
                                        if (!title.isNullOrEmpty()) {
                                            Text(
                                                title,
                                                fontWeight = FontWeight.SemiBold,
                                                fontSize = 14.sp
                                            )
                                        }
                                        if (!text.isNullOrEmpty()) {
                                            Text(text, fontSize = 13.sp)
                                        }
                                    }
                                }

                                Spacer(Modifier.size(4.dp))
                            }
                        }
                    }
                }
            }
        }

        Spacer(Modifier.size(12.dp))

        DockButton(
            {
                isDockOpened = !isDockOpened
                //isAccessibilityServiceRunning(context)
            },
            onActionDown,
            onActionMove
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
        if (settingValue != null) {
            stringColonSplitter.setString(settingValue)
            while (stringColonSplitter.hasNext()) {
                val accessbilityService = stringColonSplitter.next()
                Log.v("Accessibility", "AccessibilityService :: $accessbilityService $service")
                if (accessbilityService.equals(service, ignoreCase = true)) {
                    Log.v("Accessibility", "accessibility is on")
                    return true
                }
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
        onClick: (Offset) -> Unit,
        onActionDown: (rawX: Float, rawY: Float) -> Unit,
        onActionMove: (rawX: Float, rawY: Float) -> Unit
    ) {
    var touchX = 0f
    var touchY = 0f
    val clickThreshold = 10

    Box(
        modifier = Modifier
            .size(48.dp)
            .background(MaterialTheme.colorScheme.primary, shape = CircleShape)
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