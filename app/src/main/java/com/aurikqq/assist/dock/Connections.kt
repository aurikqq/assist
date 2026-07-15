package com.aurikqq.assist.dock

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.net.wifi.WifiManager
import android.nfc.NfcManager
import android.os.PowerManager
import android.provider.Settings
import androidx.compose.runtime.mutableStateOf
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

enum class SoundModes {
    NORMAL,
    VIBRATE,
    SILENT
}

class Connections(context: Context) {
    private var connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    val isWifiEnabledFlow: Flow<Boolean>
        get() = callbackFlow {
            val networkCallback = object : ConnectivityManager.NetworkCallback() {
                override fun onAvailable(network: Network) {
                    connectivityManager.getNetworkCapabilities(network).let {
                        if (it?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) ?: false) {
                            trySend(true)
                        }
                    }
                }

                override fun onLost(network: Network) {
                    trySend(false)
                }

                override fun onUnavailable() {
                    trySend(false)
                }

                override fun onCapabilitiesChanged(
                    network: Network,
                    networkCapabilities: NetworkCapabilities
                ) {
                    super.onCapabilitiesChanged(network, networkCapabilities)
                    if (networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)) {
                        trySend(true)
                    }
                    else {
                        trySend(false)
                    }
                }
            }

            val wifiRequest = NetworkRequest.Builder()
                .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
                .build()

            connectivityManager.registerNetworkCallback(wifiRequest, networkCallback)

            awaitClose {
                connectivityManager.unregisterNetworkCallback(networkCallback)
            }
        }

    val isCellularEnabledFlow: Flow<Boolean>
        get() = callbackFlow {
            val networkCallback = object : ConnectivityManager.NetworkCallback() {
                override fun onAvailable(network: Network) {
                    connectivityManager.getNetworkCapabilities(network).let {
                        if (it?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) ?: false) {
                            trySend(true)
                        }
                    }
                }

                override fun onLost(network: Network) {
                    trySend(false)
                }

                override fun onUnavailable() {
                    trySend(false)
                }

                override fun onCapabilitiesChanged(
                    network: Network,
                    networkCapabilities: NetworkCapabilities
                ) {
                    super.onCapabilitiesChanged(network, networkCapabilities)
                    if (networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)) {
                        trySend(true)
                    }
                    else {
                        trySend(false)
                    }
                }
            }

            val cellularRequest = NetworkRequest.Builder()
                .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                .addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR)
                .build()

            connectivityManager.registerNetworkCallback(cellularRequest, networkCallback)

            awaitClose {
                connectivityManager.unregisterNetworkCallback(networkCallback)
            }
        }

    fun isBluetoothEnabled(context: Context) : Boolean {
        val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        return bluetoothManager.adapter.isEnabled
    }

//    fun isWifiEnabled(): Boolean {
//        val capabilities = connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
//
//        return capabilities?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ?: false
//    }
//
//    @RequiresPermission(Manifest.permission.READ_PHONE_STATE)
//    fun isCellularEnabled(context: Context): Boolean {
//        val manager = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
//
//        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            manager.isDataEnabled
//        }
//        else {
//            Settings.Global.getInt(context.contentResolver, "mobile_data", 0) == 1
//        }
//    }

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
        return Settings.Global.getInt(context.contentResolver, Settings.Global.ADB_ENABLED, 0) == 1
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