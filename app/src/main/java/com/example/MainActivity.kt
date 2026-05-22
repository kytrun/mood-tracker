package com.example

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.MoodEntry
import com.example.ui.MoodViewModel
import com.example.ui.theme.MyApplicationTheme
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val viewModel: MoodViewModel = viewModel()
            val themeMode by viewModel.themeMode.collectAsStateWithLifecycle()
            val systemDark = isSystemInDarkTheme()
            val isDark = when (themeMode) {
                1 -> false
                2 -> true
                else -> systemDark
            }
            MyApplicationTheme(darkTheme = isDark) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MoodTrackerAppWithSplash(
                        viewModel = viewModel,
                        isDark = isDark
                    )
                }
            }
        }
    }
}

@Composable
fun MoodTrackerAppWithSplash(
    modifier: Modifier = Modifier,
    viewModel: MoodViewModel = viewModel(),
    isDark: Boolean
) {
    var showSplash by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        delay(1800) // Show stunning launch screen for 1.8 seconds
        showSplash = false
    }

    Box(modifier = modifier.fillMaxSize()) {
        MoodTrackerApp(viewModel = viewModel, isDark = isDark)

        // Custom full-screen Animated Splash Screen
        AnimatedVisibility(
            visible = showSplash,
            exit = fadeOut(animationSpec = tween(durationMillis = 600)) + shrinkVertically(animationSpec = tween(durationMillis = 600))
        ) {
            SplashScreen(isDark = isDark)
        }
    }
}

@Composable
fun SplashScreen(isDark: Boolean) {
    val bgBrush = if (isDark) {
        Brush.verticalGradient(
            colors = listOf(Color(0xFF0F172A), Color(0xFF020617))
        )
    } else {
        Brush.verticalGradient(
            colors = listOf(Color(0xFFECFDF5), Color(0xFFD1FAE5))
        )
    }

    // Gentle floating icon scale animation
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(bgBrush),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(24.dp)
        ) {
            // Highly beautiful visual cover loader image
            Card(
                modifier = Modifier
                    .size(240.dp)
                    .scale(pulseScale),
                shape = RoundedCornerShape(28.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.splash_banner_1779432624928),
                    contentDescription = "启动页面画",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Text Typography logo
            Text(
                text = "心情日志",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = if (isDark) Color.White else Color(0xFF065F46)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "遇见情绪，静享生活的每一个故事",
                style = MaterialTheme.typography.bodyMedium,
                color = if (isDark) Color.White.copy(0.6f) else Color(0xFF0F766E),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(48.dp))

            CircularProgressIndicator(
                color = if (isDark) Color(0xFF10B981) else Color(0xFF047857),
                modifier = Modifier.size(36.dp),
                strokeWidth = 3.dp
            )
        }
    }
}

