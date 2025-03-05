package com.example.bestc.services

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.media.RingtoneManager
import com.example.bestc.data.UserData
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.*

object ProcessScreenHelper {
    fun getUserDataFromPrefs(prefs: SharedPreferences): UserData? {
        val wakeUpTime = prefs.getString("wakeUpTime", null) ?: return null
        val sleepTime = prefs.getString("sleepTime", null) ?: return null
        
        return UserData(
            wakeUpTime = wakeUpTime,
            sleepTime = sleepTime,
            // ... other fields
        )
    }

    fun getStartDateFromPrefs(prefs: SharedPreferences): LocalDateTime? {
        return prefs.getString("startDate", null)?.let {
            LocalDateTime.parse(it)
        }
    }

    fun scheduleAlarms(context: Context, userData: UserData, startDate: LocalDateTime) {
        try {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            
            // Cancel existing alarms first
            cancelExistingAlarms(context, alarmManager)

            val formatter = DateTimeFormatter.ofPattern("HH:mm")
            val wakeUpTime = try {
                LocalTime.parse(userData.wakeUpTime, formatter)
            } catch (e: Exception) {
                LocalTime.of(6, 0) // Default fallback
            }
            
            val sleepTime = try {
                LocalTime.parse(userData.sleepTime, formatter)
            } catch (e: Exception) {
                LocalTime.of(22, 0) // Default fallback
            }
            
            val currentDate = LocalDateTime.now()
            val weeksSinceStart = ChronoUnit.WEEKS.between(startDate, currentDate)
            val currentWeek = weeksSinceStart + 1

            // Calculate time gap based on current week
            val timeGapMinutes = when {
                currentWeek <= 1 -> 90L  // Week 1: 1h 30m
                currentWeek == 2L -> 120L // Week 2: 2h
                currentWeek == 3L -> 150L // Week 3: 2h 30m
                currentWeek == 4L -> 180L // Week 4: 3h
                currentWeek == 5L -> 210L // Week 5: 3h 30m
                else -> 240L // Week 6+: 4h
            }

            val alarmTimes = calculateAlarmTimes(wakeUpTime, sleepTime, timeGapMinutes)
            scheduleAlarmsForTimes(context, alarmTimes)
        } catch (e: Exception) {
            e.printStackTrace()
            throw e
        }
    }

    fun updateUserDataAndAlarms(context: Context, userData: UserData, startDate: LocalDateTime) {
        try {
            // First save the data
            saveUserDataToPrefs(context, userData, startDate)
            
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

        while (currentTime.isBefore(sleepTime)) {
            times.add(currentTime)
            currentTime = currentTime.plusMinutes(intervalMinutes)
            
            if (currentTime.isAfter(sleepTime)) {
                break
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

    fun saveUserDataToPrefs(context: Context, userData: UserData, startDate: LocalDateTime) {
        val prefs = context.getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        prefs.edit().apply {
            putString("wakeUpTime", userData.wakeUpTime)
            putString("sleepTime", userData.sleepTime)
            putString("gender", userData.gender)
            putInt("age", userData.age)
            putInt("cigarettesPerDay", userData.cigarettesPerDay)
            putFloat("cigarettePrice", userData.cigarettePrice.toFloat())
            putInt("yearsOfSmoking", userData.yearsOfSmoking)
            putString("startDate", startDate.toString())
            apply()
        }
    }
} 