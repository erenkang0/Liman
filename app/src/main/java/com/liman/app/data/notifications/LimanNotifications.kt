package com.liman.app.data.notifications

import android.annotation.SuppressLint
import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.liman.app.MainActivity
import com.liman.app.R

/** Liman hatırlatma türleri — her biri bağımsız planlanır. */
enum class ReminderKind(
    val uniqueName: String,
    val notifId: Int,
    val title: String,
    val message: String,
    val defaultHour: Int,
    val defaultMinute: Int,
) {
    GENTLE(
        "liman_reminder_gentle", 1001,
        "Liman", "Bugün kendine bir an ayır. Dilersen birkaç satır yaz.",
        21, 0,
    ),
    MOOD(
        "liman_reminder_mood", 1002,
        "Nasılsın?", "Bugün ne durumdasın? Hızlıca bir ruh hali bırakabilirsin.",
        13, 0,
    ),
    BOND(
        "liman_reminder_bond", 1003,
        "Bir bağ", "Uzun süredir konuşmadığın biri olabilir. Kısa bir merhaba iyi gelir.",
        18, 0,
    ),
}

/** Bildirim kanalı oluşturma ve bildirim gönderme yardımcıları. */
object LimanNotifications {

    const val CHANNEL_ID = "liman_reminders"

    fun ensureChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Hatırlatıcılar",
                NotificationManager.IMPORTANCE_DEFAULT,
            ).apply { description = "Liman'ın nazik hatırlatmaları" }
            context.getSystemService(NotificationManager::class.java)
                ?.createNotificationChannel(channel)
        }
    }

    fun hasPermission(context: Context): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return true
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.POST_NOTIFICATIONS,
        ) == PackageManager.PERMISSION_GRANTED
    }

    @SuppressLint("MissingPermission") // hasPermission() ile korunuyor
    fun post(context: Context, title: String, text: String, notifId: Int) {
        if (!hasPermission(context)) return
        ensureChannel(context)

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pending = PendingIntent.getActivity(
            context,
            notifId,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(text)
            .setStyle(NotificationCompat.BigTextStyle().bigText(text))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(pending)
            .build()

        NotificationManagerCompat.from(context).notify(notifId, notification)
    }
}
