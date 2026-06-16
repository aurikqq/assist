package com.aurikqq.assist.dock

import android.bluetooth.BluetoothManager
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.wifi.WifiManager

class Connections {
    fun isBluetoothEnabled(context: Context): Boolean {
        val adapter = context.getSystemService(BluetoothManager::class.java).adapter
        return if (adapter == null) {
            false // device doesn't support bluetooth
        } else if (adapter.isEnabled) {
            true
        } else false
    }

    fun getWifiData(context: Context): Boolean {
        val manager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val capabilities = manager.getNetworkCapabilities(manager.activeNetwork)
        val wifi = context.getSystemService(Context.WIFI_SERVICE) as WifiManager
        if (capabilities?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ?: false) {
//            val ssid = wifi.connectionInfo.ssid
//            return mapOf(ssid to true)
            return true
        }
//        return mapOf(null to false)
        return false
    }

    fun isCellularEnabled(context: Context): Boolean {
        val manager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val capabilities = manager.getNetworkCapabilities(manager.activeNetwork)
        return capabilities?.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) == true
    }
}