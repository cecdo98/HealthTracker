package com.example.healthtracker.services.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.example.healthtracker.R

class NotificationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val type = intent.getStringExtra("type") ?: return
        val channelId = "health_reminders"
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val channel = NotificationChannel(
            channelId, 
            "Lembretes de Saúde", 
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Notificações para água e estado emocional"
        }
        manager.createNotificationChannel(channel)

        val (title, text, id) = when (type) {
            "water" -> Triple("💧 Hora de beber água!", "Mantenha a sua meta de hidratação hoje.", 100)
            "mood"  -> Triple("😊 Como se sente hoje?", "Registe o seu estado emocional para acompanhar o seu progresso.", 101)
            else    -> return
        }

        val notification = NotificationCompat.Builder(context, channelId)
            .setContentTitle(title)
            .setContentText(text)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        manager.notify(id, notification)
    }
}
