package com.example.data

import kotlinx.coroutines.flow.Flow

class MoodRepository(private val moodDao: MoodDao) {
    val allMoods: Flow<List<MoodEntry>> = moodDao.getAllMoods()

    suspend fun getMoodByDate(date: String): MoodEntry? = moodDao.getMoodByDate(date)

    suspend fun insertMood(moodEntry: MoodEntry) = moodDao.insertMood(moodEntry)

    suspend fun deleteMoodByDate(date: String) = moodDao.deleteMoodByDate(date)
}
