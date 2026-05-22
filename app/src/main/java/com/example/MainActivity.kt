package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.MoodEntry
import com.example.ui.MoodViewModel
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                Scaffold(
                    modifier = Modifier
                        .fillMaxSize()
                        .statusBarsPadding()
                        .navigationBarsPadding()
                ) { innerPadding ->
                    MoodTrackerApp(
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
fun MoodTrackerApp(
    modifier: Modifier = Modifier,
    viewModel: MoodViewModel = viewModel()
) {
    val selectedDate by viewModel.selectedDate.collectAsStateWithLifecycle()
    val rating by viewModel.rating.collectAsStateWithLifecycle()
    val note by viewModel.note.collectAsStateWithLifecycle()
    val selectedTags by viewModel.selectedTags.collectAsStateWithLifecycle()
    val moodsByDateMap by viewModel.moodsByDateMap.collectAsStateWithLifecycle()
    val streakCount by viewModel.streakCount.collectAsStateWithLifecycle()
    val allMoods by viewModel.allMoods.collectAsStateWithLifecycle()

    val focusManager = LocalFocusManager.current
    val scrollState = rememberScrollState()

    // Determine gradients background based on current active state/theme
    val isDark = isSystemInDarkTheme()
    val backgroundBrush = if (isDark) {
        Brush.verticalGradient(
            colors = listOf(
                Color(0xFF0F172A), // Deep Slate Blue
                Color(0xFF020617)  // Pitch Black
            )
        )
    } else {
        Brush.verticalGradient(
            colors = listOf(
                Color(0xFFF1F5F9), // Light Slate White
                Color(0xFFE2E8F0)  // Mild Grey-Cream
            )
        )
    }

    Surface(
        modifier = modifier.fillMaxSize(),
        color = Color.Transparent
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(backgroundBrush)
                .verticalScroll(scrollState)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. Header (AppName, Slogan, and Dynamic Logging Streak 🔥)
            HeaderSection(streakCount = streakCount)

            // 2. Heatmap Component Card
            HeatmapCard(
                viewModel = viewModel,
                moodsByDateMap = moodsByDateMap,
                selectedDate = selectedDate
            )

            // 3. Logger Editor Card (Interactive Emoji panel, tag choice, and journal form)
            LoggerCard(
                selectedDate = selectedDate,
                formattedDate = viewModel.formatDisplayDate(selectedDate),
                isToday = selectedDate == viewModel.getTodayDateString(),
                isYesterday = selectedDate == viewModel.getYesterdayDateString(),
                rating = rating,
                note = note,
                selectedTags = selectedTags,
                onRatingChange = { viewModel.setRating(it) },
                onNoteChange = { viewModel.setNote(it) },
                onTagToggle = { viewModel.toggleTag(it) },
                onSave = {
                    viewModel.saveCurrentMood()
                    focusManager.clearFocus()
                },
                onDelete = { viewModel.deleteMoodForSelectedDate() },
                onResetDate = { viewModel.selectDate(viewModel.getTodayDateString()) }
            )

            // 4. Mood Statistics Panel (Averages, charts, percentages)
            StatisticsCard(allMoods = allMoods)
            
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun HeaderSection(streakCount: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "心情日志",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "记录每日心情，看见情绪的起伏",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.65f)
            )
        }

        // Streak Count Flame indicator
        Row(
            modifier = Modifier
                .background(
                    color = if (streakCount > 0) Color(0xFFFFECE5) else MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(16.dp)
                )
                .border(
                    width = 1.dp,
                    color = if (streakCount > 0) Color(0xFFFFA585) else Color.Transparent,
                    shape = RoundedCornerShape(16.dp)
                )
                .padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = if (streakCount > 0) "🔥" else "💤",
                fontSize = 16.sp,
                modifier = Modifier.padding(end = 4.dp)
            )
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "$streakCount 天",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = if (streakCount > 0) Color(0xFFD84315) else MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "连续打卡",
                    fontSize = 8.sp,
                    color = if (streakCount > 0) Color(0xFFD84315).copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }
        }
    }
}