@Composable
fun MoodTrackerApp(
    viewModel: MoodViewModel = viewModel(),
    isDark: Boolean = false
) {
    val selectedDate by viewModel.selectedDate.collectAsStateWithLifecycle()
    val rating by viewModel.rating.collectAsStateWithLifecycle()
    val note by viewModel.note.collectAsStateWithLifecycle()
    val time by viewModel.time.collectAsStateWithLifecycle()
    val selectedTags by viewModel.selectedTags.collectAsStateWithLifecycle()
    val selectedMoodId by viewModel.selectedMoodId.collectAsStateWithLifecycle()
    val moodsByDateMap by viewModel.moodsByDateMap.collectAsStateWithLifecycle()
    val streakCount by viewModel.streakCount.collectAsStateWithLifecycle()
    val allMoods by viewModel.allMoods.collectAsStateWithLifecycle()
    val moodsOfSelectedDate by viewModel.moodsOfSelectedDate.collectAsStateWithLifecycle()
    val currentTab by viewModel.currentTab.collectAsStateWithLifecycle()

    val context = LocalContext.current
    val focusManager = LocalFocusManager.current

    // ActivityResult launchers for Document Export/Import actions
    val jsonFileCreatorLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        uri?.let {
            try {
                context.contentResolver.openOutputStream(it)?.use { outputStream ->
                    outputStream.write(viewModel.exportToJson().toByteArray())
                }
                Toast.makeText(context, "导出心情数据成功！🎉", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(context, "导出失败: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    val jsonFileSelectorLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let {
            try {
                context.contentResolver.openInputStream(it)?.use { inputStream ->
                    val rawJson = inputStream.bufferedReader().use { reader -> reader.readText() }
                    val successfullyImported = viewModel.importFromJson(rawJson)
                    if (successfullyImported) {
                        Toast.makeText(context, "导入心情数据成功！📊", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(context, "无法解析文件，请确保格式正确。", Toast.LENGTH_LONG).show()
                    }
                }
            } catch (e: Exception) {
                Toast.makeText(context, "导入失败: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    // Determine gradients background based on current active state/theme
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

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundBrush)
    ) {
        val activityTags by viewModel.activityTags.collectAsStateWithLifecycle()

        Scaffold(
            bottomBar = {
                NavigationBar(
                    containerColor = if (isDark) Color(0xFF1E293B) else Color(0xFFFFFFFF),
                    tonalElevation = 8.dp
                ) {
                    NavigationBarItem(
                        selected = currentTab == 0,
                        onClick = { viewModel.selectTab(0) },
                        icon = { Icon(imageVector = Icons.Default.Edit, contentDescription = "记心情") },
                        label = { Text("记心情", fontWeight = if (currentTab == 0) FontWeight.Bold else FontWeight.Normal) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                        ),
                        modifier = Modifier.testTag("tab_record")
                    )
                    NavigationBarItem(
                        selected = currentTab == 1,
                        onClick = { viewModel.selectTab(1) },
                        icon = { Icon(imageVector = Icons.Default.DateRange, contentDescription = "日历大屏") },
                        label = { Text("今日历", fontWeight = if (currentTab == 1) FontWeight.Bold else FontWeight.Normal) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                        ),
                        modifier = Modifier.testTag("tab_calendar")
                    )
                    NavigationBarItem(
                        selected = currentTab == 2,
                        onClick = { viewModel.selectTab(2) },
                        icon = { Icon(imageVector = Icons.Default.Info, contentDescription = "洞察分析") },
                        label = { Text("析数据", fontWeight = if (currentTab == 2) FontWeight.Bold else FontWeight.Normal) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                        ),
                        modifier = Modifier.testTag("tab_insights")
                    )
                }
            },
            containerColor = Color.Transparent
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = innerPadding.calculateBottomPadding())
                    .statusBarsPadding()
            ) {
                Crossfade(targetState = currentTab, label = "tab_fade") { tab ->
                    when (tab) {
                        0 -> RecordTabContent(
                            viewModel = viewModel,
                            selectedDate = selectedDate,
                            rating = rating,
                            note = note,
                            time = time,
                            selectedTags = selectedTags,
                            selectedMoodId = selectedMoodId,
                            moodsByDateMap = moodsByDateMap,
                            streakCount = streakCount,
                            allMoods = allMoods,
                            moodsOfSelectedDate = moodsOfSelectedDate,
                            activityTags = activityTags,
                            focusManager = focusManager
                        )
                        1 -> CalendarTabContent(
                            viewModel = viewModel,
                            selectedDate = selectedDate,
                            moodsByDateMap = moodsByDateMap,
                            moodsOfSelectedDate = moodsOfSelectedDate,
                            selectedMoodId = selectedMoodId
                        )
                        2 -> InsightsTabContent(
                            viewModel = viewModel,
                            allMoods = allMoods,
                            isDark = isDark,
                            activityTags = activityTags,
                            onExport = { jsonFileCreatorLauncher.launch("mood_logs_${System.currentTimeMillis()}.json") },
                            onImport = { jsonFileSelectorLauncher.launch(arrayOf("application/json")) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun RecordTabContent(
    viewModel: MoodViewModel,
    selectedDate: String,
    rating: Int,
    note: String,
    time: String,
    selectedTags: Set<String>,
    selectedMoodId: Long?,
    moodsByDateMap: Map<String, MoodEntry>,
    streakCount: Int,
    allMoods: List<MoodEntry>,
    moodsOfSelectedDate: List<MoodEntry>,
    activityTags: List<com.example.ui.ActivityTag>,
    focusManager: androidx.compose.ui.focus.FocusManager
) {
    val scrollState = rememberScrollState()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        HeaderSection(
            streakCount = streakCount
        )

        if (viewModel.formatDisplayDate(selectedDate).isNotEmpty()) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "📅 选中日期: " + viewModel.formatDisplayDate(selectedDate),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                if (selectedDate != viewModel.getTodayDateString()) {
                    TextButton(
                        onClick = { viewModel.selectDate(viewModel.getTodayDateString()) }
                    ) {
                        Text("回今天", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        LoggedMoodsListCard(
            moodsOfSelectedDate = moodsOfSelectedDate,
            selectedMoodId = selectedMoodId,
            onSelectEntry = { viewModel.selectMoodEntry(it) },
            onDeleteEntry = { viewModel.deleteMoodEntry(it.id) },
            onStartNewEntry = { viewModel.selectMoodEntry(null) }
        )

        LoggerEditorCard(
            selectedDate = selectedDate,
            isToday = selectedDate == viewModel.getTodayDateString(),
            isYesterday = selectedDate == viewModel.getYesterdayDateString(),
            rating = rating,
            note = note,
            time = time,
            selectedTags = selectedTags,
            selectedMoodId = selectedMoodId,
            activityTags = activityTags,
            onRatingChange = { viewModel.setRating(it) },
            onNoteChange = { viewModel.setNote(it) },
            onTimeChange = { viewModel.setTime(it) },
            onTagToggle = { viewModel.toggleTag(it) },
            onSave = {
                viewModel.saveCurrentMood()
                focusManager.clearFocus()
            },
            onDelete = { viewModel.deleteCurrentSelectedMood() },
            onCancelEdit = { viewModel.selectMoodEntry(null) },
            onResetDate = { viewModel.selectDate(viewModel.getTodayDateString()) }
        )
    }
}

@Composable
fun CalendarTabContent(
    viewModel: MoodViewModel,
    selectedDate: String,
    moodsByDateMap: Map<String, MoodEntry>,
    moodsOfSelectedDate: List<MoodEntry>,
    selectedMoodId: Long?
) {
    val scrollState = rememberScrollState()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "情绪大屏日历",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(bottom = 4.dp)
        )

        HeatmapCard(
            viewModel = viewModel,
            moodsByDateMap = moodsByDateMap,
            selectedDate = selectedDate
        )

        if (viewModel.formatDisplayDate(selectedDate).isNotEmpty()) {
            Text(
                text = "🗓️ 点击选中: " + viewModel.formatDisplayDate(selectedDate),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(horizontal = 4.dp)
            )
        }

        LoggedMoodsListCard(
            moodsOfSelectedDate = moodsOfSelectedDate,
            selectedMoodId = selectedMoodId,
            onSelectEntry = { 
                viewModel.selectMoodEntry(it)
                viewModel.selectTab(0)
            },
            onDeleteEntry = { viewModel.deleteMoodEntry(it.id) },
            onStartNewEntry = { 
                viewModel.selectMoodEntry(null)
                viewModel.selectTab(0)
            }
        )
    }
}

@Composable
fun InsightsTabContent(
    viewModel: MoodViewModel,
    allMoods: List<MoodEntry>,
    isDark: Boolean,
    activityTags: List<com.example.ui.ActivityTag>,
    onExport: () -> Unit,
    onImport: () -> Unit
) {
    val scrollState = rememberScrollState()
    val themeMode by viewModel.themeMode.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "情绪统计与设置",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(bottom = 4.dp)
        )

        StatisticsCard(allMoods = allMoods, activityTags = activityTags)

        BackupSettingsCard(onExport = onExport, onImport = onImport)

        ActivityTagsManagerCard(
            activityTags = activityTags,
            onAddTag = { name, emoji -> viewModel.addActivityTag(name, emoji) },
            onUpdateTag = { oldName, newName, newEmoji -> viewModel.updateActivityTag(oldName, newName, newEmoji) },
            onDeleteTag = { viewModel.deleteActivityTag(it) }
        )

        ThemeSettingsCard(
            themeMode = themeMode,
            onThemeChange = { viewModel.setThemeMode(it) },
            isDark = isDark
        )
    }
}

@Composable
fun ThemeSettingsCard(
    themeMode: Int,
    onThemeChange: (Int) -> Unit,
    isDark: Boolean
) {
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("theme_settings_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "风格设置",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    text = "主题颜色设置",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Text(
                text = "可在此一键切换应用的主题风格。您可以手动锁定浅色/深色，或是让应用智能跟随手机系统设定，带来最体贴舒适的视觉体验。",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                lineHeight = 16.sp
            )

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(12.dp)
                    )
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                val modes = listOf(
                    0 to "🖥️ 跟随系统",
                    1 to "☀️ 活力日间",
                    2 to "🌙 静谧夜间"
                )

                modes.forEach { (modeIdx, label) ->
                    val isSelected = themeMode == modeIdx
                    val activeBg = if (isDark) Color(0xFF334155) else Color(0xFFFFFFFF)
                    val bg = if (isSelected) activeBg else Color.Transparent
                    val textWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    val textColor = if (isSelected) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(bg)
                            .clickable { onThemeChange(modeIdx) }
                            .padding(vertical = 10.dp)
                            .testTag("theme_mode_$modeIdx"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = textWeight,
                            color = textColor,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun HeaderSection(
    streakCount: Int
) {
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
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "关联时间，支持一日记录，自定义活动类别",
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
                text = "💡 提示: 点击色块以按天补录或选择修改。日历颜色反映该天各项心情的总平均状态！",
                style = MaterialTheme.typography.bodySmall,
                fontSize = 11.sp,
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

@Composable
fun LoggedMoodsListCard(
    moodsOfSelectedDate: List<MoodEntry>,
    selectedMoodId: Long?,
    onSelectEntry: (MoodEntry) -> Unit,
    onDeleteEntry: (MoodEntry) -> Unit,
    onStartNewEntry: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("logged_moods_container"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
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
                    text = "已记录心情 (${moodsOfSelectedDate.size})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Button(
                    onClick = onStartNewEntry,
                    modifier = Modifier.height(34.dp).testTag("add_another_mood_btn"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    ),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp)
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "新增记事", modifier = Modifier.size(16.dp))
                    Text("新增一条", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            if (moodsOfSelectedDate.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(
                            width = 1.dp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f),
                            shape = RoundedCornerShape(12.dp)
                        )
                        .padding(vertical = 24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("☕ 这一天还没有添加任何心情日志哦", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f))
                        Text("点击下方表单录入这一刻的心情吧！", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f))
                    }
                }
            } else {
                val emojiList = mapOf(
                    1 to "😭 很糟",
                    2 to "🙁 差劲",
                    3 to "😐 一般",
                    4 to "🙂 挺好",
                    5 to "😊 棒极"
                )

                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    moodsOfSelectedDate.forEach { entry ->
                        val isEditingThis = selectedMoodId == entry.id
                        val cardBg = if (isEditingThis) {
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f)
                        } else {
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                        }
                        val borderStrokeColor = if (isEditingThis) {
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
                        } else {
                            Color.Transparent
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(cardBg)
                                .border(
                                    width = if (isEditingThis) 1.5.dp else 0.dp,
                                    color = borderStrokeColor,
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .clickable { onSelectEntry(entry) }
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Time label
                                Box(
                                    modifier = Modifier
                                        .background(
                                            color = MaterialTheme.colorScheme.primary.copy(0.12f),
                                            shape = RoundedCornerShape(6.dp)
                                        )
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = entry.time,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }

                                Spacer(modifier = Modifier.width(8.dp))

                                Column {
                                    Text(
                                        text = emojiList[entry.rating] ?: "😐 一般",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    if (entry.note.isNotEmpty()) {
                                        Text(
                                            text = entry.note,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                        )
                                    }
                                    if (entry.tags.isNotEmpty()) {
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            entry.tags.split(",").filter { it.isNotEmpty() }.forEach { tag ->
                                                Box(
                                                    modifier = Modifier
                                                        .background(
                                                            color = MaterialTheme.colorScheme.secondaryContainer.copy(0.4f),
                                                            shape = RoundedCornerShape(4.dp)
                                                        )
                                                        .padding(horizontal = 4.dp, vertical = 1.dp)
                                                ) {
                                                    Text(
                                                        text = tag,
                                                        fontSize = 9.sp,
                                                        color = MaterialTheme.colorScheme.onSecondaryContainer
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            // Individual action buttons
                            Row {
                                IconButton(
                                    onClick = { onSelectEntry(entry) },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Edit,
                                        contentDescription = "修改记录详情",
                                        tint = MaterialTheme.colorScheme.primary.copy(0.7f),
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                                IconButton(
                                    onClick = { onDeleteEntry(entry) },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "直接移除此情绪",
                                        tint = MaterialTheme.colorScheme.error.copy(0.7f),
                                        modifier = Modifier.size(16.dp)
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

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun LoggerEditorCard(
    selectedDate: String,
    isToday: Boolean,
    isYesterday: Boolean,
    rating: Int,
    note: String,
    time: String,
    selectedTags: Set<String>,
    selectedMoodId: Long?,
    activityTags: List<com.example.ui.ActivityTag>,
    onRatingChange: (Int) -> Unit,
    onNoteChange: (String) -> Unit,
    onTimeChange: (String) -> Unit,
    onTagToggle: (String) -> Unit,
    onSave: () -> Unit,
    onDelete: () -> Unit,
    onCancelEdit: () -> Unit,
    onResetDate: () -> Unit
) {
    val isEditMode = selectedMoodId != null

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
            // Form title information row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = if (isEditMode) "✏️ 编辑心情记录" else "📝 添加新心情日志",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = if (isEditMode) "保存可直接覆盖记录信息和时间" else "选择描述情绪并指定具体的关联时钟",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }

                if (isEditMode) {
                    TextButton(
                        onClick = onCancelEdit,
                        modifier = Modifier.testTag("cancel_edit_btn")
                    ) {
                        Text("放弃修改", fontSize = 12.sp, color = MaterialTheme.colorScheme.error)
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

            // Interactive Time Picker trigger
            val context = LocalContext.current
            val showTimePicker = {
                val resolvedContext = context.findActivity() ?: context
                val currentHour = time.substringBefore(":").toIntOrNull() ?: 12
                val currentMinute = time.substringAfter(":").toIntOrNull() ?: 0
                try {
                    android.app.TimePickerDialog(
                        resolvedContext,
                        { _, hourOfDay, minute ->
                            val formattedTime = String.format(java.util.Locale.getDefault(), "%02d:%02d", hourOfDay, minute)
                            onTimeChange(formattedTime)
                        },
                        currentHour,
                        currentMinute,
                        true // is24HourView
                    ).show()
                } catch (e: Exception) {
                    try {
                        android.app.TimePickerDialog(
                            context,
                            { _, hourOfDay, minute ->
                                val formattedTime = String.format(java.util.Locale.getDefault(), "%02d:%02d", hourOfDay, minute)
                                onTimeChange(formattedTime)
                            },
                            currentHour,
                            currentMinute,
                            true
                        ).show()
                    } catch (ex: Exception) {
                        Toast.makeText(context, "无法打开时间选择器", Toast.LENGTH_SHORT).show()
                    }
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                    .border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f),
                        shape = RoundedCornerShape(12.dp)
                    )
                    .clickable { showTimePicker() }
                    .padding(horizontal = 14.dp, vertical = 10.dp)
                    .testTag("time_select_button"),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = "选择时钟时间",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "关联时间点 (24小时制)",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                    Text(
                        text = time.ifEmpty { "选择时间" },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                
                Button(
                    onClick = { showTimePicker() },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                        contentColor = MaterialTheme.colorScheme.primary
                    ),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                    modifier = Modifier.height(34.dp).testTag("time_picker_picker_launcher")
                ) {
                    Text("修改时间", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }

            // Quick Tag Activity chips selector
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "这一刻正在做什么呢？(可多选)",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )

                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    activityTags.forEach { tag ->
                        val tagName = tag.name
                        val tagEmoji = tag.emoji
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
                        "这一刻发生了些什么？开心或低落，都分享记下吧 (选填 200字内)",
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
                        text = if (isEditMode) "确认保存修改" else "保存此刻心情",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }
            }
        }
    }
}

@Composable
fun StatisticsCard(allMoods: List<MoodEntry>, activityTags: List<com.example.ui.ActivityTag>) {
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
                text = "了解您过去行为活动如何影响您的心理状态",
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
                                "总心情记录条数",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(0.6f)
                            )
                            Text(
                                "$totalDays 条",
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
                                text = "$count 条 ($percent%)",
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
                            val activityEmoji = activityTags.firstOrNull { it.name == entry.key }?.emoji ?: "🏷️"
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
                                        text = "关联 ${entry.value} 条",
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

private fun android.content.Context.findActivity(): android.app.Activity? {
    var currentContext = this
    while (currentContext is android.content.ContextWrapper) {
        if (currentContext is android.app.Activity) return currentContext
        currentContext = currentContext.baseContext
    }
    return null
}

@Composable
fun BackupSettingsCard(
    onExport: () -> Unit,
    onImport: () -> Unit
) {
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("backup_settings_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Share,
                    contentDescription = "数据备份",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(22.dp)
                )
                Text(
                    text = "系统数据备份与还原",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Text(
                text = "您可以将心情数据导出为本地备份 JSON 文件，或从备份文件中还原日志。数据完全保存在本地，保障您的隐私安全。",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = onExport,
                    modifier = Modifier.weight(1f).testTag("backup_export_btn"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    ),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(imageVector = Icons.Default.KeyboardArrowUp, contentDescription = "导出", modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("导出备份", fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = onImport,
                    modifier = Modifier.weight(1f).testTag("backup_import_btn"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                    ),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(imageVector = Icons.Default.KeyboardArrowDown, contentDescription = "导入", modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("导入还原", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ActivityTagsManagerCard(
    activityTags: List<com.example.ui.ActivityTag>,
    onAddTag: (String, String) -> Boolean,
    onUpdateTag: (String, String, String) -> Boolean,
    onDeleteTag: (String) -> Unit
) {
    var editingTag by remember { mutableStateOf<com.example.ui.ActivityTag?>(null) }
    var tagNameInput by remember { mutableStateOf("") }
    var tagEmojiInput by remember { mutableStateOf("") }
    val context = LocalContext.current

    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("activity_tags_manager_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.List,
                    contentDescription = "活动标签类别",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(22.dp)
                )
                Text(
                    text = "活动图标与类型管理",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Text(
                text = "点击已存在的活动来进行编辑修改，或进行单独删除。图标输入栏推荐填入表情符号（Emoji）。",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f)
            )

            // Dynamic Tag Chips
            FlowRow(
                modifier = Modifier.fillMaxWidth().testTag("tag_manager_chips_flow"),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                activityTags.forEach { tag ->
                    val isSelectedForEdit = editingTag?.name == tag.name
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(
                                if (isSelectedForEdit) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                                else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                            )
                            .border(
                                width = 1.dp,
                                color = if (isSelectedForEdit) MaterialTheme.colorScheme.primary
                                        else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.11f),
                                shape = RoundedCornerShape(16.dp)
                            )
                            .clickable(
                                onClick = {
                                    editingTag = tag
                                    tagNameInput = tag.name
                                    tagEmojiInput = tag.emoji
                                }
                            )
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "${tag.emoji} ", fontSize = 14.sp)
                        Text(
                            text = tag.name,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelectedForEdit) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
            )

            // Add / Edit Form Panel
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f),
                        RoundedCornerShape(12.dp)
                    )
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = if (editingTag != null) "✏️ 修改当前活动" else "➕ 自定义一个新活动",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Emoji text input
                    OutlinedTextField(
                        value = tagEmojiInput,
                        onValueChange = { input ->
                            if (input.length <= 4) {
                                tagEmojiInput = input
                            }
                        },
                        label = { Text("图标") },
                        placeholder = { Text("🍲") },
                        modifier = Modifier.width(85.dp).testTag("manager_emoji_input"),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.15f)
                        )
                    )

                    // Tag name text input
                    OutlinedTextField(
                        value = tagNameInput,
                        onValueChange = { tagNameInput = it },
                        label = { Text("活动名") },
                        placeholder = { Text("例如 骑行") },
                        modifier = Modifier.weight(1f).testTag("manager_name_input"),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.15f)
                        )
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (editingTag != null) {
                        // Delete Button
                        TextButton(
                            onClick = {
                                editingTag?.let {
                                    val name = it.name
                                    onDeleteTag(name)
                                    Toast.makeText(context, "已成功删除活动: $name", Toast.LENGTH_SHORT).show()
                                }
                                editingTag = null
                                tagNameInput = ""
                                tagEmojiInput = ""
                            }
                        ) {
                            Text("🗑️ 直接删除", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        TextButton(
                            onClick = {
                                editingTag = null
                                tagNameInput = ""
                                tagEmojiInput = ""
                            }
                        ) {
                            Text("取消", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        Button(
                            onClick = {
                                val old = editingTag
                                if (old != null) {
                                    val success = onUpdateTag(old.name, tagNameInput, tagEmojiInput)
                                    if (success) {
                                        Toast.makeText(context, "更新活动成功！✨", Toast.LENGTH_SHORT).show()
                                        editingTag = null
                                        tagNameInput = ""
                                        tagEmojiInput = ""
                                    } else {
                                        Toast.makeText(context, "更新失败，重名或字段为空", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            },
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("保存修改")
                        }
                    } else {
                        // Create action button
                        Button(
                            onClick = {
                                val success = onAddTag(tagNameInput, tagEmojiInput)
                                if (success) {
                                    Toast.makeText(context, "添加活动成功！✨", Toast.LENGTH_SHORT).show()
                                    tagNameInput = ""
                                    tagEmojiInput = ""
                                } else {
                                    Toast.makeText(context, "添加失败，重名或字段为空", Toast.LENGTH_SHORT).show()
                                }
                            },
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.testTag("add_tag_action_btn")
                        ) {
                            Text("添加并建立", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
