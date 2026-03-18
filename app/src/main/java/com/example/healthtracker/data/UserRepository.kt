package com.example.healthtracker.data

import android.content.Context
import com.example.healthtracker.data.room.AppDatabase
import com.example.healthtracker.data.room.DailyEntryEntity
import kotlinx.coroutines.flow.Flow

class UserRepository(context: Context) {

    private val dataStore = UserPreferencesDataStore(context)
    private val dao       = AppDatabase.getInstance(context).dailyEntryDao()

    val preferences: Flow<UserPreferences> = dataStore.flow

    suspend fun saveProfile(firstName: String, lastName: String, weight: String, age: String) =
        dataStore.saveProfile(firstName, lastName, weight, age)

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

    suspend fun resetDailyData(newDate: String) =
        dataStore.resetDailyData(newDate)

    fun getToday(date: String) = dao.getByDate(date)
    fun getLast30Days()        = dao.getLast30Days()
}