@Composable
fun HeatmapCard(
    viewModel: MoodViewModel,
    moodsByDateMap: Map<String, MoodEntry>,
    selectedDate: String
) {
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("heatmap_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "心情热力图 (近半年)",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                // Floating indicator showing legend guide
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text("糟", style = MaterialTheme.typography.bodySmall, fontSize = 9.sp)
                    Box(modifier = Modifier.size(8.dp).background(Color(0xFFF87171), RoundedCornerShape(1.5.dp)))
                    Box(modifier = Modifier.size(8.dp).background(Color(0xFF60A5FA), RoundedCornerShape(1.5.dp)))
                    Box(modifier = Modifier.size(8.dp).background(Color(0xFFFBBF24), RoundedCornerShape(1.5.dp)))
                    Box(modifier = Modifier.size(8.dp).background(Color(0xFF34D399), RoundedCornerShape(1.5.dp)))
                    Box(modifier = Modifier.size(8.dp).background(Color(0xFF10B981), RoundedCornerShape(1.5.dp)))
                    Text("极棒", style = MaterialTheme.typography.bodySmall, fontSize = 9.sp)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Calendar heatmap view drawing helper
            val heatmapDates = viewModel.generateHeatmapDates()
            MoodHeatmap(
                dates = heatmapDates,
                moodsByDate = moodsByDateMap,
                selectedDate = selectedDate,
                onDateSelect = { viewModel.selectDate(it) }
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "💡 提示: 点击热力图中的色块，可以查看或补录/修改该日期的心情日记！",
                style = MaterialTheme.typography.bodySmall,
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
    }
}

@Composable
fun MoodHeatmap(
    dates: List<String>,
    moodsByDate: Map<String, MoodEntry>,
    selectedDate: String,
    onDateSelect: (String) -> Unit
) {
    val scrollState = rememberScrollState()
    val isDark = isSystemInDarkTheme()

    // Automatically scrolls the heatmap chart to show current date on load
    LaunchedEffect(scrollState.maxValue) {
        if (scrollState.maxValue > 0) {
            scrollState.scrollTo(scrollState.maxValue)
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Weekday labels on the left: Sunday, Monday, Tuesday, ... to Saturday
        Column(
            modifier = Modifier.padding(end = 8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            val cellHeight = 14.dp
            val spacing = 4.dp
            Spacer(modifier = Modifier.height(cellHeight + spacing)) // Sun skip label
            Text("周一", style = MaterialTheme.typography.labelSmall, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurface.copy(0.6f), modifier = Modifier.height(cellHeight))
            Spacer(modifier = Modifier.height(cellHeight + spacing)) // Tue skip label
            Text("周三", style = MaterialTheme.typography.labelSmall, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurface.copy(0.6f), modifier = Modifier.height(cellHeight))
            Spacer(modifier = Modifier.height(cellHeight + spacing)) // Thu skip label
            Text("周五", style = MaterialTheme.typography.labelSmall, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurface.copy(0.6f), modifier = Modifier.height(cellHeight))
            Spacer(modifier = Modifier.height(cellHeight + spacing)) // Sat skip label
        }

        // Horizontal scrolling columns representing the 26 weeks
        Row(
            modifier = Modifier
                .weight(1f)
                .horizontalScroll(scrollState)
                .padding(bottom = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            for (weekIdx in 0 until 26) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    for (dayOfWeekIdx in 0 until 7) {
                        val dateIdx = weekIdx * 7 + dayOfWeekIdx
                        if (dateIdx < dates.size) {
                            val currentDateStr = dates[dateIdx]
                            val entry = moodsByDate[currentDateStr]
                            val rating = entry?.rating ?: 0

                            val cellColor = when (rating) {
                                5 -> Color(0xFF10B981) // Awesome 😊 (Bright Jade/Emerald)
                                4 -> Color(0xFF34D399) // Good 🙂 (Mint)
                                3 -> Color(0xFFFBBF24) // Neutral 😐 (Honey Amber)
                                2 -> Color(0xFF60A5FA) // Sad 🙁 (Teal Sky Blue)
                                1 -> Color(0xFFF87171) // Terrible 😭 (Coral Red)
                                else -> if (isDark) Color(0xFF1E293B) else Color(0xFFE2E8F0) // Empty State
                            }

                            val isSelected = currentDateStr == selectedDate
                            val borderModifier = if (isSelected) {
                                Modifier.border(
                                    width = 2.dp,
                                    color = MaterialTheme.colorScheme.primary,
                                    shape = RoundedCornerShape(4.dp)
                                )
                            } else if (entry != null) {
                                Modifier // logged already
                            } else {
                                Modifier // empty, silent outline
                            }

                            Box(
                                modifier = Modifier
                                    .size(14.dp)
                                    .then(borderModifier)
                                    .clip(RoundedCornerShape(3.dp))
                                    .background(cellColor)
                                    .clickable { onDateSelect(currentDateStr) }
                                    .testTag("heatmap_cell_$currentDateStr")
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun LoggerCard(
    selectedDate: String,
    formattedDate: String,
    isToday: Boolean,
    isYesterday: Boolean,
    rating: Int,
    note: String,
    selectedTags: Set<String>,
    onRatingChange: (Int) -> Unit,
    onNoteChange: (String) -> Unit,
    onTagToggle: (String) -> Unit,
    onSave: () -> Unit,
    onDelete: () -> Unit,
    onResetDate: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("logger_card")
            .animateContentSize(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Logger Header Date selector
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    val labelSuffix = when {
                        isToday -> " (今天)"
                        isYesterday -> " (昨天)"
                        else -> ""
                    }
                    Text(
                        text = "写日记: $formattedDate$labelSuffix",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "选择一个代表您今天情绪的评分",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }

                // Quick back-to-today focus button when user selects old dates
                if (!isToday) {
                    TextButton(
                        onClick = onResetDate,
                        modifier = Modifier.testTag("back_to_today_btn")
                    ) {
                        Text("返回今天", fontSize = 12.sp)
                    }
                }
            }

            // Interactive Mood Selector Row
            val moodsList = listOf(
                1 to ("😭" to "很糟"),
                2 to ("🙁" to "差劲"),
                3 to ("😐" to "一般"),
                4 to ("🙂" to "挺好"),
                5 to ("😊" to "棒极")
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                moodsList.forEach { (index, pair) ->
                    val (emoji, desc) = pair
                    val isSelected = rating == index
                    val containerBgColor by animateColorAsState(
                        targetValue = if (isSelected) {
                            when (index) {
                                5 -> Color(0xFFD1FAE5) // light Emerald
                                4 -> Color(0xFFECFDF5) // light Mint
                                3 -> Color(0xFFFEF3C7) // light Amber
                                2 -> Color(0xFFDBEAFE) // light Sky Blue
                                else -> Color(0xFFFEE2E2) // light Red
                            }
                        } else Color.Transparent,
                        label = "emoji_container"
                    )

                    val emojiScale by animateFloatAsState(
                        targetValue = if (isSelected) 1.25f else 1.0f,
                        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
                        label = "emoji_scale"
                    )

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(containerBgColor)
                            .clickable { onRatingChange(index) }
                            .padding(vertical = 8.dp)
                            .testTag("mood_selector_$index"),
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = emoji,
                            fontSize = 32.sp,
                            modifier = Modifier.scale(emojiScale)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = desc,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) {
                                when (index) {
                                    5 -> Color(0xFF065F46)
                                    4 -> Color(0xFF065F46)
                                    3 -> Color(0xFF92400E)
                                    2 -> Color(0xFF1E40AF)
                                    else -> Color(0xFF991B1B)
                                }
                            } else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                            fontSize = 11.sp
                        )
                    }
                }
            }

            // Quick Tag Activity chips selector
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "今天做了些什么吗？(可多选)",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )

                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    MoodViewModel.AVAILABLE_TAGS.forEach { (tagName, tagEmoji) ->
                        val isSelected = selectedTags.contains(tagName)
                        val chipBgColor = if (isSelected) {
                            MaterialTheme.colorScheme.primaryContainer
                        } else {
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        }
                        val chipContentColor = if (isSelected) {
                            MaterialTheme.colorScheme.onPrimaryContainer
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        }

                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .background(chipBgColor)
                                .clickable { onTagToggle(tagName) }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                                .testTag("tag_chip_$tagName"),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = "$tagEmoji ", fontSize = 14.sp)
                            Text(
                                text = tagName,
                                style = MaterialTheme.typography.labelLarge,
                                color = chipContentColor,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }
            }

            // Diary Note Form Fields input
            OutlinedTextField(
                value = note,
                onValueChange = { if (it.length <= 200) onNoteChange(it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
                    .testTag("note_input"),
                shape = RoundedCornerShape(12.dp),
                placeholder = {
                    Text(
                        "这一天过得开心吗？发生些什么故事了呢... (选填 200字内)",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                    )
                },
                supportingText = {
                    Text(
                        text = "${note.length}/200",
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.End,
                        style = MaterialTheme.typography.labelSmall
                    )
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                    unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.15f)
                ),
                maxLines = 4
            )

            // Submit Buttons actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Determine save/update terminology based on existing rating record state
                val isEditMode = rating > 0

                // If logged already and saved in database, allow deletion trash button to completely clear
                if (isEditMode) {
                    Button(
                        onClick = onDelete,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer,
                            contentColor = MaterialTheme.colorScheme.onErrorContainer
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .height(48.dp)
                            .testTag("delete_mood_btn")
                    ) {
                        Icon(imageVector = Icons.Default.Delete, contentDescription = "删除心情日志")
                    }
                }

                Button(
                    onClick = onSave,
                    enabled = rating > 0, // Enabled only once rating > 0 emoji is chosen
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("save_mood_btn"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (rating > 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "保存心情",
                        modifier = Modifier.padding(end = 6.dp)
                    )
                    Text(
                        text = if (isEditMode) "更新今日心情" else "保存心情日志",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }
            }
        }
    }
}

@Composable
fun StatisticsCard(allMoods: List<MoodEntry>) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("statistics_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "心情轨迹统计与洞察",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "了解您过去的行为活动如何影响您的心理状态",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (allMoods.isEmpty()) {
                // Empty state informational guide
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "未获取到分析记录",
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                        modifier = Modifier.size(36.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "暂无充足的心情数据来进行图表统计",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                    Text(
                        text = "开始记录第一天的那一画吧！",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                    )
                }
            } else {
                // Compute standard percentages and insights metrics
                val totalDays = allMoods.size
                val ratingCounts = IntArray(6) // index 1 to 5
                val tagFreqMap = mutableMapOf<String, Int>()

                allMoods.forEach { entry ->
                    if (entry.rating in 1..5) {
                        ratingCounts[entry.rating]++
                    }
                    if (entry.tags.isNotEmpty()) {
                        entry.tags.split(",").forEach { tag ->
                            if (tag.isNotEmpty()) {
                                tagFreqMap[tag] = tagFreqMap.getOrDefault(tag, 0) + 1
                            }
                        }
                    }
                }

                val avgRating = allMoods.map { it.rating }.average()
                val topActivities = tagFreqMap.entries.sortedByDescending { it.value }.take(3)

                // Render Summary grid counters
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Left Metric Card
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .background(
                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                                RoundedCornerShape(12.dp)
                            )
                            .padding(12.dp)
                    ) {
                        Column {
                            Text(
                                "记录天数",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(0.6f)
                            )
                            Text(
                                "$totalDays 天",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    // Right Metric Card
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .background(
                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                                RoundedCornerShape(12.dp)
                            )
                            .padding(12.dp)
                    ) {
                        Column {
                            Text(
                                "平均心情指数",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(0.6f)
                            )
                            Text(
                                String.format("%.1f / 5.0", avgRating),
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = when {
                                    avgRating >= 4.0 -> Color(0xFF10B981) // awesome emerald
                                    avgRating >= 3.0 -> Color(0xFFF59E0B) // warm orange
                                    else -> Color(0xFFEF4444)             // vibrant red
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Mood percentage bars: Excel list styling
                Text(
                    text = "心情级别构成比例",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                )

                Spacer(modifier = Modifier.height(8.dp))

                val labelMoods = listOf(
                    5 to ("😊 棒极" to Color(0xFF10B981)),
                    4 to ("🙂 挺好" to Color(0xFF34D399)),
                    3 to ("😐 一般" to Color(0xFFFBBF24)),
                    2 to ("🙁 差劲" to Color(0xFF60A5FA)),
                    1 to ("😭 很糟" to Color(0xFFF87171))
                )

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    labelMoods.forEach { (index, item) ->
                        val (desc, color) = item
                        val count = ratingCounts[index]
                        val fraction = if (totalDays > 0) count.toFloat() / totalDays else 0f
                        val percent = (fraction * 100).toInt()

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = desc,
                                style = MaterialTheme.typography.bodySmall,
                                fontSize = 11.sp,
                                modifier = Modifier.width(60.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            // Glowing Progress Indicator bar
                            LinearProgressIndicator(
                                progress = { fraction },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp)),
                                color = color,
                                trackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "$count 天 ($percent%)",
                                style = MaterialTheme.typography.labelSmall,
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                                modifier = Modifier.width(65.dp),
                                textAlign = TextAlign.End
                            )
                        }
                    }
                }

                if (topActivities.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "情绪最频繁的活动因素",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        topActivities.forEachIndexed { rank, entry ->
                            val activityEmoji = MoodViewModel.AVAILABLE_TAGS.firstOrNull { it.first == entry.key }?.second ?: "🏷️"
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .background(
                                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                                        RoundedCornerShape(10.dp)
                                    )
                                    .padding(8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = "#${rank + 1} $activityEmoji",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = entry.key,
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "关联 ${entry.value} 天",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontSize = 9.sp,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
