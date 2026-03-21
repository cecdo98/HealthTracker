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
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.healthtracker.MainActivity
import com.example.healthtracker.R
import com.example.healthtracker.data.UserRepository
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class StepForegroundService : Service(), SensorEventListener {

    companion object {
        const val CHANNEL_ID = "step_counter_channel"
        const val NOTIFICATION_ID = 1

        fun start(context: Context) {
            val intent = Intent(context, StepForegroundService::class.java)
            context.startForegroundService(intent)
        }

        fun stop(context: Context) {
            val intent = Intent(context, StepForegroundService::class.java)
            context.stopService(intent)
        }
    }

    private lateinit var sensorManager: SensorManager
    private lateinit var repo: UserRepository
    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var initJob: Job? = null

    private var stepsAtStart = -1
    private var currentStepsToday = 0
    private var lastSavedDate = ""

    override fun onCreate() {
        super.onCreate()
        repo = UserRepository(applicationContext)
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager

        createNotificationChannel()
        startForeground(NOTIFICATION_ID, buildNotification(currentStepsToday))
        
        // 1. Inicializa os dados da base de dados antes de processar o sensor
        initJob = serviceScope.launch {
            val today = repo.checkAndResetIfNewDay()
            lastSavedDate = today
            val prefs = repo.preferences.first()
            currentStepsToday = prefs.todaySteps
            updateNotification(currentStepsToday)
            Log.d("StepService", "Inicializado: $currentStepsToday passos em $today")
        }

        // 2. Regista o sensor
        registerStepSensor()
    }

    private fun registerStepSensor() {
        val sensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
        if (sensor != null) {
            sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_UI)
        } else {
            Log.e("StepService", "Sensor de passos não disponível neste dispositivo!")
        }
    }

    override fun onSensorChanged(event: SensorEvent?) {
        event ?: return
        if (event.sensor.type == Sensor.TYPE_STEP_COUNTER) {
            val totalSinceReboot = event.values[0].toInt()
            val systemToday = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

            serviceScope.launch {
                // Aguarda que a inicialização termine para não sobrescrever com 0
                initJob?.join()
                
                // Verifica mudança de dia
                if (systemToday != lastSavedDate) {
                    val actualToday = repo.checkAndResetIfNewDay()
                    if (actualToday != lastSavedDate) {
                        lastSavedDate = actualToday
                        stepsAtStart = totalSinceReboot
                        currentStepsToday = 0
                    }
                }

                // Define a base de cálculo (total do sensor - o que já tínhamos hoje)
                if (stepsAtStart == -1) {
                    stepsAtStart = totalSinceReboot - currentStepsToday
                }

                val calculatedSteps = (totalSinceReboot - stepsAtStart).coerceAtLeast(0)
                
                // Só grava se houver progresso real
                if (calculatedSteps > currentStepsToday) {
                    currentStepsToday = calculatedSteps
                    updateNotification(currentStepsToday)
                    
                    val prefs = repo.preferences.first()
                    repo.saveDailyData(
                        date = lastSavedDate,
                        steps = currentStepsToday,
                        waterMl = prefs.todayWaterMl,
                        calories = (currentStepsToday * 0.04f).toInt(),
                        emotion = prefs.todayEmotion
                    )
                }
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

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
            .setOngoing(true)
            .setSilent(true)
            .setOnlyAlertOnce(true)
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
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "Contagem de passos em tempo real"
            setShowBadge(false)
        }
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.createNotificationChannel(channel)
    }

    override fun onDestroy() {
        super.onDestroy()
        sensorManager.unregisterListener(this)
        serviceScope.cancel()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}