package com.example.healthtracker.services.steps

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.example.healthtracker.MainActivity
import com.example.healthtracker.R
import com.example.healthtracker.data.UserRepository
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first

class StepForegroundService : Service(), SensorEventListener {

    companion object {
        const val CHANNEL_ID     = "step_counter_channel"
        const val NOTIFICATION_ID = 1

        fun start(context: Context) {
            val intent = Intent(context, StepForegroundService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stop(context: Context) {
            val intent = Intent(context, StepForegroundService::class.java)
            context.stopService(intent)
        }
    }

    private lateinit var sensorManager: SensorManager
    private lateinit var repo: UserRepository
    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    override fun onCreate() {
        super.onCreate()
        repo          = UserRepository(applicationContext)
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager

        createNotificationChannel()
        
        val notification = buildNotification(0)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(NOTIFICATION_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_HEALTH)
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }
        
        registerStepSensor()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        registerStepSensor()
        return START_STICKY
    }

    private fun registerStepSensor() {
        val stepCounter = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
        if (stepCounter != null) {
            sensorManager.registerListener(this, stepCounter, SensorManager.SENSOR_DELAY_UI)
        } else {
            val stepDetector = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR)
            sensorManager.registerListener(this, stepDetector, SensorManager.SENSOR_DELAY_UI)
        }
    }

    override fun onSensorChanged(event: SensorEvent?) {
        event ?: return
        
        if (event.sensor.type == Sensor.TYPE_STEP_COUNTER) {
            val totalSinceReboot = event.values[0].toInt()
            
            serviceScope.launch {
                val today = repo.checkAndResetIfNewDay()
                val prefs = repo.preferences.first()

                // Lógica de cálculo persistente
                var sensorBase = prefs.stepsSensorBase
                
                // Se for a primeira vez ou o telemóvel reiniciou (total < base)
                if (sensorBase == -1 || totalSinceReboot < sensorBase) {
                    sensorBase = totalSinceReboot - prefs.todaySteps
                }

                val todaySteps = (totalSinceReboot - sensorBase).coerceAtLeast(0)
                
                // Só guarda se houver alteração para evitar escritas desnecessárias
                if (todaySteps != prefs.todaySteps || sensorBase != prefs.stepsSensorBase) {
                    updateNotification(todaySteps)
                    repo.saveDailyData(
                        date       = today,
                        steps      = todaySteps,
                        waterMl    = prefs.todayWaterMl,
                        calories   = (todaySteps * 0.04f).toInt(),
                        emotion    = prefs.todayEmotion,
                        sensorBase = sensorBase // Guarda a base persistente
                    )
                }
            }
        } else if (event.sensor.type == Sensor.TYPE_STEP_DETECTOR) {
            serviceScope.launch {
                val today = repo.checkAndResetIfNewDay()
                val prefs = repo.preferences.first()
                val newSteps = prefs.todaySteps + 1
                
                updateNotification(newSteps)
                repo.saveDailyData(
                    date     = today,
                    steps    = newSteps,
                    waterMl  = prefs.todayWaterMl,
                    calories = (newSteps * 0.04f).toInt(),
                    emotion  = prefs.todayEmotion
                )
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
            .setContentTitle("Health Tracker Ativo")
            .setContentText("Passos hoje: $steps")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setSilent(true)
            .build()
    }

    private fun updateNotification(steps: Int) {
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(NOTIFICATION_ID, buildNotification(steps))
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID, "Contagem de Passos",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        sensorManager.unregisterListener(this)
        serviceScope.cancel()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
