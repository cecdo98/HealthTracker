package com.example.healthtracker.data

import android.content.Context
import kotlinx.coroutines.flow.Flow

class UserRepository(context: Context) {

    private val dataStore = UserPreferencesDataStore(context)

    val preferences: Flow<UserPreferences> = dataStore.flow

    suspend fun saveProfile(firstName: String, lastName: String, weight: String, age: String) =
        dataStore.saveProfile(firstName, lastName, weight, age)

    suspend fun saveSettings(
        stepsGoal: Int, waterGoalMl: Int,
        notifWater: Boolean, notifSteps: Boolean, notifMood: Boolean
    ) = dataStore.saveSettings(stepsGoal, waterGoalMl, notifWater, notifSteps, notifMood)

    suspend fun saveDailyData(
        date: String, steps: Int, waterMl: Int, calories: Int, emotion: Int
    ) = dataStore.saveDailyData(date, steps, waterMl, calories, emotion)

    suspend fun resetDailyData(newDate: String) =
        dataStore.resetDailyData(newDate)
}