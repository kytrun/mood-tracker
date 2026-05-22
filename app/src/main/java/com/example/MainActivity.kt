package com.example

import com.example.ui.I18n
import com.example.ui.ActivityTag

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
    val resolvedLanguage by viewModel.resolvedLanguage.collectAsStateWithLifecycle()
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
                Toast.makeText(context, I18n.getText(resolvedLanguage, "toast_export_success"), Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(context, "${I18n.getText(resolvedLanguage, "toast_import_fail")}: ${e.message}", Toast.LENGTH_LONG).show()
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
                        Toast.makeText(context, I18n.getText(resolvedLanguage, "toast_import_success"), Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(context, I18n.getText(resolvedLanguage, "toast_import_fail"), Toast.LENGTH_LONG).show()
                    }
                }
            } catch (e: Exception) {
                Toast.makeText(context, "${I18n.getText(resolvedLanguage, "toast_import_fail")}: ${e.message}", Toast.LENGTH_LONG).show()
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
    val resolvedLanguage by viewModel.resolvedLanguage.collectAsStateWithLifecycle()

    val backToTodayText = when (resolvedLanguage) {
        I18n.LANG_EN -> "Today"
        I18n.LANG_KO -> "오늘로"
        I18n.LANG_JA -> "今日へ"
        else -> "回今天"
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        HeaderSection(
            streakCount = streakCount,
            resolvedLanguage = resolvedLanguage
        )

        val displayDate = viewModel.formatDisplayDate(selectedDate, resolvedLanguage)
        if (displayDate.isNotEmpty()) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "📅 " + I18n.getText(resolvedLanguage, "current_date_log") + ": " + displayDate,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                if (selectedDate != viewModel.getTodayDateString()) {
                    TextButton(
                        onClick = { viewModel.selectDate(viewModel.getTodayDateString()) }
                    ) {
                        Text(backToTodayText, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        LoggedMoodsListCard(
            moodsOfSelectedDate = moodsOfSelectedDate,
            selectedMoodId = selectedMoodId,
            onSelectEntry = { viewModel.selectMoodEntry(it) },
            onDeleteEntry = { viewModel.deleteMoodEntry(it.id) },
            onStartNewEntry = { viewModel.selectMoodEntry(null) },
            resolvedLanguage = resolvedLanguage
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
            onResetDate = { viewModel.selectDate(viewModel.getTodayDateString()) },
            resolvedLanguage = resolvedLanguage
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
    val resolvedLanguage by viewModel.resolvedLanguage.collectAsStateWithLifecycle()

    val titleText = when (resolvedLanguage) {
        I18n.LANG_EN -> "Mood Calendar"
        I18n.LANG_KO -> "기분 캘린더"
        I18n.LANG_JA -> "感情のカレンダー"
        I18n.LANG_ZH_TW -> "情緒大屏日曆"
        else -> "情绪大屏日历"
    }

    val selectedLabel = when (resolvedLanguage) {
        I18n.LANG_EN -> "Selected Date"
        I18n.LANG_KO -> "선택한 날짜"
        I18n.LANG_JA -> "選択した日付"
        I18n.LANG_ZH_TW -> "點擊選中"
        else -> "点击选中"
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = titleText,
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

        val displayDate = viewModel.formatDisplayDate(selectedDate, resolvedLanguage)
        if (displayDate.isNotEmpty()) {
            Text(
                text = "🗓️ " + selectedLabel + ": " + displayDate,
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
            },
            resolvedLanguage = resolvedLanguage
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
    val appLanguage by viewModel.appLanguage.collectAsStateWithLifecycle()
    val resolvedLanguage by viewModel.resolvedLanguage.collectAsStateWithLifecycle()

    val titleText = when (resolvedLanguage) {
        I18n.LANG_EN -> "Insights & Settings"
        I18n.LANG_KO -> "분석 및 설정"
        I18n.LANG_JA -> "データ分析と設定"
        I18n.LANG_ZH_TW -> "情緒統計與設置"
        else -> "情绪统计与设置"
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = titleText,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(bottom = 4.dp)
        )

        StatisticsCard(
            allMoods = allMoods,
            activityTags = activityTags,
            resolvedLanguage = resolvedLanguage
        )

        LanguageSettingsCard(
            currentLanguage = appLanguage,
            onLanguageChange = { viewModel.setAppLanguage(it) },
            resolvedLanguage = resolvedLanguage,
            isDark = isDark
        )

        BackupSettingsCard(
            onExport = onExport,
            onImport = onImport,
            resolvedLanguage = resolvedLanguage
        )

        ActivityTagsManagerCard(
            activityTags = activityTags,
            onAddTag = { name, emoji -> viewModel.addActivityTag(name, emoji) },
            onUpdateTag = { oldName, newName, newEmoji -> viewModel.updateActivityTag(oldName, newName, newEmoji) },
            onDeleteTag = { viewModel.deleteActivityTag(it) },
            resolvedLanguage = resolvedLanguage
        )

        ThemeSettingsCard(
            themeMode = themeMode,
            onThemeChange = { viewModel.setThemeMode(it) },
            isDark = isDark,
            resolvedLanguage = resolvedLanguage
        )
    }
}

@Composable
fun ThemeSettingsCard(
    themeMode: Int,
    onThemeChange: (Int) -> Unit,
    isDark: Boolean,
    resolvedLanguage: String
) {
    val titleText = when (resolvedLanguage) {
        I18n.LANG_EN -> "Theme & Colors"
        I18n.LANG_KO -> "테마 설정"
        I18n.LANG_JA -> "テーマカラー設定"
        I18n.LANG_ZH_TW -> "主題顏色設置"
        else -> "主题颜色设置"
    }

    val descText = when (resolvedLanguage) {
        I18n.LANG_EN -> "Switch the application's appearance here. Toggle between Light/Dark mode, or let the app follow system preferences dynamically."
        I18n.LANG_KO -> "앱의 테마 스타일을 전환할 수 있습니다. 라이트/다크 모드를 수동으로 고정하거나 시스템 설정을 따르도록 지정할 수 있습니다."
        I18n.LANG_JA -> "アプリのテーマスタイルを切り替えることができます。ライト・ダークモードを固定するか、システムの設定に自動追従させます。"
        I18n.LANG_ZH_TW -> "可在太一鍵切換應用的主題風格。您可以手動鎖定淺色/深色，或是讓應用智能跟隨手機系統設定，帶來最適意的視覺體驗。"
        else -> "可在此一键切换应用的主题风格。您可以手动锁定浅色/深色，或是让应用智能跟随手机系统设定，带来最体贴舒适的视觉体验。"
    }

    val labelSystem = when (resolvedLanguage) {
        I18n.LANG_EN -> "🖥️ Auto"
        I18n.LANG_KO -> "🖥️ 시스템"
        I18n.LANG_JA -> "🖥️ システム"
        I18n.LANG_ZH_TW -> "🖥️ 跟隨系統"
        else -> "🖥️ 跟随系统"
    }

    val labelLight = when (resolvedLanguage) {
        I18n.LANG_EN -> "☀️ Light"
        I18n.LANG_KO -> "☀️ 라이트"
        I18n.LANG_JA -> "☀️ ライト"
        I18n.LANG_ZH_TW -> "☀️ 活力日間"
        else -> "☀️ 活力日间"
    }

    val labelDark = when (resolvedLanguage) {
        I18n.LANG_EN -> "🌙 Dark"
        I18n.LANG_KO -> "🌙 다크"
        I18n.LANG_JA -> "🌙 ダーク"
        I18n.LANG_ZH_TW -> "🌙 靜謐夜間"
        else -> "🌙 静谧夜间"
    }

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
                    text = titleText,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Text(
                text = descText,
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
                    0 to labelSystem,
                    1 to labelLight,
                    2 to labelDark
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
fun LanguageSettingsCard(
    currentLanguage: String,
    onLanguageChange: (String) -> Unit,
    resolvedLanguage: String,
    isDark: Boolean
) {
    val titleText = when (resolvedLanguage) {
        I18n.LANG_EN -> "Application Language"
        I18n.LANG_KO -> "언어 설정 (Language)"
        I18n.LANG_JA -> "言語設定 (Language)"
        I18n.LANG_ZH_TW -> "多語言設置"
        else -> "多语言设置"
    }

    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("language_settings_card"),
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
                    imageVector = Icons.Default.Menu,
                    contentDescription = "语言设置",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    text = titleText,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            val languages = listOf(
                I18n.LANG_AUTO to I18n.getText(resolvedLanguage, "lang_auto"),
                I18n.LANG_ZH_CN to "简体中文",
                I18n.LANG_ZH_TW to "繁體中文",
                I18n.LANG_EN to "English",
                I18n.LANG_KO to "한국어",
                I18n.LANG_JA to "日本語"
            )

            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                for (row in languages.chunked(2)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        for ((langCode, label) in row) {
                            val isSelected = currentLanguage == langCode
                            val activeBg = if (isDark) Color(0xFF334155) else Color(0xFFECFDF5)
                            val bg = if (isSelected) activeBg else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                            val borderCol = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent
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
                                    .border(1.dp, borderCol, RoundedCornerShape(10.dp))
                                    .clickable { onLanguageChange(langCode) }
                                    .padding(vertical = 10.dp)
                                    .testTag("lang_btn_$langCode"),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = label,
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = textColor,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun HeaderSection(
    streakCount: Int,
    resolvedLanguage: String
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
                text = I18n.getText(resolvedLanguage, "app_title"),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = I18n.getText(resolvedLanguage, "app_sub"),
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
                    text = if (resolvedLanguage == I18n.LANG_EN) "$streakCount Days" else if (resolvedLanguage == I18n.LANG_KO) "$streakCount 일" else if (resolvedLanguage == I18n.LANG_JA) "$streakCount 日" else "$streakCount 天",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = if (streakCount > 0) Color(0xFFD84315) else MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = if (resolvedLanguage == I18n.LANG_EN) "Streak" else if (resolvedLanguage == I18n.LANG_KO) "연속기록" else if (resolvedLanguage == I18n.LANG_JA) "連続打刻" else "连续打卡",
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
    onStartNewEntry: () -> Unit,
    resolvedLanguage: String
) {
    val loggedTitle = when (resolvedLanguage) {
        I18n.LANG_EN -> "Logged Moods (${moodsOfSelectedDate.size})"
        I18n.LANG_KO -> "기록된 기분 (${moodsOfSelectedDate.size})"
        I18n.LANG_JA -> "記録された感情 (${moodsOfSelectedDate.size})"
        I18n.LANG_ZH_TW -> "已記錄心情 (${moodsOfSelectedDate.size})"
        else -> "已记录心情 (${moodsOfSelectedDate.size})"
    }

    val addNewText = when (resolvedLanguage) {
        I18n.LANG_EN -> "Add New"
        I18n.LANG_KO -> "새로 추가"
        I18n.LANG_JA -> "新規追加"
        I18n.LANG_ZH_TW -> "新增一條"
        else -> "新增一条"
    }

    val emptyMsg1 = when (resolvedLanguage) {
        I18n.LANG_EN -> "☕ No mood entries for this day yet"
        I18n.LANG_KO -> "☕ 이날 기록된 기분이 아직 없습니다"
        I18n.LANG_JA -> "☕ この日の感情記録はまだありません"
        I18n.LANG_ZH_TW -> "☕ 這一天還沒有添加任何心情日誌哦"
        else -> "☕ 这一天还没有添加任何心情日志哦"
    }

    val emptyMsg2 = when (resolvedLanguage) {
        I18n.LANG_EN -> "Tap below to log your current feelings!"
        I18n.LANG_KO -> "아래 양식을 탭하여 기분을 기록해 보세요!"
        I18n.LANG_JA -> "下のフォームに入力して感情を記録しましょう！"
        I18n.LANG_ZH_TW -> "點擊下方表單錄入這一刻的心情吧！"
        else -> "点击下方表单录入这一刻的心情吧！"
    }

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
                    text = loggedTitle,
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
                    Text(addNewText, fontSize = 12.sp, fontWeight = FontWeight.Bold)
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
                        Text(emptyMsg1, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f))
                        Text(emptyMsg2, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f))
                    }
                }
            } else {
                val emojiList = mapOf(
                    1 to I18n.getText(resolvedLanguage, "rating_1"),
                    2 to I18n.getText(resolvedLanguage, "rating_2"),
                    3 to I18n.getText(resolvedLanguage, "rating_3"),
                    4 to I18n.getText(resolvedLanguage, "rating_4"),
                    5 to I18n.getText(resolvedLanguage, "rating_5")
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
                                    val ratingDesc = emojiList[entry.rating] ?: I18n.getText(resolvedLanguage, "rating_3")
                                    val ratingEmoji = when(entry.rating) {
                                        5 -> "😊"
                                        4 -> "🙂"
                                        3 -> "😐"
                                        2 -> "🙁"
                                        else -> "😭"
                                    }
                                    Text(
                                        text = "$ratingEmoji $ratingDesc",
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
                                                        text = I18n.getTranslatedTagName(resolvedLanguage, tag),
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
    onResetDate: () -> Unit,
    resolvedLanguage: String
) {
    val isEditMode = selectedMoodId != null

    val cardTitle = if (isEditMode) {
        when (resolvedLanguage) {
            I18n.LANG_EN -> "✏️ Edit Mood Entry"
            I18n.LANG_KO -> "✏️ 기분 기록 편집"
            I18n.LANG_JA -> "✏️ 記録を編集"
            I18n.LANG_ZH_TW -> "✏️ 編輯心情記錄"
            else -> "✏️ 编辑心情记录"
        }
    } else {
        when (resolvedLanguage) {
            I18n.LANG_EN -> "📝 Add Mood Entry"
            I18n.LANG_KO -> "📝 기분 기록하기"
            I18n.LANG_JA -> "📝 感情を記録しにいく"
            I18n.LANG_ZH_TW -> "📝 添加新心情日誌"
            else -> "📝 添加新心情日志"
        }
    }

    val cardDesc = if (isEditMode) {
        when (resolvedLanguage) {
            I18n.LANG_EN -> "Save directly overwrites past notes and time"
            I18n.LANG_KO -> "저장 시 기존의 정보와 시간이 덮어써집니다"
            I18n.LANG_JA -> "保存すると、以前の記録と時間が上書きされます"
            I18n.LANG_ZH_TW -> "保存可直接覆蓋記錄資訊和時間"
            else -> "保存可直接覆盖记录信息和时间"
        }
    } else {
        when (resolvedLanguage) {
            I18n.LANG_EN -> "Select an emoji and set the correct time"
            I18n.LANG_KO -> "기분을 나타내는 이모지를 선택하고 정확한 시간을 지정하세요"
            I18n.LANG_JA -> "絵文字を選択して時間を設定します"
            I18n.LANG_ZH_TW -> "選擇描述情緒並指定具體的關聯時鐘"
            else -> "选择描述情绪并指定具体的关联时钟"
        }
    }

    val cancelText = when (resolvedLanguage) {
        I18n.LANG_EN -> "Cancel"
        I18n.LANG_KO -> "취소"
        I18n.LANG_JA -> "キャンセル"
        I18n.LANG_ZH_TW -> "放棄修改"
        else -> "放弃修改"
    }

    val selectTimeText = when (resolvedLanguage) {
        I18n.LANG_EN -> "Associated Time (24h)"
        I18n.LANG_KO -> "연관된 시간 (24시간제)"
        I18n.LANG_JA -> "関連する時間 (24時間表記)"
        I18n.LANG_ZH_TW -> "關聯時間點 (24小時制)"
        else -> "关联时间点 (24小时制)"
    }

    val changeTimeButtonText = when (resolvedLanguage) {
        I18n.LANG_EN -> "Change"
        I18n.LANG_KO -> "시간 수정"
        I18n.LANG_JA -> "時間変更"
        I18n.LANG_ZH_TW -> "修改時間"
        else -> "修改时间"
    }

    val doingWhatText = when (resolvedLanguage) {
        I18n.LANG_EN -> "What are you doing? (Select multiple)"
        I18n.LANG_KO -> "이 순간 무엇을 하고 있었나요? (중복 선택 가능)"
        I18n.LANG_JA -> "何をしていますか？(複数選択可)"
        I18n.LANG_ZH_TW -> "這一刻正在做什麼呢？(可多選)"
        else -> "这一刻正在做什么呢？(可多选)"
    }

    val notePlaceholder = when (resolvedLanguage) {
        I18n.LANG_EN -> "What's happening? Happy or sad, write down your thoughts..."
        I18n.LANG_KO -> "이 순간 무슨 일이 있었나요? 기쁘거나 슬픈 이야기를 기록해 보세요..."
        I18n.LANG_JA -> "何がありましたか？嬉しかったことや落ち込んだことなど、自由に記述してください..."
        I18n.LANG_ZH_TW -> "這一刻發生了些什麼？開心或低落，都分享記下吧 (選填 200字內)"
        else -> "这一刻发生了些什么？开心或低落，都分享记下吧 (选填 200字内)"
    }

    val saveButtonText = if (isEditMode) {
        when (resolvedLanguage) {
            I18n.LANG_EN -> "Confirm Changes"
            I18n.LANG_KO -> "변경 완료"
            I18n.LANG_JA -> "変更内容を保存"
            I18n.LANG_ZH_TW -> "確認保存修改"
            else -> "确认保存修改"
        }
    } else {
        when (resolvedLanguage) {
            I18n.LANG_EN -> "Save My Mood"
            I18n.LANG_KO -> "기분 기록 저장"
            I18n.LANG_JA -> "感情を記録する"
            I18n.LANG_ZH_TW -> "保存此刻心情"
            else -> "保存此刻心情"
        }
    }

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
                        text = cardTitle,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = cardDesc,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }

                if (isEditMode) {
                    TextButton(
                        onClick = onCancelEdit,
                        modifier = Modifier.testTag("cancel_edit_btn")
                    ) {
                        Text(cancelText, fontSize = 12.sp, color = MaterialTheme.colorScheme.error)
                    }
                }
            }

            // Interactive Mood Selector Row
            val moodsList = listOf(
                1 to ("😭" to I18n.getText(resolvedLanguage, "rating_1")),
                2 to ("🙁" to I18n.getText(resolvedLanguage, "rating_2")),
                3 to ("😐" to I18n.getText(resolvedLanguage, "rating_3")),
                4 to ("🙂" to I18n.getText(resolvedLanguage, "rating_4")),
                5 to ("😊" to I18n.getText(resolvedLanguage, "rating_5"))
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
                        text = selectTimeText,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                    Text(
                        text = time.ifEmpty { "12:00" },
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
                    Text(changeTimeButtonText, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }

            // Quick Tag Activity chips selector
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = doingWhatText,
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
                                text = I18n.getTranslatedTagName(resolvedLanguage, tagName),
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
                        text = notePlaceholder,
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
                        text = saveButtonText,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }
            }
        }
    }
}

@Composable
fun StatisticsCard(
    allMoods: List<MoodEntry>,
    activityTags: List<com.example.ui.ActivityTag>,
    resolvedLanguage: String
) {
    val cardTitle = when (resolvedLanguage) {
        I18n.LANG_EN -> "Mood Trends & Insights"
        I18n.LANG_KO -> "기분 통계 및 인사이트"
        I18n.LANG_JA -> "気分傾向と統計"
        I18n.LANG_ZH_TW -> "心情軌跡統計與洞察"
        else -> "心情轨迹统计与洞察"
    }

    val cardSub = when (resolvedLanguage) {
        I18n.LANG_EN -> "See how your activities dynamically impact your mental state"
        I18n.LANG_KO -> "어떤 활동이 통계적으로 기분에 영향을 주는지 분석하세요"
        I18n.LANG_JA -> "どのような活動があなたの感情に影響しているかを分析します"
        I18n.LANG_ZH_TW -> "瞭解您過去行為活動如何影響您的心理狀態"
        else -> "了解您过去行为活动如何影响您的心理状态"
    }

    val emptyMsg = when (resolvedLanguage) {
        I18n.LANG_EN -> "Not enough mood records to display insights yet"
        I18n.LANG_KO -> "통계를 생성할 만큼의 기분 데이터가 충분하지 않습니다"
        I18n.LANG_JA -> "統計を表示するのに十分なデータがありません"
        I18n.LANG_ZH_TW -> "暫無充足的心情數據來進行圖表統計"
        else -> "暂无充足的心情数据来进行图表统计"
    }

    val totalLogsLabel = when (resolvedLanguage) {
         I18n.LANG_EN -> "Total Mood Logs"
         I18n.LANG_KO -> "기분 기록 수"
         I18n.LANG_JA -> "総感情記録数"
         I18n.LANG_ZH_TW -> "總心情記錄條數"
         else -> "总心情记录条数"
    }

    val totalLogsValue = { count: Int ->
        when (resolvedLanguage) {
            I18n.LANG_EN -> "$count logs"
            I18n.LANG_KO -> "$count 개"
            I18n.LANG_JA -> "$count 件"
            I18n.LANG_ZH_TW -> "$count 條"
            else -> "$count 条"
        }
    }

    val avgMoodLabel = when (resolvedLanguage) {
        I18n.LANG_EN -> "Average Mood Rating"
        I18n.LANG_KO -> "평균 기분 지수"
        I18n.LANG_JA -> "平均感情指数"
        I18n.LANG_ZH_TW -> "平均心情指數"
        else -> "平均心情指数"
    }

    val distributionLabel = when (resolvedLanguage) {
        I18n.LANG_EN -> "Mood Distribution Ratio"
        I18n.LANG_KO -> "기분 상태 분포 비율"
        I18n.LANG_JA -> "感情レベルの構成比率"
        I18n.LANG_ZH_TW -> "心情級別構成比例"
        else -> "心情级别构成比例"
    }

    val driversLabel = when (resolvedLanguage) {
        I18n.LANG_EN -> "Most Frequent Activity Drivers"
        I18n.LANG_KO -> "가장 빈번한 활동 요소"
        I18n.LANG_JA -> "顕著な活動要因"
        I18n.LANG_ZH_TW -> "情緒最頻繁的活動因素"
        else -> "情绪最频繁的活动因素"
    }

    val associatedLabel = { freq: Int ->
        when (resolvedLanguage) {
            I18n.LANG_EN -> "$freq logs"
            I18n.LANG_KO -> "$freq 회 연관"
            I18n.LANG_JA -> "$freq 件関連"
            I18n.LANG_ZH_TW -> "關聯 $freq 條"
            else -> "关联 $freq 条"
        }
    }

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
                text = cardTitle,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = cardSub,
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
                        text = emptyMsg,
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
                                totalLogsLabel,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(0.6f)
                            )
                            Text(
                                totalLogsValue(totalDays),
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
                                avgMoodLabel,
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
                    text = distributionLabel,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                )

                Spacer(modifier = Modifier.height(8.dp))

                val labelMoods = listOf(
                    5 to (I18n.getText(resolvedLanguage, "rating_5") to Color(0xFF10B981)),
                    4 to (I18n.getText(resolvedLanguage, "rating_4") to Color(0xFF34D399)),
                    3 to (I18n.getText(resolvedLanguage, "rating_3") to Color(0xFFFBBF24)),
                    2 to (I18n.getText(resolvedLanguage, "rating_2") to Color(0xFF60A5FA)),
                    1 to (I18n.getText(resolvedLanguage, "rating_1") to Color(0xFFF87171))
                )

                val ratingEmojis = mapOf(
                    5 to "😊",
                    4 to "🙂",
                    3 to "😐",
                    2 to "🙁",
                    1 to "😭"
                )

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    labelMoods.forEach { (index, item) ->
                        val (desc, color) = item
                        val count = ratingCounts[index]
                        val fraction = if (totalDays > 0) count.toFloat() / totalDays else 0f
                        val percent = (fraction * 100).toInt()
                        val emoji = ratingEmojis[index] ?: "😐"

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "$emoji $desc",
                                style = MaterialTheme.typography.bodySmall,
                                fontSize = 11.sp,
                                modifier = Modifier.width(75.dp)
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
                                text = "$count (${percent}%)",
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
                        text = driversLabel,
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
                                        text = I18n.getTranslatedTagName(resolvedLanguage, entry.key),
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = associatedLabel(entry.value),
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
    onImport: () -> Unit,
    resolvedLanguage: String
) {
    val cardTitle = when (resolvedLanguage) {
        I18n.LANG_EN -> "Backup & Restore"
        I18n.LANG_KO -> "데이터 백업 및 복원"
        I18n.LANG_JA -> "バックアップと復元"
        I18n.LANG_ZH_TW -> "系統數據備份與還原"
        else -> "系统数据备份与还原"
    }

    val cardDesc = when (resolvedLanguage) {
        I18n.LANG_EN -> "You can export your mood logs and custom activities as a local JSON backup, or import from an existing file. All data is saved on device for total privacy."
        I18n.LANG_KO -> "기분 기록과 가 활동 목록 정보를 로컬 JSON 파일로 백업하거나 가져와서 복원할 수 있습니다. 모든 개인 정보는 기기에 로컬로만 보호됩니다."
        I18n.LANG_JA -> "感情ログやカスタムアクティビティをローカルのJSON形式でバックアップ、または復元が可能です。データは端末内にのみ保存されるため安心です。"
        I18n.LANG_ZH_TW -> "您可以將心情數據以及自定義活動與表情導出為本地備份 JSON 文件，或從備份文件中還原。數據完全保存在本地端，深度保障您的隱私安全。"
        else -> "您可以将心情数据以及自定义活动与表情导出为本地备份 JSON 文件，或从备份文件中还原。数据完全保存在本地端，深度保障您的隐私安全。"
    }

    val exportText = when (resolvedLanguage) {
        I18n.LANG_EN -> "Export Backup"
        I18n.LANG_KO -> "백업 내보내기"
        I18n.LANG_JA -> "バックアップ出力"
        I18n.LANG_ZH_TW -> "導出備份"
        else -> "导出备份"
    }

    val importText = when (resolvedLanguage) {
        I18n.LANG_EN -> "Import Restore"
        I18n.LANG_KO -> "가져오기 복원"
        I18n.LANG_JA -> "インポート復元"
        I18n.LANG_ZH_TW -> "導入還原"
        else -> "导入还原"
    }

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
                    text = cardTitle,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Text(
                text = cardDesc,
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
                    Text(exportText, fontWeight = FontWeight.Bold)
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
                    Text(importText, fontWeight = FontWeight.Bold)
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
    onDeleteTag: (String) -> Unit,
    resolvedLanguage: String
) {
    var editingTag by remember { mutableStateOf<com.example.ui.ActivityTag?>(null) }
    var tagNameInput by remember { mutableStateOf("") }
    var tagEmojiInput by remember { mutableStateOf("") }
    val context = LocalContext.current

    val cardTitle = when (resolvedLanguage) {
        I18n.LANG_EN -> "Activity Category Setup"
        I18n.LANG_KO -> "활동 카테고리 설정"
        I18n.LANG_JA -> "アクティビティ管理設定"
        I18n.LANG_ZH_TW -> "活動圖標與類型管理"
        else -> "活动图标与类型管理"
    }

    val cardDesc = when (resolvedLanguage) {
        I18n.LANG_EN -> "Tap an existing activity to edit or delete it. Emojis are recommended for custom icons."
        I18n.LANG_KO -> "기존 활동을 탭하여 수정하거나 삭제할 수 있습니다. 이미지는 터치 시 이모지로 지정하는 걸 권장합니다."
        I18n.LANG_JA -> "既存のカテゴリをタップして編集、または削除できます。絵文字を入力するのがおすすめです。"
        I18n.LANG_ZH_TW -> "點擊已存在的活動來進行編輯修改，或進行單獨刪除。圖標輸入欄推薦填入表情符號（Emoji）。"
        else -> "点击已存在的活动来进行编辑修改，或进行单独删除。图标输入栏推荐填入表情符号（Emoji）。"
    }

    val panelHeader = if (editingTag != null) {
        when (resolvedLanguage) {
            I18n.LANG_EN -> "✏️ Modify Current Activity"
            I18n.LANG_KO -> "✏️ 활동 정보 편집"
            I18n.LANG_JA -> "✏️ 選択中カテゴリを編集"
            I18n.LANG_ZH_TW -> "✏️ 修改當前活動"
            else -> "✏️ 修改当前活动"
        }
    } else {
        when (resolvedLanguage) {
            I18n.LANG_EN -> "➕ Define New Activity"
            I18n.LANG_KO -> "➕ 활동 정보 추가"
            I18n.LANG_JA -> "➕ 新規カテゴリ登録"
            I18n.LANG_ZH_TW -> "➕ 自定義一個新活動"
            else -> "➕ 自定义一个新活动"
        }
    }

    val emojiLabel = when (resolvedLanguage) {
        I18n.LANG_EN -> "Icon"
        I18n.LANG_KO -> "아이콘"
        I18n.LANG_JA -> "マーク"
        I18n.LANG_ZH_TW -> "圖標"
        else -> "图标"
    }

    val nameLabel = when (resolvedLanguage) {
        I18n.LANG_EN -> "Name"
        I18n.LANG_KO -> "활동명"
        I18n.LANG_JA -> "名称"
        I18n.LANG_ZH_TW -> "活動名"
        else -> "活动名"
    }

    val namePlaceholder = when (resolvedLanguage) {
        I18n.LANG_EN -> "e.g. running"
        I18n.LANG_KO -> "예: 자전거"
        I18n.LANG_JA -> "例: サイクリング"
        I18n.LANG_ZH_TW -> "例如 騎行"
        else -> "例如 骑行"
    }

    val deleteBtnText = when (resolvedLanguage) {
        I18n.LANG_EN -> "🗑️ Delete"
        I18n.LANG_KO -> "🗑️ 삭제"
        I18n.LANG_JA -> "🗑️ 削除"
        I18n.LANG_ZH_TW -> "🗑️ 直接刪除"
        else -> "🗑️ 直接删除"
    }

    val cancelText = when (resolvedLanguage) {
        I18n.LANG_EN -> "Cancel"
        I18n.LANG_KO -> "취소"
        I18n.LANG_JA -> "キャンセル"
        I18n.LANG_ZH_TW -> "取消"
        else -> "取消"
    }

    val saveBtnText = when (resolvedLanguage) {
        I18n.LANG_EN -> "Save"
        I18n.LANG_KO -> "저장"
        I18n.LANG_JA -> "保存"
        I18n.LANG_ZH_TW -> "保存修改"
        else -> "保存修改"
    }

    val createBtnText = when (resolvedLanguage) {
        I18n.LANG_EN -> "Add & Create"
        I18n.LANG_KO -> "추가 완료"
        I18n.LANG_JA -> "登録する"
        I18n.LANG_ZH_TW -> "添加並建立"
        else -> "添加并建立"
    }

    val msgDeleted = when (resolvedLanguage) {
        I18n.LANG_EN -> "Successfully deleted: "
        I18n.LANG_KO -> "성공적으로 삭제되었습니다: "
        I18n.LANG_JA -> "削除しました: "
        I18n.LANG_ZH_TW -> "已成功刪除活動: "
        else -> "已成功删除活动: "
    }

    val msgUpdated = when (resolvedLanguage) {
        I18n.LANG_EN -> "Updated successfully! ✨"
        I18n.LANG_KO -> "성공적으로 업데이트되었습니다! ✨"
        I18n.LANG_JA -> "更新されました！✨"
        I18n.LANG_ZH_TW -> "更新活動成功！✨"
        else -> "更新活动成功！✨"
    }

    val msgUpdateFailed = when (resolvedLanguage) {
        I18n.LANG_EN -> "Failed to update: empty or duplicate name"
        I18n.LANG_KO -> "업데이트 실패: 중복 또는 입력 빈칸 오류"
        I18n.LANG_JA -> "更新に失敗しました。重複しているか、入力欄が空白です。"
        I18n.LANG_ZH_TW -> "更新失敗，重名或字段為空"
        else -> "更新失败，重名或字段为空"
    }

    val msgCreated = when (resolvedLanguage) {
        I18n.LANG_EN -> "Created successfully! ✨"
        I18n.LANG_KO -> "성공적으로 추가되었습니다! ✨"
        I18n.LANG_JA -> "登録されました！✨"
        I18n.LANG_ZH_TW -> "添加活動成功！✨"
        else -> "添加活动成功！✨"
    }

    val msgCreateFailed = when (resolvedLanguage) {
        I18n.LANG_EN -> "Failed to add: empty or duplicate name"
        I18n.LANG_KO -> "추가 실패: 중복 또는 입력 빈칸 오류"
        I18n.LANG_JA -> "登録に失敗しました。重複しているか、入力欄が空白です。"
        I18n.LANG_ZH_TW -> "添加失敗，重名或欄位為空"
        else -> "添加失败，重名或字段为空"
    }

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
                    text = cardTitle,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Text(
                text = cardDesc,
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
                            text = I18n.getTranslatedTagName(resolvedLanguage, tag.name),
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
                    text = panelHeader,
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
                        label = { Text(emojiLabel) },
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
                        label = { Text(nameLabel) },
                        placeholder = { Text(namePlaceholder) },
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
                                    Toast.makeText(context, "$msgDeleted$name", Toast.LENGTH_SHORT).show()
                                }
                                editingTag = null
                                tagNameInput = ""
                                tagEmojiInput = ""
                            }
                        ) {
                            Text(deleteBtnText, color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        TextButton(
                            onClick = {
                                editingTag = null
                                tagNameInput = ""
                                tagEmojiInput = ""
                            }
                        ) {
                            Text(cancelText, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        Button(
                            onClick = {
                                val old = editingTag
                                if (old != null) {
                                    val success = onUpdateTag(old.name, tagNameInput, tagEmojiInput)
                                    if (success) {
                                        Toast.makeText(context, msgUpdated, Toast.LENGTH_SHORT).show()
                                        editingTag = null
                                        tagNameInput = ""
                                        tagEmojiInput = ""
                                    } else {
                                        Toast.makeText(context, msgUpdateFailed, Toast.LENGTH_SHORT).show()
                                    }
                                }
                            },
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(saveBtnText)
                        }
                    } else {
                        // Create action button
                        Button(
                            onClick = {
                                val success = onAddTag(tagNameInput, tagEmojiInput)
                                if (success) {
                                    Toast.makeText(context, msgCreated, Toast.LENGTH_SHORT).show()
                                    tagNameInput = ""
                                    tagEmojiInput = ""
                                } else {
                                    Toast.makeText(context, msgCreateFailed, Toast.LENGTH_SHORT).show()
                                }
                            },
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.testTag("add_tag_action_btn")
                        ) {
                            Text(createBtnText, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
