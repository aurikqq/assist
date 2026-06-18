package com.aurikqq.assist.dock

import android.Manifest
import android.bluetooth.BluetoothManager
import android.content.Context
import android.media.AudioManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.wifi.WifiManager
import android.nfc.NfcManager
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import android.telephony.TelephonyManager
import androidx.annotation.RequiresPermission

enum class SoundModes {
    NORMAL,
    VIBRATE,
    SILENT
}

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

    fun isHotspotEnabled(context: Context) : Boolean {
        return try {
            val manager = context.getSystemService(Context.WIFI_SERVICE) as WifiManager
            val method = manager.javaClass.getDeclaredMethod("isWifiApEnabled")
            method.isAccessible = true
            method.invoke(manager) as Boolean
        }
        catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun isNfcEnabled(context: Context) : Boolean {
        val manager = context.getSystemService(Context.NFC_SERVICE) as NfcManager

        return manager.defaultAdapter.isEnabled
    }

    fun isAdbEnabled(context: Context) : Boolean {
        return Settings.Secure.getInt(context.contentResolver, Settings.Global.ADB_ENABLED, 0) == 1
    }

    fun isSaverEnabled(context: Context) : Boolean {
        val manager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        return manager.isPowerSaveMode
    }

    fun isAutorotateEnabled(context: Context) : Boolean {
        return Settings.System.getInt(context.contentResolver, Settings.System.ACCELEROMETER_ROTATION, 0) == 1
    }

    fun soundMode(context: Context) : SoundModes {
        val manager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager

        val mode = when (manager.ringerMode) {
            AudioManager.RINGER_MODE_NORMAL -> SoundModes.NORMAL
            AudioManager.RINGER_MODE_VIBRATE -> SoundModes.VIBRATE
            else -> SoundModes.SILENT
        }
        return mode
    }
}