package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.MoodDatabase
import com.example.data.MoodEntry
import com.example.data.MoodRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class MoodViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: MoodRepository
    
    init {
        val database = MoodDatabase.getDatabase(application)
        repository = MoodRepository(database.moodDao())
    }

    val allMoods: StateFlow<List<MoodEntry>> = repository.allMoods
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    private val _selectedDate = MutableStateFlow(getTodayDateString())
    val selectedDate: StateFlow<String> = _selectedDate.asStateFlow()

    private val _rating = MutableStateFlow(0)
    val rating: StateFlow<Int> = _rating.asStateFlow()

    private val _note = MutableStateFlow("")
    val note: StateFlow<String> = _note.asStateFlow()

    private val _selectedTags = MutableStateFlow(setOf<String>())
    val selectedTags: StateFlow<Set<String>> = _selectedTags.asStateFlow()

    // Map to lookup mood entries quickly by date
    val moodsByDateMap: StateFlow<Map<String, MoodEntry>> = allMoods
        .combine(allMoods) { moods, _ -> moods.associateBy { it.date } }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyMap()
        )

    val streakCount: StateFlow<Int> = allMoods
        .combine(moodsByDateMap) { _, moodsMap ->
            calculateStreak(moodsMap.keys)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0
        )

    private fun calculateStreak(loggedDates: Set<String>): Int {
        if (loggedDates.isEmpty()) return 0
        val cal = Calendar.getInstance()
        var streak = 0
        
        val todayStr = dateFormat.format(cal.time)
        cal.add(Calendar.DAY_OF_YEAR, -1)
        val yesterdayStr = dateFormat.format(cal.time)
        
        // If neither today nor yesterday is logged, the streak is currently 0
        if (!loggedDates.contains(todayStr) && !loggedDates.contains(yesterdayStr)) {
            return 0
        }
        
        // Start counting back from today
        cal.time = Calendar.getInstance().time
        while (true) {
            val dateStr = dateFormat.format(cal.time)
            if (loggedDates.contains(dateStr)) {
                streak++
                cal.add(Calendar.DAY_OF_YEAR, -1)
            } else {
                // If today is not logged but yesterday was, skip today and check yesterday (which starts our streak from yesterday)
                if (dateStr == todayStr) {
                    cal.add(Calendar.DAY_OF_YEAR, -1)
                    continue
                }
                break
            }
        }
        return streak
    }

    init {
        loadMoodForDate(getTodayDateString())
    }

    fun getTodayDateString(): String {
        return dateFormat.format(Calendar.getInstance().time)
    }

    fun getYesterdayDateString(): String {
        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_YEAR, -1)
        return dateFormat.format(cal.time)
    }

    fun formatDisplayDate(dateStr: String): String {
        return try {
            val date = dateFormat.parse(dateStr)
            val displayFormat = SimpleDateFormat("yyyy年M月d日 EEEE", Locale.CHINESE)
            displayFormat.format(date ?: Calendar.getInstance().time)
        } catch (e: Exception) {
            dateStr
        }
    }

    fun selectDate(date: String) {
        _selectedDate.value = date
        loadMoodForDate(date)
    }

    private fun loadMoodForDate(date: String) {
        viewModelScope.launch {
            val entry = repository.getMoodByDate(date)
            if (entry != null) {
                _rating.value = entry.rating
                _note.value = entry.note
                _selectedTags.value = if (entry.tags.isNotEmpty()) {
                    entry.tags.split(",").toSet()
                } else {
                    emptySet()
                }
            } else {
                _rating.value = 0
                _note.value = ""
                _selectedTags.value = emptySet()
            }
        }
    }

    fun setRating(rate: Int) {
        _rating.value = rate
    }

    fun setNote(text: String) {
        _note.value = text
    }

    fun toggleTag(tag: String) {
        val current = _selectedTags.value.toMutableSet()
        if (current.contains(tag)) {
            current.remove(tag)
        } else {
            current.add(tag)
        }
        _selectedTags.value = current
    }

    fun saveCurrentMood() {
        val date = _selectedDate.value
        val rate = _rating.value
        val noteText = _note.value
        val tagsStr = _selectedTags.value.joinToString(",")
        
        if (rate == 0) return

        viewModelScope.launch {
            val entry = MoodEntry(
                date = date,
                rating = rate,
                note = noteText,
                tags = tagsStr
            )
            repository.insertMood(entry)
        }
    }

    fun deleteMoodForSelectedDate() {
        val date = _selectedDate.value
        viewModelScope.launch {
            repository.deleteMoodByDate(date)
            _rating.value = 0
            _note.value = ""
            _selectedTags.value = emptySet()
        }
    }

    // Generates a list of completed weeks (Sunday to Saturday) covering 26 weeks
    fun generateHeatmapDates(): List<String> {
        val datesList = mutableListOf<String>()
        val cal = Calendar.getInstance()
        
        val currentDayOfWeek = cal.get(Calendar.DAY_OF_WEEK)
        val daysUntilSaturday = Calendar.SATURDAY - currentDayOfWeek
        cal.add(Calendar.DAY_OF_YEAR, daysUntilSaturday)
        
        cal.add(Calendar.WEEK_OF_YEAR, -25)
        cal.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY)
        
        for (i in 0 until 182) {
            datesList.add(dateFormat.format(cal.time))
            cal.add(Calendar.DAY_OF_YEAR, 1)
        }
        return datesList
    }

    companion object {
        val AVAILABLE_TAGS = listOf(
            "运动" to "🏃",
            "美食" to "🍲",
            "娱乐" to "🎮",
            "社交" to "👥",
            "工作" to "💼",
            "学习" to "📚",
            "睡眠" to "🛌",
            "家务" to "🧹",
            "购物" to "🛍️",
            "户外" to "🌳"
        )
    }
}
