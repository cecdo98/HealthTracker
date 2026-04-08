package com.example.healthtracker.services.user

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.healthtracker.data.UserPreferences
import com.example.healthtracker.data.UserRepository
import com.example.healthtracker.data.room.DailyEntryEntity
import com.example.healthtracker.workers.MidnightResetWorker
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Calendar

class UserViewModel(application: Application) : AndroidViewModel(application) {

    private val repo = UserRepository(application)

    val prefs: StateFlow<UserPreferences> = repo.preferences
        .stateIn(
            scope        = viewModelScope,
            started      = SharingStarted.WhileSubscribed(5_000),
            initialValue = UserPreferences()
        )

    val historyEntries: StateFlow<List<DailyEntryEntity>> = repo.allEntries
        .stateIn(
            scope        = viewModelScope,
            started      = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    init {
        // Verifica ao abrir a app (fallback se o Worker não correu)
        viewModelScope.launch {
            repo.checkAndResetIfNewDay()
        }

        // Agenda o Worker para quando a app está fechada
        MidnightResetWorker.scheduleMidnightReset(application)

        // Watcher: recalcula a cada iteração quanto falta para a meia-noite
        // Isto garante o reset quando a app está aberta, mesmo vários dias seguidos
        viewModelScope.launch {
            while (true) {
                val ms = msUntilMidnight()
                Log.d("UserViewModel", "Watcher: próximo reset em ${ms / 1000 / 60} min")
                delay(ms + 1_000L) // +1s de margem para o relógio já ter avançado o dia
                Log.d("UserViewModel", "Watcher: a executar reset de meia-noite")
                repo.checkAndResetIfNewDay()
                // O loop volta ao início e recalcula o delay para a meia-noite SEGUINTE
            }
        }
    }

    /** Calcula os milissegundos que faltam até às 00:00:00 do dia seguinte */
    private fun msUntilMidnight(): Long {
        val now = Calendar.getInstance()
        val midnight = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_YEAR, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return midnight.timeInMillis - now.timeInMillis
    }

    // ── Perfil ────────────────────────────────────────────────────────────────

    fun saveProfile(
        firstName: String, lastName: String,
        weight: String, height: String,
        age: String, isMetric: Boolean
    ) {
        viewModelScope.launch {
            repo.saveProfile(firstName, lastName, weight, height, age, isMetric)
        }
    }

    fun saveProfilePicture(uri: String?) {
        viewModelScope.launch { repo.saveProfilePicture(uri) }
    }

    // ── Definições ────────────────────────────────────────────────────────────

    fun saveSettings(
        stepsGoal: Int, waterGoalMl: Int,
        notifWater: Boolean, notifSteps: Boolean, notifMood: Boolean,
        waterFreq: String, moodFreq: String,
        darkMode: Boolean, googleLinked: Boolean
    ) {
        viewModelScope.launch {
            repo.saveSettings(
                stepsGoal    = stepsGoal,
                waterGoalMl  = waterGoalMl,
                notifWater   = notifWater,
                notifSteps   = notifSteps,
                notifMood    = notifMood,
                waterFreq    = waterFreq,
                moodFreq     = moodFreq,
                darkMode     = darkMode,
                googleLinked = googleLinked
            )
        }
    }

    fun saveDarkMode(enabled: Boolean) {
        viewModelScope.launch { repo.saveDarkMode(enabled) }
    }

    // ── Dados diários ─────────────────────────────────────────────────────────

    fun addWater(ml: Int) {
        viewModelScope.launch {
            val (date, currentPrefs) = repo.checkAndResetIfNewDay()
            val newTotal = currentPrefs.todayWaterMl + ml
            repo.saveDailyData(
                date, currentPrefs.todaySteps, newTotal,
                currentPrefs.todayCalories, currentPrefs.todayEmotion,
                currentPrefs.stepsSensorBase
            )
        }
    }

    fun setEmotion(index: Int) {
        viewModelScope.launch {
            val (date, currentPrefs) = repo.checkAndResetIfNewDay()
            repo.saveDailyData(
                date, currentPrefs.todaySteps, currentPrefs.todayWaterMl,
                currentPrefs.todayCalories, index,
                currentPrefs.stepsSensorBase
            )
        }
    }
}