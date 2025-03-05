package com.example.bestc.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.net.Uri
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.core.app.NotificationCompat
import com.example.bestc.MainActivity
import com.example.bestc.R

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val soundUri = intent.getParcelableExtra<Uri>("sound_uri")
            ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
        val vibrationEnabled = intent.getBooleanExtra("vibration_enabled", true)

        // Start alarm
        MediaPlayerHelper.play(context, soundUri)
        
        // Create notification
        val notification = NotificationCompat.Builder(context, "alarm_channel")
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle("Nicotine Reminder")
            .setContentText(intent.getStringExtra("message") ?: "Time for your dose")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .addAction(
                R.drawable.ic_check,
                "Take",
                getActionPendingIntent(context, ACTION_TAKE, intent)
            )
            .addAction(
                R.drawable.ic_snooze,
                "Snooze (5m)",
                getActionPendingIntent(context, ACTION_SNOOZE, intent)
            )
            .build()

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) 
            as NotificationManager
        notificationManager.notify(intent.getIntExtra("alarm_id", 0), notification)
    }

    private fun getActionPendingIntent(
        context: Context,
        action: String,
        originalIntent: Intent
    ): PendingIntent {
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtras(originalIntent)
            this.action = action
        }
        return PendingIntent.getBroadcast(
            context,
            originalIntent.getIntExtra("alarm_id", 0),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    companion object {
        const val ACTION_TAKE = "TAKE_ACTION"
        const val ACTION_SNOOZE = "SNOOZE_ACTION"
    }
} 