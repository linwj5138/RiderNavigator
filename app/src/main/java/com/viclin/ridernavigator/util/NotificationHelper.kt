package com.viclin.ridernavigator.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import com.viclin.ridernavigator.R

//处理通知
class NotificationHelper(private val notificationManager: NotificationManager) {

    companion object {
        const val GPS_CHANNEL_ID = "GPS_CHANNEL"
        const val GPS_CHANNEL_NAME = "GPS Notifications"

        fun create(context: Context): NotificationHelper {
            return NotificationHelper(context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).apply {
                createNotificationChannel(context.applicationContext)
            }
        }
    }

    private fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                GPS_CHANNEL_ID,
                GPS_CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Channel for GPS signal notifications"
            }
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun showNotification(context: Context, title: String, content: String) {
        val notification = NotificationCompat.Builder(context, GPS_CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher) // 替换为您的通知图标资源
            .setContentTitle(title)
            .setContentText(content)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()
        notificationManager.notify(1, notification)
    }
}

