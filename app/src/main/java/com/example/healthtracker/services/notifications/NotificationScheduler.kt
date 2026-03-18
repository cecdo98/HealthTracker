package com.example.healthtracker.services.notifications

import android.content.Context
import androidx.work.*
import java.util.concurrent.TimeUnit

object NotificationScheduler {

    private const val WATER_WORK_TAG = "water_reminder"

    // Frequências disponíveis em minutos
    val FREQUENCIES = mapOf(
        "30m" to 30L,
        "1h"  to 60L,
        "2h"  to 120L,
        "4h"  to 240L,
        "8h"  to 480L
    )

    // Agenda notificações periódicas de água
    fun scheduleWaterReminder(context: Context, frequencyMinutes: Long) {
        val request = PeriodicWorkRequestBuilder<WaterReminderWorker>(
            frequencyMinutes, TimeUnit.MINUTES
        )
            .addTag(WATER_WORK_TAG)
            .setConstraints(Constraints.Builder().build())
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            WATER_WORK_TAG,
            ExistingPeriodicWorkPolicy.UPDATE,  // substitui se já existir
            request
        )
    }

    // Cancela as notificações de água
    fun cancelWaterReminder(context: Context) {
        WorkManager.getInstance(context).cancelAllWorkByTag(WATER_WORK_TAG)
    }
}

