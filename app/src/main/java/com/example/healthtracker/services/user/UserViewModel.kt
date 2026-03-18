package com.example.healthtracker.services.user

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.healthtracker.data.UserPreferences
import com.example.healthtracker.data.UserRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class UserViewModel(application: Application) : AndroidViewModel(application) {

    private val repo = UserRepository(application)

    val prefs: StateFlow<UserPreferences> = repo.preferences
        .stateIn(
            scope        = viewModelScope,
            started      = SharingStarted.WhileSubscribed(5_000),
            initialValue = UserPreferences()
        )

    // ── Atalhos de leitura ──
    val firstName     get() = prefs.value.firstName
    val lastName      get() = prefs.value.lastName
    val weight        get() = prefs.value.weight
    val height        get() = prefs.value.height
    val age           get() = prefs.value.age
    val isMetric      get() = prefs.value.isMetric
    val stepsGoal     get() = prefs.value.stepsGoal
    val waterGoalMl   get() = prefs.value.waterGoalMl
    val todayWaterMl  get() = prefs.value.todayWaterMl
    val todayEmotion  get() = prefs.value.todayEmotion
    val todayCalories get() = prefs.value.todayCalories

    // ──────────────────────────────────────────
    //  PERFIL
    // ──────────────────────────────────────────
    fun saveProfile(firstName: String, lastName: String, weight: String, height: String, age: String, isMetric: Boolean) {
        viewModelScope.launch { repo.saveProfile(firstName, lastName, weight, height, age, isMetric) }
    }

    // ──────────────────────────────────────────
    //  DEFINIÇÕES
    // ──────────────────────────────────────────
    fun saveSettings(
        stepsGoal: Int, waterGoalMl: Int,
        notifWater: Boolean, notifSteps: Boolean, notifMood: Boolean,
        waterFreq: String = prefs.value.waterFreq,
        moodFreq: String  = prefs.value.moodFreq,
        darkMode: Boolean = prefs.value.darkMode,
        googleLinked: Boolean = prefs.value.googleLinked
    ) {
        viewModelScope.launch {
            repo.saveSettings(
                stepsGoal, waterGoalMl, notifWater, notifSteps, notifMood,
                waterFreq, moodFreq, darkMode, googleLinked
            )
        }
    }

    // ── Guarda só o dark mode (chamado pelo toggle) ──
    fun saveDarkMode(enabled: Boolean) {
        viewModelScope.launch { repo.saveDarkMode(enabled) }
    }

    // ──────────────────────────────────────────
    //  ÁGUA & EMOÇÃO
    // ──────────────────────────────────────────
    fun addWater(ml: Int) {
        val newTotal = (prefs.value.todayWaterMl + ml).coerceAtMost(prefs.value.waterGoalMl)
        saveDailyData(waterMl = newTotal)
    }

    fun setEmotion(index: Int) {
        saveDailyData(emotion = index)
    }

    private fun saveDailyData(
        steps:    Int = prefs.value.todaySteps,
        waterMl:  Int = prefs.value.todayWaterMl,
        calories: Int = prefs.value.todayCalories,
        emotion:  Int = prefs.value.todayEmotion
    ) {
        viewModelScope.launch {
            repo.saveDailyData(
                date     = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()),
                steps    = steps,
                waterMl  = waterMl,
                calories = calories,
                emotion  = emotion
            )
        }
    }
}