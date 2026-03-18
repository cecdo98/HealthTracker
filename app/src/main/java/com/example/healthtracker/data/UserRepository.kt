package com.example.healthtracker.data

import android.content.Context
import com.example.healthtracker.data.room.AppDatabase
import com.example.healthtracker.data.room.DailyEntryEntity
import kotlinx.coroutines.flow.Flow

class UserRepository(context: Context) {

    private val dataStore = UserPreferencesDataStore(context)
    private val dao       = AppDatabase.getInstance(context).dailyEntryDao()  // ← novo

    val preferences: Flow<UserPreferences> = dataStore.flow

    // ── DataStore — perfil e definições (igual ao que já tens) ──
    suspend fun saveProfile(firstName: String, lastName: String, weight: String, age: String) =
        dataStore.saveProfile(firstName, lastName, weight, age)

    suspend fun saveSettings(
        stepsGoal: Int, waterGoalMl: Int,
        notifWater: Boolean, notifSteps: Boolean, notifMood: Boolean
    ) = dataStore.saveSettings(stepsGoal, waterGoalMl, notifWater, notifSteps, notifMood)

    // ── DataStore — dados do dia atual (igual ao que já tens) ──
    suspend fun saveDailyData(
        date: String, steps: Int, waterMl: Int, calories: Int, emotion: Int
    ) {
        // Guarda no DataStore (dia atual — acesso rápido)
        dataStore.saveDailyData(date, steps, waterMl, calories, emotion)

        // Guarda no Room (histórico permanente)
        dao.upsert(
            DailyEntryEntity(
                date = date,
                steps = steps,
                waterMl = waterMl,
                calories = calories,
                emotionIndex = emotion
            )
        )
    }

    suspend fun resetDailyData(newDate: String) =
        dataStore.resetDailyData(newDate)


    // ── Room — histórico ──
    fun getToday(date: String) = dao.getByDate(date)

    fun getLast30Days() = dao.getLast30Days()
}
