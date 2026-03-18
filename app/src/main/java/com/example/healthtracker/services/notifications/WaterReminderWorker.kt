package com.example.healthtracker.services.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.example.healthtracker.R

class WaterReminderWorker(
    context: Context,
    params: WorkerParameters
) : Worker(context, params) {

    companion object {
        const val CHANNEL_ID = "water_reminder_channel"
        const val NOTIF_ID   = 10
    }

    override fun doWork(): Result {
        sendNotification()
        return Result.success()
    }

    private fun sendNotification() {
        val manager = applicationContext
            .getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Cria o canal se ainda não existir
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Lembretes de Água",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply { description = "Lembrete para beber água" }
        manager.createNotificationChannel(channel)

        val notification = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Hora de beber água! 💧")
            .setContentText("Mantém-te hidratado. Bebe um copo de água agora.")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()

        manager.notify(NOTIF_ID, notification)
    }
}