package com.example.bestc.services

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.media.RingtoneManager
import android.util.Log
import com.example.bestc.data.UserData
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.*

object ProcessScreenHelper {
    fun getUserDataFromPrefs(prefs: SharedPreferences): UserData? {
        return try {
            UserData(
                gender = prefs.getString("gender", "") ?: "",
                age = prefs.getInt("age", 0),
                cigarettesPerDay = prefs.getInt("cigarettesPerDay", 0),
                cigarettePrice = prefs.getString("cigarettePrice", null)?.toDouble() ?: run {
                    Log.e("ProcessScreenHelper", "Invalid cigarette price format")
                    return null
                },
                yearsOfSmoking = prefs.getInt("yearsOfSmoking", 0),
                wakeUpTime = prefs.getString("wakeUpTime", "06:00") ?: "06:00",
                sleepTime = prefs.getString("sleepTime", "22:00") ?: "22:00"
            )
        } catch (e: Exception) {
            Log.e("ProcessScreenHelper", "Error loading user data: ${e.message}")
            null
        }
    }

    fun getStartDateFromPrefs(prefs: SharedPreferences): LocalDateTime? {
        return prefs.getString("startDate", null)?.let {
            LocalDateTime.parse(it)
        }
    }

    fun scheduleAlarms(context: Context, userData: UserData, startDate: LocalDateTime) {
        // Validate wake/sleep times first
        if (userData.wakeUpTime.isEmpty() || userData.sleepTime.isEmpty()) {
            throw IllegalArgumentException("Invalid wake/sleep times")
        }
        
        val formatter = DateTimeFormatter.ofPattern("HH:mm")
        val wakeUpTime = try {
            LocalTime.parse(userData.wakeUpTime, formatter)
        } catch (e: Exception) {
            throw IllegalArgumentException("Invalid wake-up time format")
        }
        
        val sleepTime = try {
            LocalTime.parse(userData.sleepTime, formatter)
        } catch (e: Exception) {
            throw IllegalArgumentException("Invalid sleep time format")
        }

        // Validate time order
        if (wakeUpTime >= sleepTime) {
            throw IllegalArgumentException("Wake-up time must be before sleep time")
        }

        try {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            
            // Cancel existing alarms first
            cancelExistingAlarms(context, alarmManager)

            val currentWeek = ChronoUnit.WEEKS.between(startDate, LocalDateTime.now()).toInt() + 1
            
            // Calculate time gap based on current week
            val timeGapMinutes = when {
                currentWeek == 1 -> 90L    // 1h 30m
                currentWeek == 2 -> 120L   // 2h
                currentWeek == 3 -> 150L   // 2h 30m
                currentWeek == 4 -> 180L   // 3h
                currentWeek == 5 -> 210L   // 3h 30m
                else -> 240L               // 4h (week 6+)
            }

            val alarmTimes = calculateAlarmTimes(
                wakeUpTime, 
                sleepTime,
                timeGapMinutes
            )
            
            // Filter alarms within wake-sleep window
            val filteredTimes = alarmTimes.filter { time ->
                time.isAfter(wakeUpTime) &&
                time.isBefore(sleepTime)
            }

            scheduleAlarmsForTimes(context, filteredTimes)
        } catch (e: Exception) {
            e.printStackTrace()
            throw e
        }
    }

    fun updateUserDataAndAlarms(context: Context, userData: UserData, startDate: LocalDateTime) {
        try {
            // First save the data
            saveUserData(context, userData, startDate)
            
            // Then schedule new alarms (this will handle canceling existing alarms)
            scheduleAlarms(context, userData, startDate)
        } catch (e: Exception) {
            e.printStackTrace()
            throw e // Rethrow to handle in UI
        }
    }

    private fun calculateAlarmTimes(
        wakeUpTime: LocalTime,
        sleepTime: LocalTime,
        intervalMinutes: Long
    ): List<LocalTime> {
        val times = mutableListOf<LocalTime>()
        var currentTime = wakeUpTime
        
        // Add safety check for maximum alarms
        val maxAlarms = 1000
        var iterationCount = 0

        while (currentTime.isBefore(sleepTime) && iterationCount < maxAlarms) {
            times.add(currentTime)
            currentTime = currentTime.plusMinutes(intervalMinutes)
            iterationCount++
            
            // Add overflow protection
            if (iterationCount >= maxAlarms) {
                throw IllegalStateException("Exceeded maximum allowed alarms ($maxAlarms)")
            }
        }
        
        return times
    }

    private fun scheduleAlarmsForTimes(context: Context, alarmTimes: List<LocalTime>) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        
        // Schedule new alarms
        alarmTimes.forEachIndexed { index, time ->
            val calendar = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, time.hour)
                set(Calendar.MINUTE, time.minute)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }

            if (calendar.timeInMillis <= System.currentTimeMillis()) {
                calendar.add(Calendar.DAY_OF_YEAR, 1)
            }

            val intent = Intent(context, AlarmReceiver::class.java).apply {
                putExtra("alarm_id", index)
                putExtra("is_repeat", false)
                putExtra("sound_uri", RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                putExtra("vibration_enabled", true)
                putExtra("message", "Time for Nicotine Gum")
            }

            val pendingIntent = PendingIntent.getBroadcast(
                context,
                index,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                pendingIntent
            )
        }
    }

    private fun cancelExistingAlarms(context: Context, alarmManager: AlarmManager) {
        for (i in 0..40) {
            val intent = Intent(context, AlarmReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                i,
                intent,
                PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
            )
            pendingIntent?.let { alarmManager.cancel(it) }
        }
    }

    fun saveUserData(context: Context, userData: UserData, startDate: LocalDateTime) {
        val prefs = context.getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        val editor = prefs.edit()
        editor.putString("wakeUpTime", userData.wakeUpTime)
        editor.putString("sleepTime", userData.sleepTime)
        editor.putString("gender", userData.gender)
        editor.putInt("age", userData.age)
        editor.putInt("cigarettesPerDay", userData.cigarettesPerDay)
        editor.putString("cigarettePrice", userData.cigarettePrice.toString())
        editor.putInt("yearsOfSmoking", userData.yearsOfSmoking)
        editor.putString("startDate", startDate.toString())
        editor.apply()
    }
} 