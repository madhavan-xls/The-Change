package com.example.bestc.services

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.core.content.ContextCompat
import com.example.bestc.data.UserData
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import com.example.bestc.services.ProcessScreenHelper

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            val serviceIntent = Intent(context, AlarmService::class.java).apply {
                action = "RESCHEDULE_ALARMS"
            }
            ContextCompat.startForegroundService(context, serviceIntent)
        }
    }

    private fun getUserDataFromPrefs(prefs: SharedPreferences): UserData? {
        val wakeUpTime = prefs.getString("wakeUpTime", null) ?: return null
        val sleepTime = prefs.getString("sleepTime", null) ?: return null
        
        return UserData(
            gender = prefs.getString("gender", "") ?: "",
            age = prefs.getInt("age", 0),
            cigarettesPerDay = prefs.getInt("cigarettesPerDay", 0),
            cigarettePrice = prefs.getFloat("cigarettePrice", 0f).toDouble(),
            yearsOfSmoking = prefs.getInt("yearsOfSmoking", 0),
            wakeUpTime = wakeUpTime,
            sleepTime = sleepTime
        )
    }

    private fun getStartDateFromPrefs(prefs: SharedPreferences): LocalDateTime? {
        return prefs.getString("startDate", null)?.let {
            LocalDateTime.parse(it)
        }
    }
} 