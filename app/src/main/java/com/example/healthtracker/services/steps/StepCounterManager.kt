package com.example.healthtracker.services.steps


import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class StepCounterManager(context: Context) : SensorEventListener {

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val stepSensor     = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)

    private val _steps = MutableStateFlow(0)
    val steps: StateFlow<Int> = _steps

    // Passos no reboot guardados — para calcular os de hoje
    private var stepsAtMidnight = -1

    fun start() {
        stepSensor?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL)
        }
    }

    fun stop() {
        sensorManager.unregisterListener(this)
    }

    // Chama isto com o valor guardado no DataStore ao iniciar o dia
    fun setBaseSteps(base: Int) {
        stepsAtMidnight = base
    }

    override fun onSensorChanged(event: SensorEvent?) {
        event ?: return
        val totalSinceReboot = event.values[0].toInt()

        if (stepsAtMidnight == -1) {
            // Primeiro valor do dia — guarda como base
            stepsAtMidnight = totalSinceReboot
        }

        // Passos de hoje = total desde reboot - base do início do dia
        _steps.value = (totalSinceReboot - stepsAtMidnight).coerceAtLeast(0)
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
}