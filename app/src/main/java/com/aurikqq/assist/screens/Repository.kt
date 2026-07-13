package com.aurikqq.assist.screens

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit

class Repository(private val sharedPreferences: SharedPreferences, private val context: Context) {
    fun getSharedPreferencesInt(key: String, defaultValue: Int = 0) : Int {
        return sharedPreferences.getInt(key, defaultValue)
    }
    fun getSharedPreferencesFloat(key: String, defaultValue: Float = 0f) : Float {
        return sharedPreferences.getFloat(key, defaultValue)
    }
    fun getSharedPreferencesString(key: String, defaultValue: String = "") : String {
        return sharedPreferences.getString(key, defaultValue) ?: ""
    }
    fun getSharedPreferencesBool(key: String, defaultValue: Boolean = false) : Boolean {
        return sharedPreferences.getBoolean(key, defaultValue)
    }

    fun setSharedPreferencesInt(key: String, value: Int) {
        sharedPreferences.edit { putInt(key, value) }
    }
    fun setSharedPreferencesFloat(key: String, value: Float) {
        sharedPreferences.edit { putFloat(key, value) }
    }
    fun setSharedPreferencesString(key: String, value: String) {
        sharedPreferences.edit { putString(key, value) }
    }
    fun setSharedPreferencesBoolean(key: String, value: Boolean) {
        sharedPreferences.edit { putBoolean(key, value) }
    }



}