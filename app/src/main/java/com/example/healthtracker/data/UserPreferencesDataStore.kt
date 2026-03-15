package com.example.healthtracker.data

import android.content.Context
import androidx.datastore.core.IOException
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.*

// ─────────────────────────────────────────────
//  EXTENSÃO — um único DataStore por processo
// ─────────────────────────────────────────────
val Context.appDataStore by preferencesDataStore(name = "health_prefs")

// ─────────────────────────────────────────────
//  MODELO DE DADOS
// ─────────────────────────────────────────────
data class UserPreferences(
    // Perfil
    val firstName: String = "",
    val lastName:  String = "",
    val weight:    String = "",
    val age:       String = "",
    // Definições — metas diárias
    val stepsGoal: Int   = 10000,
    val waterGoalMl: Int = 2500,
    // Definições — notificações
    val notifWater:    Boolean = true,
    val notifSteps:    Boolean = true,
    val notifMood:     Boolean = false,
    // Dados diários (guardados por dia)
    val todayDate:     String = "",
    val todaySteps:    Int    = 0,
    val todayWaterMl:  Int    = 0,
    val todayCalories: Int    = 0,
    val todayEmotion:  Int    = 2   // 0=MuitoBem … 4=Estressado
)

// ─────────────────────────────────────────────
//  DATA STORE — chaves + operações
// ─────────────────────────────────────────────
object PrefsKeys {
    // Perfil
    val FIRST_NAME    = stringPreferencesKey("first_name")
    val LAST_NAME     = stringPreferencesKey("last_name")
    val WEIGHT        = stringPreferencesKey("weight")
    val AGE           = stringPreferencesKey("age")
    // Metas
    val STEPS_GOAL    = intPreferencesKey("steps_goal")
    val WATER_GOAL    = intPreferencesKey("water_goal_ml")
    // Notificações
    val NOTIF_WATER   = booleanPreferencesKey("notif_water")
    val NOTIF_STEPS   = booleanPreferencesKey("notif_steps")
    val NOTIF_MOOD    = booleanPreferencesKey("notif_mood")
    // Dados diários
    val TODAY_DATE     = stringPreferencesKey("today_date")
    val TODAY_STEPS    = intPreferencesKey("today_steps")
    val TODAY_WATER_ML = intPreferencesKey("today_water_ml")
    val TODAY_CALORIES = intPreferencesKey("today_calories")
    val TODAY_EMOTION  = intPreferencesKey("today_emotion")
}

class UserPreferencesDataStore(private val context: Context) {

    val flow: Flow<UserPreferences> = context.appDataStore.data
        .catch { if (it is IOException) emit(emptyPreferences()) else throw it }
        .map { p ->
            UserPreferences(
                firstName    = p[PrefsKeys.FIRST_NAME]    ?: "",
                lastName     = p[PrefsKeys.LAST_NAME]     ?: "",
                weight       = p[PrefsKeys.WEIGHT]        ?: "",
                age          = p[PrefsKeys.AGE]           ?: "",
                stepsGoal    = p[PrefsKeys.STEPS_GOAL]    ?: 10000,
                waterGoalMl  = p[PrefsKeys.WATER_GOAL]    ?: 2500,
                notifWater   = p[PrefsKeys.NOTIF_WATER]   ?: true,
                notifSteps   = p[PrefsKeys.NOTIF_STEPS]   ?: true,
                notifMood    = p[PrefsKeys.NOTIF_MOOD]    ?: false,
                todayDate    = p[PrefsKeys.TODAY_DATE]    ?: "",
                todaySteps   = p[PrefsKeys.TODAY_STEPS]   ?: 0,
                todayWaterMl = p[PrefsKeys.TODAY_WATER_ML]?: 0,
                todayCalories= p[PrefsKeys.TODAY_CALORIES]?: 0,
                todayEmotion = p[PrefsKeys.TODAY_EMOTION] ?: 2
            )
        }

    // ── Perfil ──
    suspend fun saveProfile(firstName: String, lastName: String, weight: String, age: String) {
        context.appDataStore.edit { p ->
            p[PrefsKeys.FIRST_NAME] = firstName
            p[PrefsKeys.LAST_NAME]  = lastName
            p[PrefsKeys.WEIGHT]     = weight
            p[PrefsKeys.AGE]        = age
        }
    }

    // ── Definições ──
    suspend fun saveSettings(
        stepsGoal: Int, waterGoalMl: Int,
        notifWater: Boolean, notifSteps: Boolean, notifMood: Boolean
    ) {
        context.appDataStore.edit { p ->
            p[PrefsKeys.STEPS_GOAL]  = stepsGoal
            p[PrefsKeys.WATER_GOAL]  = waterGoalMl
            p[PrefsKeys.NOTIF_WATER] = notifWater
            p[PrefsKeys.NOTIF_STEPS] = notifSteps
            p[PrefsKeys.NOTIF_MOOD]  = notifMood
        }
    }

    // ── Dados diários ──
    suspend fun saveDailyData(
        date: String, steps: Int, waterMl: Int, calories: Int, emotion: Int
    ) {
        context.appDataStore.edit { p ->
            p[PrefsKeys.TODAY_DATE]     = date
            p[PrefsKeys.TODAY_STEPS]    = steps
            p[PrefsKeys.TODAY_WATER_ML] = waterMl
            p[PrefsKeys.TODAY_CALORIES] = calories
            p[PrefsKeys.TODAY_EMOTION]  = emotion
        }
    }

    // ── Reset diário (chamado quando muda o dia) ──
    suspend fun resetDailyData(newDate: String) {
        context.appDataStore.edit { p ->
            p[PrefsKeys.TODAY_DATE]     = newDate
            p[PrefsKeys.TODAY_STEPS]    = 0
            p[PrefsKeys.TODAY_WATER_ML] = 0
            p[PrefsKeys.TODAY_CALORIES] = 0
            p[PrefsKeys.TODAY_EMOTION]  = 2
        }
    }
}