package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface MoodDao {
    @Query("SELECT * FROM mood_entries ORDER BY date DESC")
    fun getAllMoods(): Flow<List<MoodEntry>>

    @Query("SELECT * FROM mood_entries WHERE date = :date LIMIT 1")
    suspend fun getMoodByDate(date: String): MoodEntry?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMood(moodEntry: MoodEntry)

    @Query("DELETE FROM mood_entries WHERE date = :date")
    suspend fun deleteMoodByDate(date: String)
}
