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
        viewModelScope.launch { repo.checkAndResetIfNewDay() }

        MidnightResetWorker.scheduleMidnightReset(application)

        viewModelScope.launch {
            while (true) {
                val ms = msUntilMidnight()
                Log.d("UserViewModel", "Watcher: próximo reset em ${ms / 1000 / 60} min")
                delay(ms + 1_000L)
                Log.d("UserViewModel", "Watcher: a executar reset de meia-noite")
                repo.checkAndResetIfNewDay()
            }
        }
    }

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

    /**
     * Adiciona água de forma atómica — reseta o dia e adiciona num único edit{},
     * eliminando qualquer race condition entre o reset e a escrita.
     */
    fun addWater(ml: Int) {
        viewModelScope.launch {
            repo.addWaterAtomic(ml)
        }
    }

    /**
     * Define o humor de forma atómica — reseta o dia e escreve o humor num único edit{},
     * eliminando qualquer race condition entre o reset e a escrita.
     */
    fun setEmotion(index: Int) {
        viewModelScope.launch {
            repo.setEmotionAtomic(index)
        }
    }
}