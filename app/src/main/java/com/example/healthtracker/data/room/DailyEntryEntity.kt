package com.example.healthtracker.data.room

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "daily_entries")
data class DailyEntryEntity(
    @PrimaryKey
    val date: String,        // "2025-06-16" — chave única por dia
    val steps: Int = 0,
    val waterMl: Int = 0,
    val calories: Int = 0,
    val emotionIndex: Int = 2
)