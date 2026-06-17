package com.aurikqq.assist.dock

import android.content.Context
import android.content.Intent
import android.provider.Settings
import androidx.annotation.RequiresPermission
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.SignalCellularAlt
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat.startActivity
import com.aurikqq.assist.Root

@RequiresPermission(android.Manifest.permission.READ_PHONE_STATE)
@Composable
fun Dock(context: Context) {
    var isDockOpened by remember { mutableStateOf(false) }
    val connections = Connections()

//    LaunchedEffect(Unit) {
//        val intent = Intent(Settings.Panel.ACTION_INTERNET_CONNECTIVITY).apply {
//            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
//        }
//        context.startActivity(intent)
//    }

    Row {
        if (isDockOpened) {
            Card {
                Row {
                    IconButton(
                        onClick = { if (connections.isWifiEnabled(context)) { Root.executeSingle("svc wifi enable") }
                            else { Root.executeSingle("svc wifi disable") } }
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Wifi,
                            contentDescription = null,
                            tint = if (connections.isWifiEnabled(context)) MaterialTheme.colorScheme.onSurface
                                else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.25f)
                        )
                    }

                    IconButton(
                        onClick = { /*TODO*/ }
                    ) {
                        Icon(
                            imageVector = Icons.Filled.SignalCellularAlt,
                            contentDescription = null,
                            tint = if (connections.isCellularEnabled(context)) MaterialTheme.colorScheme.onSurface
                            else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.25f)
                        )
                    }

                    IconButton(
                        onClick = { /*TODO*/ }
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Bluetooth,
                            contentDescription = null,
                            tint = if (connections.isBluetoothEnabled(context)) MaterialTheme.colorScheme.onSurface
                            else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.25f)
                        )
                    }
                }
            }
        }

        Button(
            onClick = { isDockOpened = !isDockOpened }
        ) {
            Card(shape = CircleShape) { }
        }
    }
}