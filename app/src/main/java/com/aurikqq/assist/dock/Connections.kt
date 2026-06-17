package com.aurikqq.assist.dock

import android.Manifest
import android.bluetooth.BluetoothManager
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.provider.Settings
import android.telephony.TelephonyManager
import androidx.annotation.RequiresPermission

class Connections {
    fun isBluetoothEnabled(context: Context): Boolean {
        val adapter = context.getSystemService(BluetoothManager::class.java).adapter
        return if (adapter == null) {
            false // device doesn't support bluetooth
        } else if (adapter.isEnabled) {
            true
        } else false
    }

    fun isWifiEnabled(context: Context): Boolean {
        val manager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val capabilities = manager.getNetworkCapabilities(manager.activeNetwork)

        return capabilities?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ?: false
    }

    @RequiresPermission(Manifest.permission.READ_PHONE_STATE)
    fun isCellularEnabled(context: Context): Boolean {
        val manager = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            manager.isDataEnabled
        }
        else {
            Settings.Global.getInt(context.contentResolver, "mobile_data", 0) == 1
        }
    }

    fun isLocationEnabled(context: Context) : Boolean {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as android.location.LocationManager
        return locationManager.isProviderEnabled(android.location.LocationManager.GPS_PROVIDER)
    }
}