package com.example.bestc.ui.screens

import android.content.Context
import android.content.SharedPreferences
import com.example.bestc.data.UserData
import java.time.LocalDateTime

object ProcessScreenHelper {
    fun scheduleAlarms(context: Context, userData: UserData, startDate: LocalDateTime) {
        // Move alarm scheduling logic here from ProcessScreen
        // This allows reuse from BootReceiver
    }

    fun saveUserDataToPrefs(context: Context, userData: UserData, startDate: LocalDateTime) {
        val prefs = context.getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        prefs.edit().apply {
            putString("wakeUpTime", userData.wakeUpTime)
            putString("sleepTime", userData.sleepTime)
            putString("startDate", startDate.toString())
            // ... save other user data
            apply()
        }
    }
} 