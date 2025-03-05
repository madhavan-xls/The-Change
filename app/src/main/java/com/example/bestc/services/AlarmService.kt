package com.example.bestc.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.example.bestc.R
import android.content.pm.ServiceInfo
import android.app.AlarmManager
import android.os.SystemClock
import android.app.PendingIntent
import android.content.Context
import android.media.MediaPlayer
import android.net.Uri
import java.util.*
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.app.Notification

class AlarmService : Service() {
    companion object {
        const val CHANNEL_ID = "BestcServiceChannel"
        const val NOTIFICATION_ID = 1
    }

    override fun onCreate() {
        super.onCreate()
        try {
            createNotificationChannel()
            val notification = createNotification()
            startForeground(NOTIFICATION_ID, notification)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == "RESCHEDULE_ALARMS") {
            val prefs = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
            if (!prefs.contains("wakeUpTime")) {
                // Stop service if no user data exists
                stopSelf()
                return START_NOT_STICKY
            }
            // Reschedule alarms only if user data exists
            val userData = ProcessScreenHelper.getUserDataFromPrefs(prefs)
            val startDate = ProcessScreenHelper.getStartDateFromPrefs(prefs)
            if (userData != null && startDate != null) {
                ProcessScreenHelper.scheduleAlarms(this, userData, startDate)
            }
        }
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        // Restart service if app is force stopped
        val restartServiceIntent = Intent(applicationContext, AlarmService::class.java)
        val restartServicePendingIntent = PendingIntent.getService(
            applicationContext, 1, restartServiceIntent, PendingIntent.FLAG_IMMUTABLE
        )
        val alarmService = applicationContext.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmService.set(
            AlarmManager.ELAPSED_REALTIME,
            SystemClock.elapsedRealtime() + 1000,
            restartServicePendingIntent
        )
        super.onTaskRemoved(rootIntent)
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Bestc Service Channel",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "Channel for nicotine gum reminder service"
        }
        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(channel)
    }

    private fun createNotification() = NotificationCompat.Builder(this, CHANNEL_ID)
        .setContentTitle("Nicotine Gum Reminder")
        .setContentText("Running in background")
        .setSmallIcon(R.drawable.ic_notification)
        .setPriority(NotificationCompat.PRIORITY_LOW)
        .build()

    private fun handleTakeAction() {
        MediaPlayerHelper.stop()
        stopForeground(true)
        stopSelf()
    }

    private fun handleSnoozeAction(originalIntent: Intent) {
        MediaPlayerHelper.stop()
        
        // Reschedule alarm after 5 minutes
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val calendar = Calendar.getInstance().apply {
            timeInMillis = System.currentTimeMillis()
            add(Calendar.MINUTE, 5)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            this,
            originalIntent.getIntExtra("alarm_id", 0),
            originalIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            calendar.timeInMillis,
            pendingIntent
        )

        stopForeground(true)
        stopSelf()
    }
} 