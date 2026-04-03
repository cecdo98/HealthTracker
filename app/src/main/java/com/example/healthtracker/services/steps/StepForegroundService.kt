package com.example.healthtracker.services.steps

import android.app.Service
import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.IBinder
import com.example.healthtracker.data.UserRepository
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first

class StepForegroundService : Service(), SensorEventListener {

    companion object {
        fun start(context: Context) {
            val intent = Intent(context, StepForegroundService::class.java)
            context.startService(intent)
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
        repo = UserRepository(applicationContext)
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        registerStepSensor()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY // Garante que o Android tenta reiniciar o serviço se o matar
    }

    private fun registerStepSensor() {
        val sensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER) ?: 
                     sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR)
        sensor?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
        }
    }

    override fun onSensorChanged(event: SensorEvent?) {
        event ?: return
        serviceScope.launch {
            val prefs = repo.preferences.first()
            val today = repo.checkAndResetIfNewDay()
            
            var currentSteps = prefs.todaySteps
            var sensorBase = prefs.stepsSensorBase

            if (event.sensor.type == Sensor.TYPE_STEP_COUNTER) {
                val total = event.values[0].toInt()
                // Se for a primeira vez ou o telemóvel reiniciou
                if (sensorBase == -1 || total < sensorBase) {
                    sensorBase = total - currentSteps
                }
                currentSteps = (total - sensorBase).coerceAtLeast(0)
                
                repo.saveDailyData(today, currentSteps, prefs.todayWaterMl, (currentSteps * 0.04f).toInt(), prefs.todayEmotion, sensorBase)
            } else {
                currentSteps++
                repo.saveDailyData(today, currentSteps, prefs.todayWaterMl, (currentSteps * 0.04f).toInt(), prefs.todayEmotion)
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        sensorManager.unregisterListener(this)
        serviceScope.cancel()
    }
}
