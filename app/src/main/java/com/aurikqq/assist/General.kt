package com.aurikqq.assist

import android.content.Context
import android.hardware.display.DisplayManager
import android.net.ConnectivityManager
import android.view.Display

class General {
    fun stringToNumber(input: String): Int? {
        val units = mapOf(
            "ноль" to 0, "один" to 1, "два" to 2, "три" to 3, "четыре" to 4,
            "пять" to 5, "шесть" to 6, "семь" to 7, "восемь" to 8, "девять" to 9
        )
        val teens = mapOf(
            "десять" to 10, "одиннадцать" to 11, "двенадцать" to 12, "тринадцать" to 13,
            "четырнадцать" to 14, "пятнадцать" to 15, "шестнадцать" to 16,
            "семнадцать" to 17, "восемнадцать" to 18, "девятнадцать" to 19
        )
        val tens = mapOf(
            "двадцать" to 20, "тридцать" to 30, "сорок" to 40, "пятьдесят" to 50,
            "шестьдесят" to 60, "семьдесят" to 70, "восемьдесят" to 80, "девяносто" to 90
        )
        val hundreds = mapOf("сто" to 100)

        val words = input.lowercase().split(" ")
        var result = 0
        var found = false

        for (word in words) {
            val value = units[word] ?: teens[word] ?: tens[word] ?: hundreds[word]
            if (value != null) {
                result += value
                found = true
            }
        }
        return if (found) result else null
    }

    fun isScreenOff(context: Context): Boolean {
        val displayManager = context.getSystemService(Context.DISPLAY_SERVICE) as DisplayManager
        val display = displayManager.getDisplay(Display.DEFAULT_DISPLAY)
        return display?.state == Display.STATE_OFF
    }

    fun isInternetAvailable(context: Context): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val capabilities =
            connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
        return capabilities != null
    }
 }