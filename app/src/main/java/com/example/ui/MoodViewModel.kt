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
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import kotlin.math.roundToInt

class MoodViewModel(application: Application) : AndroidViewModel(application) {
    private val database = MoodDatabase.getDatabase(application)
    private val repository = MoodRepository(database.moodDao())
    private val prefs = application.getSharedPreferences("mood_app_prefs", android.content.Context.MODE_PRIVATE)
    
    private val _activityTags = MutableStateFlow<List<ActivityTag>>(emptyList())
    val activityTags: StateFlow<List<ActivityTag>> = _activityTags.asStateFlow()

    private val _appLanguage = MutableStateFlow(prefs.getString("app_language", I18n.LANG_AUTO) ?: I18n.LANG_AUTO)
    val appLanguage: StateFlow<String> = _appLanguage.asStateFlow()

    val resolvedLanguage: StateFlow<String> = _appLanguage
        .map { lang ->
            if (lang == I18n.LANG_AUTO) getSystemLanguageCode() else lang
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = if (prefs.getString("app_language", I18n.LANG_AUTO) == I18n.LANG_AUTO) getSystemLanguageCode() else prefs.getString("app_language", I18n.LANG_AUTO) ?: I18n.LANG_AUTO
        )

    fun setAppLanguage(lang: String) {
        _appLanguage.value = lang
        prefs.edit().putString("app_language", lang).apply()
    }

    private fun getSystemLanguageCode(): String {
        val locale = Locale.getDefault()
        val lang = locale.language
        val country = locale.country
        return if (lang == "zh") {
            if (country == "TW" || country == "HK" || country == "MO") {
                I18n.LANG_ZH_TW
            } else {
                I18n.LANG_ZH_CN
            }
        } else if (lang == "ko") {
            I18n.LANG_KO
        } else if (lang == "ja") {
            I18n.LANG_JA
        } else {
            I18n.LANG_EN
        }
    }

    init {
        loadActivityTags()
    }

    val allMoods: StateFlow<List<MoodEntry>> = repository.allMoods
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _themeMode = MutableStateFlow(prefs.getInt("theme_mode", 0))
    val themeMode: StateFlow<Int> = _themeMode.asStateFlow()

    fun setThemeMode(mode: Int) {
        _themeMode.value = mode
        prefs.edit().putInt("theme_mode", mode).apply()
    }

    private val _currentTab = MutableStateFlow(0)
    val currentTab: StateFlow<Int> = _currentTab.asStateFlow()

    fun selectTab(tab: Int) {
        _currentTab.value = tab
    }

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    private val dateFormatLock = Any()

    private fun safeFormatDate(date: java.util.Date): String {
        return synchronized(dateFormatLock) {
            dateFormat.format(date)
        }
    }

    private fun safeParseDate(str: String): java.util.Date? {
        return synchronized(dateFormatLock) {
            dateFormat.parse(str)
        }
    }

    private val _selectedDate = MutableStateFlow(getTodayDateString())
    val selectedDate: StateFlow<String> = _selectedDate.asStateFlow()

    private val _selectedMoodId = MutableStateFlow<Long?>(null)
    val selectedMoodId: StateFlow<Long?> = _selectedMoodId.asStateFlow()

    private val _rating = MutableStateFlow(0)
    val rating: StateFlow<Int> = _rating.asStateFlow()

    private val _note = MutableStateFlow("")
    val note: StateFlow<String> = _note.asStateFlow()

    private val _time = MutableStateFlow(getCurrentTime24h())
    val time: StateFlow<String> = _time.asStateFlow()

    private val _selectedTags = MutableStateFlow(setOf<String>())
    val selectedTags: StateFlow<Set<String>> = _selectedTags.asStateFlow()

