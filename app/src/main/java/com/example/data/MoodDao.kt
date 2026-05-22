package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface MoodDao {
    @Query("SELECT * FROM mood_entries ORDER BY date DESC, time DESC")
    fun getAllMoods(): Flow<List<MoodEntry>>

    @Query("SELECT * FROM mood_entries WHERE date = :date ORDER BY time DESC")
    fun getMoodsByDate(date: String): Flow<List<MoodEntry>>

    @Query("SELECT * FROM mood_entries WHERE id = :id LIMIT 1")
    suspend fun getMoodById(id: Long): MoodEntry?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMood(moodEntry: MoodEntry): Long

    @Query("DELETE FROM mood_entries WHERE id = :id")
    suspend fun deleteMoodById(id: Long)
}
