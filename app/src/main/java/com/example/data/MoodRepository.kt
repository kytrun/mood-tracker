package com.example.data

import kotlinx.coroutines.flow.Flow

class MoodRepository(private val moodDao: MoodDao) {
    val allMoods: Flow<List<MoodEntry>> = moodDao.getAllMoods()

    fun getMoodsByDate(date: String): Flow<List<MoodEntry>> = moodDao.getMoodsByDate(date)

    suspend fun getMoodById(id: Long): MoodEntry? = moodDao.getMoodById(id)

    suspend fun insertMood(moodEntry: MoodEntry): Long = moodDao.insertMood(moodEntry)

    suspend fun deleteMoodById(id: Long) = moodDao.deleteMoodById(id)
}
