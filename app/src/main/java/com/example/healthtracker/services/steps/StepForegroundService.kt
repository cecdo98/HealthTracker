package com.example.healthtracker.services.steps

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.example.healthtracker.MainActivity
import com.example.healthtracker.R
import com.example.healthtracker.data.UserRepository
import kotlinx.coroutines.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.flow.first

class StepForegroundService : Service(), SensorEventListener {

    companion object {
        const val CHANNEL_ID     = "step_counter_channel"
        const val NOTIFICATION_ID = 1

        // Inicia o serviço
        fun start(context: Context) {
            val intent = Intent(context, StepForegroundService::class.java)
            context.startForegroundService(intent)
        }

        // Para o serviço
        fun stop(context: Context) {
            val intent = Intent(context, StepForegroundService::class.java)
            context.stopService(intent)
        }
    }

    private lateinit var sensorManager: SensorManager
    private lateinit var repo: UserRepository
    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    private var stepsAtStart   = -1
    private var savedStepsToday = 0

    // ── onCreate — chamado uma vez quando o serviço arranca ──
    override fun onCreate() {
        super.onCreate()
        repo          = UserRepository(applicationContext)
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager

        createNotificationChannel()
        startForeground(NOTIFICATION_ID, buildNotification(0))
        registerStepSensor()
        loadSavedSteps()
    }

    // ── Carrega os passos já guardados no DataStore ──
    private fun loadSavedSteps() {
        serviceScope.launch {
            val today      = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            val savedPrefs = repo.preferences.first()

            savedStepsToday = if (savedPrefs.todayDate == today) {
                savedPrefs.todaySteps
            } else {
                repo.resetDailyData(today)
                0
            }
        }
    }

    // ── Regista o sensor de passos ──
    private fun registerStepSensor() {
        val sensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
        sensor?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL)
        }
    }

    // ── Callback do sensor ──
    override fun onSensorChanged(event: SensorEvent?) {
        event ?: return
        val totalSinceReboot = event.values[0].toInt()

        if (stepsAtStart == -1) {
            stepsAtStart = totalSinceReboot - savedStepsToday
        }

        val todaySteps = (totalSinceReboot - stepsAtStart).coerceAtLeast(0)

        // Atualiza a notificação
        updateNotification(todaySteps)

        // Guarda no DataStore
        serviceScope.launch {
            val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            val prefs = repo.preferences.first()
            repo.saveDailyData(
                date     = today,
                steps    = todaySteps,
                waterMl  = prefs.todayWaterMl,
                calories = (todaySteps * 0.04f).toInt(),
                emotion  = prefs.todayEmotion
            )
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    // ── Notificação persistente ──
    private fun buildNotification(steps: Int): Notification {
        val openAppIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            this, 0, openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Health Tracker")
            .setContentText("Passos hoje: $steps")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentIntent(pendingIntent)
            .setOngoing(true)       // não pode ser dispensada pelo utilizador
            .setSilent(true)        // sem som
            .build()
    }

    private fun updateNotification(steps: Int) {
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(NOTIFICATION_ID, buildNotification(steps))
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Contador de Passos",
            NotificationManager.IMPORTANCE_LOW   // IMPORTANCE_LOW = sem som nem vibração
        ).apply {
            description = "Conta os passos em background"
        }
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.createNotificationChannel(channel)
    }

    // ── Cleanup ──
    override fun onDestroy() {
        super.onDestroy()
        sensorManager.unregisterListener(this)
        serviceScope.cancel()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}