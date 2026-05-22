package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "mood_entries")
data class MoodEntry(
    @PrimaryKey val date: String, // format: "yyyy-MM-dd"
    val rating: Int,             // 1 to 5 (1 = Terrible, 2 = Sad, 3 = Neutral, 4 = Good, 5 = Awesome)
    val note: String,
    val tags: String,            // comma-separated tags
    val timestamp: Long = System.currentTimeMillis()
)
