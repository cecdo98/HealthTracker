package com.example.healthtracker.data

import android.content.Context
import androidx.datastore.core.IOException
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.*

val Context.appDataStore by preferencesDataStore(name = "health_prefs")

// ─────────────────────────────────────────────
//  MODELO DE DADOS
// ─────────────────────────────────────────────
data class UserPreferences(
    // Perfil
    val firstName: String = "",
    val lastName:  String = "",
    val weight:    String = "",
    val height:    String = "",
    val age:       String = "",
    val isMetric:  Boolean = true, // true: kg/cm, false: lbs/inches

    // Metas diárias
    val stepsGoal:   Int = 10000,
    val waterGoalMl: Int = 2500,

    // Notificações
    val notifWater: Boolean = false,
    val notifSteps: Boolean = false,
    val notifMood:  Boolean = false,

    // Frequências de notificação
    val waterFreq: String = "1h",
    val moodFreq:  String = "1h",

    // Preferências visuais
    val darkMode:     Boolean = false,
    val googleLinked: Boolean = false,

    // Dados diários
    val todayDate:     String = "",
    val todaySteps:    Int    = 0,
    val todayWaterMl:  Int    = 0,
    val todayCalories: Int    = 0,
    val todayEmotion:  Int    = 2
)

// ─────────────────────────────────────────────
//  CHAVES
// ─────────────────────────────────────────────
object PrefsKeys {
    // Perfil
    val FIRST_NAME = stringPreferencesKey("first_name")
    val LAST_NAME  = stringPreferencesKey("last_name")
    val WEIGHT     = stringPreferencesKey("weight")
    val HEIGHT     = stringPreferencesKey("height")
    val AGE        = stringPreferencesKey("age")
    val IS_METRIC  = booleanPreferencesKey("is_metric")
    // Metas
    val STEPS_GOAL = intPreferencesKey("steps_goal")
    val WATER_GOAL = intPreferencesKey("water_goal_ml")
    // Notificações
    val NOTIF_WATER = booleanPreferencesKey("notif_water")
    val NOTIF_STEPS = booleanPreferencesKey("notif_steps")
    val NOTIF_MOOD  = booleanPreferencesKey("notif_mood")
    // Frequências
    val WATER_FREQ = stringPreferencesKey("water_freq")
    val MOOD_FREQ  = stringPreferencesKey("mood_freq")
    // Preferências visuais
    val DARK_MODE     = booleanPreferencesKey("dark_mode")
    val GOOGLE_LINKED = booleanPreferencesKey("google_linked")
    // Dados diários
    val TODAY_DATE     = stringPreferencesKey("today_date")
    val TODAY_STEPS    = intPreferencesKey("today_steps")
    val TODAY_WATER_ML = intPreferencesKey("today_water_ml")
    val TODAY_CALORIES = intPreferencesKey("today_calories")
    val TODAY_EMOTION  = intPreferencesKey("today_emotion")
}

// ─────────────────────────────────────────────
//  DATA STORE
// ─────────────────────────────────────────────
class UserPreferencesDataStore(private val context: Context) {

    val flow: Flow<UserPreferences> = context.appDataStore.data
        .catch { if (it is IOException) emit(emptyPreferences()) else throw it }
        .map { p ->
            UserPreferences(
                firstName    = p[PrefsKeys.FIRST_NAME]    ?: "",
                lastName     = p[PrefsKeys.LAST_NAME]     ?: "",
                weight       = p[PrefsKeys.WEIGHT]        ?: "",
                height       = p[PrefsKeys.HEIGHT]        ?: "",
                age          = p[PrefsKeys.AGE]           ?: "",
                isMetric     = p[PrefsKeys.IS_METRIC]     ?: true,
                stepsGoal    = p[PrefsKeys.STEPS_GOAL]    ?: 10000,
                waterGoalMl  = p[PrefsKeys.WATER_GOAL]    ?: 2500,
                notifWater   = p[PrefsKeys.NOTIF_WATER]   ?: false,
                notifSteps   = p[PrefsKeys.NOTIF_STEPS]   ?: false,
                notifMood    = p[PrefsKeys.NOTIF_MOOD]    ?: false,
                waterFreq    = p[PrefsKeys.WATER_FREQ]    ?: "1h",
                moodFreq     = p[PrefsKeys.MOOD_FREQ]     ?: "1h",
                darkMode     = p[PrefsKeys.DARK_MODE]     ?: false,
                googleLinked = p[PrefsKeys.GOOGLE_LINKED] ?: false,
                todayDate    = p[PrefsKeys.TODAY_DATE]    ?: "",
                todaySteps   = p[PrefsKeys.TODAY_STEPS]   ?: 0,
                todayWaterMl = p[PrefsKeys.TODAY_WATER_ML]?: 0,
                todayCalories= p[PrefsKeys.TODAY_CALORIES]?: 0,
                todayEmotion = p[PrefsKeys.TODAY_EMOTION] ?: 2
            )
        }

    suspend fun saveProfile(firstName: String, lastName: String, weight: String, height: String, age: String, isMetric: Boolean) {
        context.appDataStore.edit { p ->
            p[PrefsKeys.FIRST_NAME] = firstName
            p[PrefsKeys.LAST_NAME]  = lastName
            p[PrefsKeys.WEIGHT]     = weight
            p[PrefsKeys.HEIGHT]     = height
            p[PrefsKeys.AGE]        = age
            p[PrefsKeys.IS_METRIC]  = isMetric
        }
    }

    suspend fun saveSettings(
        stepsGoal: Int, waterGoalMl: Int,
        notifWater: Boolean, notifSteps: Boolean, notifMood: Boolean,
        waterFreq: String, moodFreq: String,
        darkMode: Boolean, googleLinked: Boolean
    ) {
        context.appDataStore.edit { p ->
            p[PrefsKeys.STEPS_GOAL]    = stepsGoal
            p[PrefsKeys.WATER_GOAL]    = waterGoalMl
            p[PrefsKeys.NOTIF_WATER]   = notifWater
            p[PrefsKeys.NOTIF_STEPS]   = notifSteps
            p[PrefsKeys.NOTIF_MOOD]    = notifMood
            p[PrefsKeys.WATER_FREQ]    = waterFreq
            p[PrefsKeys.MOOD_FREQ]     = moodFreq
            p[PrefsKeys.DARK_MODE]     = darkMode
            p[PrefsKeys.GOOGLE_LINKED] = googleLinked
        }
    }

    suspend fun saveDarkMode(enabled: Boolean) {
        context.appDataStore.edit { p ->
            p[PrefsKeys.DARK_MODE] = enabled
        }
    }

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