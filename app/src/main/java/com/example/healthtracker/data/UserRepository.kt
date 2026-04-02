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
     * Agora passa corretamente o sensorBase para persistência.
     */
    suspend fun saveDailyData(
        date: String, steps: Int, waterMl: Int, calories: Int, emotion: Int, sensorBase: Int? = null
    ) {
        // CORREÇÃO: Passar o sensorBase para o dataStore
        dataStore.saveDailyData(date, steps, waterMl, calories, emotion, sensorBase ?: -1)
        
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

    suspend fun checkAndResetIfNewDay(): String {
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val prefs = preferences.first()
        
        if (prefs.todayDate.isNotEmpty() && prefs.todayDate != today) {
            dao.upsert(
                DailyEntryEntity(
                    date         = prefs.todayDate,
                    steps        = prefs.todaySteps,
                    waterMl      = prefs.todayWaterMl,
                    calories     = prefs.todayCalories,
                    emotionIndex = prefs.todayEmotion
                )
            )
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
