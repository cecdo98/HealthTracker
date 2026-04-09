package com.example.healthtracker.data

import android.content.Context
import com.example.healthtracker.data.room.AppDatabase
import com.example.healthtracker.data.room.DailyEntryDao
import com.example.healthtracker.data.room.DailyEntryEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class UserRepository(context: Context) {

    private val dataStore = UserPreferencesDataStore(context)
    private val dao: DailyEntryDao = AppDatabase.getInstance(context).dailyEntryDao()

    val preferences: Flow<UserPreferences> = dataStore.flow
    val allEntries: Flow<List<DailyEntryEntity>> = dao.getAllEntries()

    private fun today() = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

    // ── Perfil ────────────────────────────────────────────────────────────────

    suspend fun saveProfile(
        firstName: String, lastName: String,
        weight: String, height: String,
        age: String, isMetric: Boolean
    ) = dataStore.saveProfile(firstName, lastName, weight, height, age, isMetric)

    suspend fun saveProfilePicture(uri: String?) = dataStore.saveProfilePicture(uri)

    // ── Definições ────────────────────────────────────────────────────────────

    suspend fun saveSettings(
        stepsGoal: Int, waterGoalMl: Int,
        notifWater: Boolean, notifSteps: Boolean, notifMood: Boolean,
        waterFreq: String, moodFreq: String,
        darkMode: Boolean, animationsEnabled: Boolean, hapticEnabled: Boolean
    ) = dataStore.saveSettings(
        stepsGoal, waterGoalMl,
        notifWater, notifSteps, notifMood,
        waterFreq, moodFreq,
        darkMode, animationsEnabled, hapticEnabled
    )

    suspend fun saveDarkMode(enabled: Boolean) = dataStore.saveDarkMode(enabled)
    suspend fun saveHapticEnabled(enabled: Boolean) = dataStore.saveHapticEnabled(enabled)
    suspend fun saveAnimationsEnabled(enabled: Boolean) = dataStore.saveAnimationsEnabled(enabled)

    // ── Dados diários ─────────────────────────────────────────────────────────

    suspend fun saveDailyData(
        date: String, steps: Int, waterMl: Int,
        calories: Int, emotion: Int, sensorBase: Int = -1
    ) = dataStore.saveDailyData(date, steps, waterMl, calories, emotion, sensorBase)

    suspend fun resetDailyData(newDate: String) = dataStore.resetDailyData(newDate)

    suspend fun saveEntryToHistory(
        date: String, steps: Int, waterMl: Int, calories: Int, emotion: Int
    ) {
        dao.upsert(
            DailyEntryEntity(
                date         = date,
                steps        = steps,
                waterMl      = waterMl,
                calories     = calories,
                emotionIndex = emotion
            )
        )
    }

    /**
     * Verifica se o dia mudou e reseta se necessário.
     * Usado no arranque da app e no watcher de meia-noite.
     */
    suspend fun checkAndResetIfNewDay() {
        val today = today()
        val prefs = preferences.first()

        when {
            prefs.todayDate.isNotEmpty() && prefs.todayDate != today -> {
                saveEntryToHistory(
                    date     = prefs.todayDate,
                    steps    = prefs.todaySteps,
                    waterMl  = prefs.todayWaterMl,
                    calories = prefs.todayCalories,
                    emotion  = prefs.todayEmotion
                )
                resetDailyData(today)
            }
            prefs.todayDate.isEmpty() -> {
                dataStore.saveDailyData(today, 0, 0, 0, 2)
            }
        }
    }

    /**
     * Adiciona água de forma atómica — o reset do dia e a adição são
     * uma operação única no DataStore, sem risco de race condition.
     * Se for novo dia, guarda o histórico de ontem antes de resetar.
     */
    suspend fun addWaterAtomic(ml: Int) {
        val today = today()
        val prefs = preferences.first()

        // Se é novo dia, guarda o histórico de ontem primeiro
        if (prefs.todayDate.isNotEmpty() && prefs.todayDate != today) {
            saveEntryToHistory(
                date     = prefs.todayDate,
                steps    = prefs.todaySteps,
                waterMl  = prefs.todayWaterMl,
                calories = prefs.todayCalories,
                emotion  = prefs.todayEmotion
            )
        }

        // O atomicAddWater verifica a data internamente e reseta+adiciona num único edit{}
        dataStore.atomicAddWater(today, ml)
    }

    /**
     * Define o humor de forma atómica — o reset do dia e a escrita do humor são
     * uma operação única no DataStore, sem risco de race condition.
     * Se for novo dia, guarda o histórico de ontem antes de resetar.
     */
    suspend fun setEmotionAtomic(emotion: Int) {
        val today = today()
        val prefs = preferences.first()

        // Se é novo dia, guarda o histórico de ontem primeiro
        if (prefs.todayDate.isNotEmpty() && prefs.todayDate != today) {
            saveEntryToHistory(
                date     = prefs.todayDate,
                steps    = prefs.todaySteps,
                waterMl  = prefs.todayWaterMl,
                calories = prefs.todayCalories,
                emotion  = prefs.todayEmotion
            )
        }

        // O atomicSetEmotion verifica a data internamente e reseta+escreve num único edit{}
        dataStore.atomicSetEmotion(today, emotion)
    }
}
