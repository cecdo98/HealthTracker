package com.example.healthtracker.services.steps

import android.app.NotificationChannel
import android.app.NotificationManager
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
import com.example.healthtracker.R
import com.example.healthtracker.data.UserRepository
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first

class StepForegroundService : Service(), SensorEventListener {

    companion object {
        private const val TAG = "StepForegroundService"
        private const val CHANNEL_ID = "step_counter_channel"
        private const val NOTIF_ID = 1

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

    // Estado local — evita ler o DataStore a cada evento do sensor
    private var sensorBase = -1
    private var currentSteps = 0
    private var todayDate = ""
    private var todayWaterMl = 0
    private var todayEmotion = 2

    // Debounce: só persiste no DataStore no máximo 1x por segundo
    private var lastSaveJob: Job? = null

    override fun onCreate() {
        super.onCreate()
        repo = UserRepository(applicationContext)
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager

        createNotificationChannel()
        startForeground(NOTIF_ID, buildNotification(0))

        // Carrega o estado do DataStore UMA VEZ ao arrancar
        serviceScope.launch {
            repo.checkAndResetIfNewDay() // reseta se necessário

            // Lê as prefs já atualizadas após o possível reset
            val prefs = repo.preferences.first()
            val today = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
                .format(java.util.Date())

            todayDate    = today
            currentSteps = prefs.todaySteps
            sensorBase   = prefs.stepsSensorBase
            todayWaterMl = prefs.todayWaterMl
            todayEmotion = prefs.todayEmotion

            Log.d(TAG, "Serviço iniciado — steps=$currentSteps, base=$sensorBase, data=$todayDate")

            registerStepSensor()
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    private fun registerStepSensor() {
        val sensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
            ?: sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR)

        if (sensor == null) {
            Log.w(TAG, "Sensor de passos não disponível neste dispositivo")
            return
        }

        sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_UI)
        Log.d(TAG, "Sensor registado: ${sensor.name}")
    }

    override fun onSensorChanged(event: SensorEvent?) {
        event ?: return

        if (event.sensor.type == Sensor.TYPE_STEP_COUNTER) {
            val totalSinceReboot = event.values[0].toInt()

            if (sensorBase == -1 || totalSinceReboot < sensorBase) {
                sensorBase = totalSinceReboot - currentSteps
                Log.d(TAG, "Nova base definida: $sensorBase (total=$totalSinceReboot, steps=$currentSteps)")
            }

            currentSteps = (totalSinceReboot - sensorBase).coerceAtLeast(0)

        } else if (event.sensor.type == Sensor.TYPE_STEP_DETECTOR) {
            currentSteps++
        }

        updateNotification(currentSteps)
        scheduleSave()
    }

    private fun scheduleSave() {
        lastSaveJob?.cancel()
        lastSaveJob = serviceScope.launch {
            delay(1_000)

            // Verifica se o dia mudou enquanto o serviço estava ativo
            val today = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
                .format(java.util.Date())

            if (today != todayDate) {
                Log.d(TAG, "Novo dia detetado no serviço: $today — resetando base")
                repo.checkAndResetIfNewDay()
                todayDate    = today
                sensorBase   = -1
                currentSteps = 0
                // Atualiza água e humor do novo dia
                val prefs = repo.preferences.first()
                todayWaterMl = prefs.todayWaterMl
                todayEmotion = prefs.todayEmotion
            }

            val calories = (currentSteps * 0.04f).toInt()
            repo.saveDailyData(todayDate, currentSteps, todayWaterMl, calories, todayEmotion, sensorBase)

            Log.d(TAG, "Guardado: steps=$currentSteps, base=$sensorBase")
        }
    }

    // ── Notificação ───────────────────────────────────────────────────────────

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Contador de Passos",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "Mantém o contador de passos ativo em segundo plano"
        }
        getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
    }

    private fun buildNotification(steps: Int) =
        NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Health Tracker")
            .setContentText("$steps passos hoje")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setOngoing(true)
            .setSilent(true)
            .setLocalOnly(true)
            .build()

    private fun updateNotification(steps: Int) {
        getSystemService(NotificationManager::class.java).notify(NOTIF_ID, buildNotification(steps))
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        sensorManager.unregisterListener(this)
        serviceScope.cancel()
        Log.d(TAG, "Serviço destruído")
    }
}