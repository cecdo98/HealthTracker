package com.example.healthtracker.data.room

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface DailyEntryDao {

    // Guarda ou atualiza o dia
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entry: DailyEntryEntity)

    // Lê um dia específico
    @Query("SELECT * FROM daily_entries WHERE date = :date LIMIT 1")
    fun getByDate(date: String): Flow<DailyEntryEntity?>

    // Histórico dos últimos 30 dias
    @Query("SELECT * FROM daily_entries ORDER BY date DESC LIMIT 30")
    fun getLast30Days(): Flow<List<DailyEntryEntity>>

    // Apaga registos antigos (mais de 30 dias)
    @Query("DELETE FROM daily_entries WHERE date < :cutoff")
    suspend fun deleteOlderThan(cutoff: String)
}

