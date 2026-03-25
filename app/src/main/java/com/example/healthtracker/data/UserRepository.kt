package com.example.healthtracker.data

import android.content.Context
import com.example.healthtracker.data.room.AppDatabase
import com.example.healthtracker.data.room.DailyEntryEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class UserRepository(context: Context) {

    private val dataStore = UserPreferencesDataStore(context)
    private val dao       = AppDatabase.getInstance(context).dailyEntryDao()

    val allEntries: Flow<List<DailyEntryEntity>> = AppDatabase.getInstance(context).dailyEntryDao().getAllEntries()

    val preferences: Flow<UserPreferences> = dataStore.flow

    suspend fun saveProfile(firstName: String, lastName: String, weight: String, height: String, age: String, isMetric: Boolean) =
        dataStore.saveProfile(firstName, lastName, weight, height, age, isMetric)

    suspend fun saveProfilePicture(uri: String?) =
        dataStore.saveProfilePicture(uri)

    suspend fun saveSettings(
        stepsGoal: Int, waterGoalMl: Int,
        notifWater: Boolean, notifSteps: Boolean, notifMood: Boolean,
        waterFreq: String, moodFreq: String,
        darkMode: Boolean, googleLinked: Boolean
    ) = dataStore.saveSettings(
        stepsGoal, waterGoalMl, notifWater, notifSteps, notifMood,
        waterFreq, moodFreq, darkMode, googleLinked
    )

    suspend fun saveDarkMode(enabled: Boolean) =
        dataStore.saveDarkMode(enabled)

    /**
     * Guarda os dados diários no DataStore e no Room.
     */
    suspend fun saveDailyData(
        date: String, steps: Int, waterMl: Int, calories: Int, emotion: Int
    ) {
        dataStore.saveDailyData(date, steps, waterMl, calories, emotion)
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
     * Verifica se o dia mudou e reseta os dados se necessário.
     * Retorna a data atual ("yyyy-MM-dd").
     */
    suspend fun checkAndResetIfNewDay(): String {
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val prefs = preferences.first()
        
        if (prefs.todayDate.isNotEmpty() && prefs.todayDate != today) {
            // Antes de resetar, garantimos que o dia anterior está bem guardado no Room
            // (Embora saveDailyData já o faça incrementalmente, isto é uma segurança extra)
            dao.upsert(
                DailyEntryEntity(
                    date         = prefs.todayDate,
                    steps        = prefs.todaySteps,
                    waterMl      = prefs.todayWaterMl,
                    calories     = prefs.todayCalories,
                    emotionIndex = prefs.todayEmotion
                )
            )
            
            // Reset no DataStore para o novo dia
            resetDailyData(today)
        }
        return today
    }

    suspend fun resetDailyData(newDate: String) =
        dataStore.resetDailyData(newDate)

    suspend fun saveDarkModeSync(enabled: Boolean) = dataStore.saveDarkMode(enabled)

    fun getToday(date: String) = dao.getByDate(date)
    fun getLast30Days()        = dao.getLast30Days()
}