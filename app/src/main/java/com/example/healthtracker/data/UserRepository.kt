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
        darkMode: Boolean, googleLinked: Boolean
    ) = dataStore.saveSettings(
        stepsGoal, waterGoalMl,
        notifWater, notifSteps, notifMood,
        waterFreq, moodFreq,
        darkMode, googleLinked
    )

    suspend fun saveDarkMode(enabled: Boolean) = dataStore.saveDarkMode(enabled)

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
     * Verifica se o dia mudou. Se sim, guarda o histórico, reseta, e devolve
     * prefs limpas (zeros). Se não mudou, devolve as prefs atuais.
     *
     * IMPORTANTE: devolve sempre as prefs "frescas" prontas a usar —
     * nunca chames preferences.first() depois disto ou podes apanhar cache velha.
     */
    suspend fun checkAndResetIfNewDay(): Pair<String, UserPreferences> {
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val prefs = preferences.first()

        return when {
            prefs.todayDate.isNotEmpty() && prefs.todayDate != today -> {
                // Guarda o dia anterior no histórico
                saveEntryToHistory(
                    date     = prefs.todayDate,
                    steps    = prefs.todaySteps,
                    waterMl  = prefs.todayWaterMl,
                    calories = prefs.todayCalories,
                    emotion  = prefs.todayEmotion
                )
                // Reseta e devolve prefs zeradas — sem ir buscar ao DataStore outra vez
                resetDailyData(today)
                Pair(today, UserPreferences(todayDate = today))
            }
            prefs.todayDate.isEmpty() -> {
                // Primeira execução — inicializa
                dataStore.saveDailyData(today, 0, 0, 0, 2)
                Pair(today, UserPreferences(todayDate = today))
            }
            else -> {
                // Mesmo dia — devolve as prefs atuais sem tocar em nada
                Pair(today, prefs)
            }
        }
    }
}