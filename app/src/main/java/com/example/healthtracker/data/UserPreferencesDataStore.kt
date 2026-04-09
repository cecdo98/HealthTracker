package com.example.healthtracker.data

import android.content.Context
import androidx.datastore.core.IOException
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.*

val Context.appDataStore by preferencesDataStore(name = "health_prefs")

data class UserPreferences(
    val firstName: String = "",
    val lastName:  String = "",
    val weight:    String = "",
    val height:    String = "",
    val age:       String = "",
    val isMetric:  Boolean = true,
    val profilePictureUri: String? = null,
    val stepsGoal:   Int = 10000,
    val waterGoalMl: Int = 2500,
    val notifWater: Boolean = false,
    val notifSteps: Boolean = false,
    val notifMood:  Boolean = false,
    val waterFreq: String = "1h",
    val moodFreq:  String = "1h",
    val darkMode:     Boolean = false,
    val googleLinked: Boolean = false,
    val todayDate:     String = "",
    val todaySteps:    Int    = 0,
    val todayWaterMl:  Int    = 0,
    val todayCalories: Int    = 0,
    val todayEmotion:  Int    = 2,
    val stepsSensorBase: Int  = -1
)

object PrefsKeys {
    val FIRST_NAME = stringPreferencesKey("first_name")
    val LAST_NAME  = stringPreferencesKey("last_name")
    val WEIGHT     = stringPreferencesKey("weight")
    val HEIGHT     = stringPreferencesKey("height")
    val AGE        = stringPreferencesKey("age")
    val IS_METRIC  = booleanPreferencesKey("is_metric")
    val PROFILE_PICTURE_URI = stringPreferencesKey("profile_picture_uri")
    val STEPS_GOAL = intPreferencesKey("steps_goal")
    val WATER_GOAL = intPreferencesKey("water_goal_ml")
    val NOTIF_WATER = booleanPreferencesKey("notif_water")
    val NOTIF_STEPS = booleanPreferencesKey("notif_steps")
    val NOTIF_MOOD  = booleanPreferencesKey("notif_mood")
    val WATER_FREQ = stringPreferencesKey("water_freq")
    val MOOD_FREQ  = stringPreferencesKey("mood_freq")
    val DARK_MODE     = booleanPreferencesKey("dark_mode")
    val GOOGLE_LINKED = booleanPreferencesKey("google_linked")
    val TODAY_DATE     = stringPreferencesKey("today_date")
    val TODAY_STEPS    = intPreferencesKey("today_steps")
    val TODAY_WATER_ML = intPreferencesKey("today_water_ml")
    val TODAY_CALORIES = intPreferencesKey("today_calories")
    val TODAY_EMOTION  = intPreferencesKey("today_emotion")
    val STEPS_SENSOR_BASE = intPreferencesKey("steps_sensor_base")
}

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
                profilePictureUri = p[PrefsKeys.PROFILE_PICTURE_URI],
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
                todayEmotion = p[PrefsKeys.TODAY_EMOTION] ?: 2,
                stepsSensorBase = p[PrefsKeys.STEPS_SENSOR_BASE] ?: -1
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

    suspend fun saveProfilePicture(uri: String?) {
        context.appDataStore.edit { p ->
            if (uri != null) p[PrefsKeys.PROFILE_PICTURE_URI] = uri
            else p.remove(PrefsKeys.PROFILE_PICTURE_URI)
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
        context.appDataStore.edit { p -> p[PrefsKeys.DARK_MODE] = enabled }
    }

    suspend fun saveDailyData(
        date: String, steps: Int, waterMl: Int, calories: Int, emotion: Int, sensorBase: Int = -1
    ) {
        context.appDataStore.edit { p ->
            p[PrefsKeys.TODAY_DATE]     = date
            p[PrefsKeys.TODAY_STEPS]    = steps
            p[PrefsKeys.TODAY_WATER_ML] = waterMl
            p[PrefsKeys.TODAY_CALORIES] = calories
            p[PrefsKeys.TODAY_EMOTION]  = emotion
            if (sensorBase != -1) p[PrefsKeys.STEPS_SENSOR_BASE] = sensorBase
        }
    }

    suspend fun resetDailyData(newDate: String) {
        context.appDataStore.edit { p ->
            p[PrefsKeys.TODAY_DATE]        = newDate
            p[PrefsKeys.TODAY_STEPS]       = 0
            p[PrefsKeys.TODAY_WATER_ML]    = 0
            p[PrefsKeys.TODAY_CALORIES]    = 0
            p[PrefsKeys.TODAY_EMOTION]     = 2
            p[PrefsKeys.STEPS_SENSOR_BASE] = -1
        }
    }

    /**
     * Operação atómica: se a data guardada for diferente de [today], reseta tudo
     * e adiciona [addWaterMl] ao novo dia (que começa em 0).
     * Se for o mesmo dia, soma [addWaterMl] ao valor atual.
     * Devolve o novo total de água.
     */
    suspend fun atomicAddWater(today: String, addWaterMl: Int): Int {
        var newTotal = 0
        context.appDataStore.edit { p ->
            val storedDate = p[PrefsKeys.TODAY_DATE] ?: ""
            if (storedDate != today) {
                // Novo dia — reseta tudo primeiro
                p[PrefsKeys.TODAY_DATE]        = today
                p[PrefsKeys.TODAY_STEPS]       = 0
                p[PrefsKeys.TODAY_WATER_ML]    = addWaterMl
                p[PrefsKeys.TODAY_CALORIES]    = 0
                p[PrefsKeys.TODAY_EMOTION]     = 2
                p[PrefsKeys.STEPS_SENSOR_BASE] = -1
                newTotal = addWaterMl
            } else {
                // Mesmo dia — soma
                val current = p[PrefsKeys.TODAY_WATER_ML] ?: 0
                newTotal = current + addWaterMl
                p[PrefsKeys.TODAY_WATER_ML] = newTotal
            }
        }
        return newTotal
    }

    /**
     * Operação atómica: se a data guardada for diferente de [today], reseta tudo
     * e guarda [emotion] para o novo dia.
     * Se for o mesmo dia, atualiza apenas o humor.
     */
    suspend fun atomicSetEmotion(today: String, emotion: Int) {
        context.appDataStore.edit { p ->
            val storedDate = p[PrefsKeys.TODAY_DATE] ?: ""
            if (storedDate != today) {
                // Novo dia — reseta tudo primeiro
                p[PrefsKeys.TODAY_DATE]        = today
                p[PrefsKeys.TODAY_STEPS]       = 0
                p[PrefsKeys.TODAY_WATER_ML]    = 0
                p[PrefsKeys.TODAY_CALORIES]    = 0
                p[PrefsKeys.TODAY_EMOTION]     = emotion
                p[PrefsKeys.STEPS_SENSOR_BASE] = -1
            } else {
                // Mesmo dia — atualiza só o humor
                p[PrefsKeys.TODAY_EMOTION] = emotion
            }
        }
    }
}