    // All moods recorded for the currently selected date
    val moodsOfSelectedDate: StateFlow<List<MoodEntry>> = allMoods
        .combine(selectedDate) { moods, date ->
            moods.filter { it.date == date }.sortedByDescending { it.time }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Map to lookup representative mood entries by date (showing the average rating) for heatmap display
    val moodsByDateMap: StateFlow<Map<String, MoodEntry>> = allMoods
        .map { moods ->
            moods.groupBy { it.date }.mapValues { (date, entries) ->
                if (entries.isNotEmpty()) {
                    val avgRating = entries.map { it.rating }.average().roundToInt()
                    // Return representative entry with average rating for calendar decoration
                    entries.first().copy(rating = avgRating)
                } else {
                    entries.first()
                }
            }
        }
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
        
        val todayStr = safeFormatDate(cal.time)
        cal.add(Calendar.DAY_OF_YEAR, -1)
        val yesterdayStr = safeFormatDate(cal.time)
        
        // If neither today nor yesterday is logged, the streak is currently 0
        if (!loggedDates.contains(todayStr) && !loggedDates.contains(yesterdayStr)) {
            return 0
        }
        
        // Start counting back from today
        cal.time = Calendar.getInstance().time
        while (true) {
            val dateStr = safeFormatDate(cal.time)
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

    fun getTodayDateString(): String {
        return safeFormatDate(Calendar.getInstance().time)
    }

    fun getYesterdayDateString(): String {
        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_YEAR, -1)
        return safeFormatDate(cal.time)
    }

    fun getCurrentTime24h(): String {
        return SimpleDateFormat("HH:mm", Locale.getDefault()).format(Calendar.getInstance().time)
    }

    fun formatDisplayDate(dateStr: String, lang: String): String {
        return try {
            val date = safeParseDate(dateStr)
            val locale = when (lang) {
                I18n.LANG_ZH_CN -> Locale.SIMPLIFIED_CHINESE
                I18n.LANG_ZH_TW -> Locale.TRADITIONAL_CHINESE
                I18n.LANG_KO -> Locale.KOREAN
                I18n.LANG_JA -> Locale.JAPANESE
                else -> Locale.ENGLISH
            }
            val pattern = when (lang) {
                I18n.LANG_ZH_CN, I18n.LANG_ZH_TW -> "yyyy年M月d日 EEEE"
                I18n.LANG_JA -> "yyyy年M月d日(E)"
                I18n.LANG_KO -> "yyyy년 M월 d일(E)"
                else -> "EEEE, MMM d, yyyy"
            }
            val displayFormat = SimpleDateFormat(pattern, locale)
            displayFormat.format(date ?: Calendar.getInstance().time)
        } catch (e: Exception) {
            dateStr
        }
    }

    fun selectDate(date: String) {
        _selectedDate.value = date
        selectMoodEntry(null)
    }

    fun selectMoodEntry(entry: MoodEntry?) {
        if (entry != null) {
            _selectedMoodId.value = entry.id
            _rating.value = entry.rating
            _note.value = entry.note
            _time.value = entry.time
            _selectedTags.value = if (entry.tags.isNotEmpty()) {
                entry.tags.split(",").toSet()
            } else {
                emptySet()
            }
        } else {
            _selectedMoodId.value = null
            _rating.value = 0
            _note.value = ""
            // When initiating a new entry, default to current 24h time representation
            _time.value = getCurrentTime24h()
            _selectedTags.value = emptySet()
        }
    }

    fun setRating(rate: Int) {
        _rating.value = rate
    }

    fun setNote(text: String) {
        _note.value = text
    }

    fun setTime(customTime: String) {
        _time.value = customTime
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
        val timeText = _time.value.trim().ifEmpty { getCurrentTime24h() }
        val tagsStr = _selectedTags.value.joinToString(",")
        
        if (rate == 0) return

        viewModelScope.launch {
            val entry = MoodEntry(
                id = _selectedMoodId.value ?: 0L,
                date = date,
                time = timeText,
                rating = rate,
                note = noteText,
                tags = tagsStr
            )
            repository.insertMood(entry)
            // Reset to prevent double click, ready for logging next mood
            selectMoodEntry(null)
        }
    }

    fun deleteMoodEntry(id: Long) {
        viewModelScope.launch {
            repository.deleteMoodById(id)
            if (_selectedMoodId.value == id) {
                selectMoodEntry(null)
            }
        }
    }

    fun deleteCurrentSelectedMood() {
        _selectedMoodId.value?.let { id ->
            deleteMoodEntry(id)
        } ?: selectMoodEntry(null)
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

    fun exportToJson(): String {
        val root = org.json.JSONObject()
        root.put("version", 2)

        // Serialize mood entries
        val moodsList = allMoods.value
        val moodsArray = org.json.JSONArray()
        for (entry in moodsList) {
            val obj = org.json.JSONObject()
            obj.put("date", entry.date)
            obj.put("time", entry.time)
            obj.put("rating", entry.rating)
            obj.put("note", entry.note)
            obj.put("tags", entry.tags)
            obj.put("timestamp", entry.timestamp)
            moodsArray.put(obj)
        }
        root.put("moods", moodsArray)

        // Serialize custom activity tags
        val tagsList = _activityTags.value
        val tagsArray = org.json.JSONArray()
        for (tag in tagsList) {
            val obj = org.json.JSONObject()
            obj.put("name", tag.name)
            obj.put("emoji", tag.emoji)
            tagsArray.put(obj)
        }
        root.put("activity_tags", tagsArray)

        return root.toString(2)
    }

    fun importFromJson(jsonStr: String): Boolean {
        return try {
            val trimmed = jsonStr.trim()
            if (trimmed.startsWith("{")) {
                val root = org.json.JSONObject(trimmed)

                // 1. Parse moods
                val moodsArray = root.optJSONArray("moods")
                if (moodsArray != null) {
                    viewModelScope.launch {
                        for (i in 0 until moodsArray.length()) {
                            val obj = moodsArray.getJSONObject(i)
                            val entry = MoodEntry(
                                date = obj.optString("date", getTodayDateString()),
                                time = obj.optString("time", "12:00"),
                                rating = obj.optInt("rating", 3),
                                note = obj.optString("note", ""),
                                tags = obj.optString("tags", ""),
                                timestamp = obj.optLong("timestamp", System.currentTimeMillis())
                            )
                            repository.insertMood(entry)
                        }
                    }
                }

                // 2. Parse custom activity tags and merge them
                val tagsArray = root.optJSONArray("activity_tags")
                if (tagsArray != null) {
                    val currentTags = _activityTags.value.toMutableList()
                    for (i in 0 until tagsArray.length()) {
                        val obj = tagsArray.getJSONObject(i)
                        val name = obj.getString("name").trim()
                        val emoji = obj.getString("emoji").trim()
                        if (name.isNotEmpty()) {
                            val existingIndex = currentTags.indexOfFirst { it.name.equals(name, ignoreCase = true) }
                            if (existingIndex == -1) {
                                currentTags.add(ActivityTag(name, emoji))
                            } else {
                                currentTags[existingIndex] = ActivityTag(name, emoji)
                            }
                        }
                    }
                    _activityTags.value = currentTags
                    saveActivityTagsToPrefs(currentTags)
                }
                true
            } else {
                // Backward compatibility: old format where top-level element is a JSONArray
                val jsonArray = org.json.JSONArray(trimmed)
                viewModelScope.launch {
                    for (i in 0 until jsonArray.length()) {
                        val obj = jsonArray.getJSONObject(i)
                        val entry = MoodEntry(
                            date = obj.optString("date", getTodayDateString()),
                            time = obj.optString("time", "12:00"),
                            rating = obj.optInt("rating", 3),
                            note = obj.optString("note", ""),
                            tags = obj.optString("tags", ""),
                            timestamp = obj.optLong("timestamp", System.currentTimeMillis())
                        )
                        repository.insertMood(entry)
                    }
                }
                true
            }
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    private fun loadActivityTags() {
        val serialized = prefs.getString("custom_activity_tags", null)
        if (serialized == null) {
            val defaultTags = listOf(
                ActivityTag("运动", "🏃"),
                ActivityTag("美食", "🍲"),
                ActivityTag("娱乐", "🎮"),
                ActivityTag("社交", "👥"),
                ActivityTag("工作", "💼"),
                ActivityTag("学习", "📚"),
                ActivityTag("睡眠", "🛌"),
                ActivityTag("家务", "🧹"),
                ActivityTag("购物", "🛍️"),
                ActivityTag("户外", "🌳")
            )
            saveActivityTagsToPrefs(defaultTags)
            _activityTags.value = defaultTags
        } else {
            try {
                val list = mutableListOf<ActivityTag>()
                val arr = org.json.JSONArray(serialized)
                for (i in 0 until arr.length()) {
                    val obj = arr.getJSONObject(i)
                    list.add(ActivityTag(obj.getString("name"), obj.getString("emoji")))
                }
                _activityTags.value = list
            } catch (e: Exception) {
                _activityTags.value = emptyList()
            }
        }
    }

    private fun saveActivityTagsToPrefs(list: List<ActivityTag>) {
        val arr = org.json.JSONArray()
        for (tag in list) {
            val obj = org.json.JSONObject()
            obj.put("name", tag.name)
            obj.put("emoji", tag.emoji)
            arr.put(obj)
        }
        prefs.edit().putString("custom_activity_tags", arr.toString()).apply()
    }

    fun addActivityTag(name: String, emoji: String): Boolean {
        val trimmedName = name.trim()
        if (trimmedName.isEmpty()) return false
        val current = _activityTags.value.toMutableList()
        if (current.any { it.name.equals(trimmedName, ignoreCase = true) }) {
            return false
        }
        val newTag = ActivityTag(trimmedName, if (emoji.trim().isEmpty()) "🏷️" else emoji.trim())
        current.add(newTag)
        _activityTags.value = current
        saveActivityTagsToPrefs(current)
        return true
    }

    fun updateActivityTag(oldName: String, newName: String, newEmoji: String): Boolean {
        val trimmedNewName = newName.trim()
        if (trimmedNewName.isEmpty()) return false
        val current = _activityTags.value.toMutableList()
        val index = current.indexOfFirst { it.name == oldName }
        if (index == -1) return false
        
        if (!oldName.equals(trimmedNewName, ignoreCase = true) && 
            current.any { it.name.equals(trimmedNewName, ignoreCase = true) }) {
            return false
        }
        
        current[index] = ActivityTag(trimmedNewName, if (newEmoji.trim().isEmpty()) "🏷️" else newEmoji.trim())
        _activityTags.value = current
        saveActivityTagsToPrefs(current)
        return true
    }

    fun deleteActivityTag(name: String) {
        val current = _activityTags.value.toMutableList()
        current.removeAll { it.name == name }
        _activityTags.value = current
        saveActivityTagsToPrefs(current)
    }


}

data class ActivityTag(val name: String, val emoji: String)
