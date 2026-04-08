package com.example.healthtracker.workers

import android.content.Context
import android.util.Log
import androidx.work.*
import kotlinx.coroutines.flow.first
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

class MidnightResetWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        Log.d(TAG, "Worker disparou às ${SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())}")

        return try {
            val repo = com.example.healthtracker.data.UserRepository(applicationContext)
            val prefs = repo.preferences.first()
            val yesterday = prefs.todayDate

            Log.d(TAG, "Data guardada no DataStore: '$yesterday'")

            if (yesterday.isNotEmpty()) {
                repo.saveEntryToHistory(
                    date     = yesterday,
                    steps    = prefs.todaySteps,
                    waterMl  = prefs.todayWaterMl,
                    calories = prefs.todayCalories,
                    emotion  = prefs.todayEmotion
                )
                Log.d(TAG, "Histórico guardado: steps=${prefs.todaySteps}, water=${prefs.todayWaterMl}")
            }

            val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            repo.resetDailyData(today)
            Log.d(TAG, "Reset feito para o dia: $today")

            // Agenda o próximo reset
            scheduleMidnightReset(applicationContext)

            Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "Erro no Worker: ${e.message}", e)
            Result.retry()
        }
    }

    companion object {
        private const val TAG = "MidnightResetWorker"
        const val WORK_NAME = "midnight_reset"

        fun scheduleMidnightReset(context: Context) {
            val now = Calendar.getInstance()

            val nextMidnight = Calendar.getInstance().apply {
                add(Calendar.DAY_OF_YEAR, 1)
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }

            val delayMs = nextMidnight.timeInMillis - now.timeInMillis
            val delayMinutes = delayMs / 1000 / 60

            Log.d(TAG, "Worker agendado para daqui a ${delayMinutes} minutos (meia-noite seguinte)")

            val request = OneTimeWorkRequestBuilder<MidnightResetWorker>()
                .setInitialDelay(delayMs, TimeUnit.MILLISECONDS)
                .build()

            WorkManager.getInstance(context).enqueueUniqueWork(
                WORK_NAME,
                ExistingWorkPolicy.REPLACE,
                request
            )
        }
    }
}