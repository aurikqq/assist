package com.aurikqq.assist.dock

import android.content.Context
import androidx.annotation.RequiresPermission
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.aurikqq.assist.Root

@RequiresPermission(android.Manifest.permission.READ_PHONE_STATE)
@Composable
fun Dock(context: Context) {
    var isDockOpened by remember { mutableStateOf(false) }
    val connections = Connections()

    var isWifiEnabled = connections.isWifiEnabled(context)
    var isBluetoothEnabled = connections.isBluetoothEnabled(context)
    var isCellularEnabled = connections.isCellularEnabled(context)
    var isLocationEnabled = connections.isLocationEnabled(context)
    var isHotspotEnabled = connections.isHotspotEnabled(context)
    var isNfcEnabled = connections.isNfcEnabled(context)
    var isAdbEnabled = connections.isAdbEnabled(context)
    var isSaverEnabled = connections.isSaverEnabled(context)
    var isAutorotateEnabled = connections.isAutorotateEnabled(context)
    val soundMode = connections.soundMode(context)

//    LaunchedEffect(Unit) {
//        val intent = Intent(Settings.Panel.ACTION_INTERNET_CONNECTIVITY).apply {
//            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
//        }
//        context.startActivity(intent)
//    }

    Row(
        horizontalArrangement = Arrangement.End,
        modifier = Modifier.fillMaxWidth()
    ) {
        if (isDockOpened) {
            Card(Modifier.width(240.dp)) {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 4.dp),
                    modifier = Modifier.padding(horizontal = 8.dp)
                ) {
                    item {
                        IconButton(
                            onClick = {
                                if (isWifiEnabled) {
                                    Root.execute("svc wifi disable")
                                    isWifiEnabled = false
                                } else {
                                    Root.execute("svc wifi enable")
                                    isWifiEnabled = true
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Wifi,
                                contentDescription = null,
                                tint = if (isWifiEnabled) MaterialTheme.colorScheme.onSurface
                                else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.25f)
                            )
                        }
                    }

                    item {
                        IconButton(
                            onClick = {
                                if (isCellularEnabled) {
                                    Root.execute("svc data disable")
                                    isCellularEnabled = false
                                } else {
                                    Root.execute("svc data enable")
                                    isCellularEnabled = true
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Filled.SignalCellularAlt,
                                contentDescription = null,
                                tint = if (isCellularEnabled) MaterialTheme.colorScheme.onSurface
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
                                    isAutorotateEnabled = false
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
                                    SoundModes.NORMAL -> Root.executeSingle("cmd audio set-ringer-mode 2")
                                    SoundModes.VIBRATE -> Root.executeSingle("cmd audio set-ringer-mode 1")
                                    SoundModes.SILENT -> Root.executeSingle("cmd audio set-ringer-mode 0")
                                }
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
        }

        Spacer(Modifier.size(12.dp))

        ElevatedButton(
            onClick = { isDockOpened = !isDockOpened },
            shape = CircleShape,
            modifier = Modifier
                .size(48.dp)
        ) { }
    }
}