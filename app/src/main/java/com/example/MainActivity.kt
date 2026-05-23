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
    val resolvedLanguage by viewModel.resolvedLanguage.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        delay(800) // Show stunning launch screen for 0 seconds
        showSplash = false
    }

    Box(modifier = modifier.fillMaxSize()) {
        MoodTrackerApp(viewModel = viewModel, isDark = isDark)

        // Custom full-screen Animated Splash Screen
        AnimatedVisibility(
            visible = showSplash,
            exit = fadeOut(animationSpec = tween(durationMillis = 600)) + shrinkVertically(animationSpec = tween(durationMillis = 600))
        ) {
            SplashScreen(isDark = isDark, resolvedLanguage = resolvedLanguage)
        }
    }
}

@Composable
fun SplashScreen(isDark: Boolean, resolvedLanguage: String) {
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
                    contentDescription = I18n.getText(resolvedLanguage, "splash_content_desc"),
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Text Typography logo
            Text(
                text = I18n.getText(resolvedLanguage, "app_title"),
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = if (isDark) Color.White else Color(0xFF065F46)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = I18n.getText(resolvedLanguage, "splash_sub"),
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
                        icon = { Icon(imageVector = Icons.Default.Edit, contentDescription = I18n.getText(resolvedLanguage, "tab_record")) },
                        label = { Text(I18n.getText(resolvedLanguage, "tab_record"), fontWeight = if (currentTab == 0) FontWeight.Bold else FontWeight.Normal) },
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
                        icon = { Icon(imageVector = Icons.Default.DateRange, contentDescription = I18n.getText(resolvedLanguage, "tab_calendar")) },
                        label = { Text(I18n.getText(resolvedLanguage, "tab_calendar"), fontWeight = if (currentTab == 1) FontWeight.Bold else FontWeight.Normal) },
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
                        icon = { Icon(imageVector = Icons.Default.Info, contentDescription = I18n.getText(resolvedLanguage, "tab_insights")) },
                        label = { Text(I18n.getText(resolvedLanguage, "tab_insights"), fontWeight = if (currentTab == 2) FontWeight.Bold else FontWeight.Normal) },
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
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 0.dp)
                    .testTag("current_date_selection_card"),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f),
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "📅",
                            fontSize = 16.sp,
                            modifier = Modifier.padding(bottom = 2.dp)
                        )
                        Text(
                            text = displayDate,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            maxLines = 1,
                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                        )
                    }

                    if (selectedDate != viewModel.getTodayDateString()) {
                        Button(
                            onClick = { viewModel.selectDate(viewModel.getTodayDateString()) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            ),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                            modifier = Modifier
                                .height(32.dp)
                                .testTag("back_to_today_btn")
                        ) {
                            Text(
                                text = I18n.getText(resolvedLanguage, "back_to_today"),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = I18n.getText(resolvedLanguage, "cal_screen_title"),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(bottom = 4.dp)
        )

        HeatmapCard(
            viewModel = viewModel,
            moodsByDateMap = moodsByDateMap,
            selectedDate = selectedDate,
            resolvedLanguage = resolvedLanguage
        )

        val displayDate = viewModel.formatDisplayDate(selectedDate, resolvedLanguage)
        if (displayDate.isNotEmpty()) {
            Text(
                text = "🗓️ " + I18n.getText(resolvedLanguage, "selected_label") + ": " + displayDate,
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = I18n.getText(resolvedLanguage, "insights_title_tab"),
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
                    contentDescription = I18n.getText(resolvedLanguage, "content_desc_theme_settings"),
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    text = I18n.getText(resolvedLanguage, "theme_settings_title"),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Text(
                text = I18n.getText(resolvedLanguage, "theme_settings_desc"),
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
                    0 to I18n.getText(resolvedLanguage, "theme_mode_system_label"),
                    1 to I18n.getText(resolvedLanguage, "theme_mode_light_label"),
                    2 to I18n.getText(resolvedLanguage, "theme_mode_dark_label")
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
                    contentDescription = I18n.getText(resolvedLanguage, "content_desc_lang_settings"),
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    text = I18n.getText(resolvedLanguage, "lang_settings_card_title"),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            val languages = listOf(
                I18n.LANG_AUTO to I18n.getText(resolvedLanguage, "lang_auto"),
                I18n.LANG_ZH_CN to I18n.getText(resolvedLanguage, "lang_zh_cn_display"),
                I18n.LANG_ZH_TW to I18n.getText(resolvedLanguage, "lang_zh_tw_display"),
                I18n.LANG_EN to I18n.getText(resolvedLanguage, "lang_en_display"),
                I18n.LANG_KO to I18n.getText(resolvedLanguage, "lang_ko_display"),
                I18n.LANG_JA to I18n.getText(resolvedLanguage, "lang_ja_display")
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
                    text = I18n.getText(resolvedLanguage, "streak_days_count").format(streakCount),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = if (streakCount > 0) Color(0xFFD84315) else MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = I18n.getText(resolvedLanguage, "streak_label"),
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
    selectedDate: String,
    resolvedLanguage: String
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
                    text = I18n.getText(resolvedLanguage, "cal_heatmap_title"),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                // Floating indicator showing legend guide
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(I18n.getText(resolvedLanguage, "cal_heatmap_low"), style = MaterialTheme.typography.bodySmall, fontSize = 9.sp)
                    Box(modifier = Modifier.size(8.dp).background(Color(0xFFF87171), RoundedCornerShape(1.5.dp)))
                    Box(modifier = Modifier.size(8.dp).background(Color(0xFF60A5FA), RoundedCornerShape(1.5.dp)))
                    Box(modifier = Modifier.size(8.dp).background(Color(0xFFFBBF24), RoundedCornerShape(1.5.dp)))
                    Box(modifier = Modifier.size(8.dp).background(Color(0xFF34D399), RoundedCornerShape(1.5.dp)))
                    Box(modifier = Modifier.size(8.dp).background(Color(0xFF10B981), RoundedCornerShape(1.5.dp)))
                    Text(I18n.getText(resolvedLanguage, "cal_heatmap_high"), style = MaterialTheme.typography.bodySmall, fontSize = 9.sp)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Calendar heatmap view drawing helper
            val heatmapDates = remember { viewModel.generateHeatmapDates() }
            MoodHeatmap(
                dates = heatmapDates,
                moodsByDate = moodsByDateMap,
                selectedDate = selectedDate,
                resolvedLanguage = resolvedLanguage,
                onDateSelect = { viewModel.selectDate(it) }
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = I18n.getText(resolvedLanguage, "cal_heatmap_tip"),
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
    resolvedLanguage: String,
    onDateSelect: (String) -> Unit
) {
    val scrollState = rememberScrollState()
    val isDark = isSystemInDarkTheme()

    // Automatically scrolls the heatmap chart to show current date on load exactly once
    var hasScrolled by remember { mutableStateOf(false) }
    LaunchedEffect(scrollState.maxValue) {
        if (!hasScrolled && scrollState.maxValue > 0) {
            scrollState.scrollTo(scrollState.maxValue)
            hasScrolled = true
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
            Text(I18n.getText(resolvedLanguage, "cal_day_mon"), style = MaterialTheme.typography.labelSmall, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurface.copy(0.6f), modifier = Modifier.height(cellHeight))
            Spacer(modifier = Modifier.height(cellHeight + spacing)) // Tue skip label
            Text(I18n.getText(resolvedLanguage, "cal_day_wed"), style = MaterialTheme.typography.labelSmall, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurface.copy(0.6f), modifier = Modifier.height(cellHeight))
            Spacer(modifier = Modifier.height(cellHeight + spacing)) // Thu skip label
            Text(I18n.getText(resolvedLanguage, "cal_day_fri"), style = MaterialTheme.typography.labelSmall, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurface.copy(0.6f), modifier = Modifier.height(cellHeight))
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
                    text = I18n.getText(resolvedLanguage, "logged_moods_title_pattern").format(moodsOfSelectedDate.size),
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
                    Icon(imageVector = Icons.Default.Add, contentDescription = I18n.getText(resolvedLanguage, "content_desc_add_note"), modifier = Modifier.size(16.dp))
                    Text(I18n.getText(resolvedLanguage, "btn_add_new"), fontSize = 12.sp, fontWeight = FontWeight.Bold)
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
                        Text(I18n.getText(resolvedLanguage, "no_moods_today_msg1"), fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f))
                        Text(I18n.getText(resolvedLanguage, "no_moods_today_msg2"), fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f))
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
                                        contentDescription = I18n.getText(resolvedLanguage, "content_desc_modify_detail"),
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
                                        contentDescription = I18n.getText(resolvedLanguage, "content_desc_remove_mood"),
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

    val cardTitle = I18n.getText(resolvedLanguage, if (isEditMode) "editor_edit_title" else "editor_add_title")
    val cardDesc = I18n.getText(resolvedLanguage, if (isEditMode) "editor_edit_desc" else "editor_add_desc")
    val cancelText = I18n.getText(resolvedLanguage, "btn_cancel_edit")
    val selectTimeText = I18n.getText(resolvedLanguage, "editor_label_time")
    val changeTimeButtonText = I18n.getText(resolvedLanguage, "btn_change_time")
    val doingWhatText = I18n.getText(resolvedLanguage, "editor_label_doing_what")
    val notePlaceholder = I18n.getText(resolvedLanguage, "editor_placeholder_note")
    val saveButtonText = I18n.getText(resolvedLanguage, if (isEditMode) "btn_confirm_changes" else "btn_save_my_mood")

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
                        val errText = I18n.getText(resolvedLanguage, "err_time_picker_failed")
                        Toast.makeText(context, errText, Toast.LENGTH_SHORT).show()
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
                    contentDescription = I18n.getText(resolvedLanguage, "choose_time"),
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
                        Icon(imageVector = Icons.Default.Delete, contentDescription = I18n.getText(resolvedLanguage, "content_desc_delete_mood_log"))
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
                        contentDescription = I18n.getText(resolvedLanguage, "content_desc_save_mood"),
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
    val cardTitle = I18n.getText(resolvedLanguage, "stats_card_title")
    val cardSub = I18n.getText(resolvedLanguage, "stats_card_subtitle")
    val emptyMsg = I18n.getText(resolvedLanguage, "stats_empty_msg")
    val totalLogsLabel = I18n.getText(resolvedLanguage, "stats_total_logs_label")
    val totalLogsValue = { count: Int -> I18n.getText(resolvedLanguage, "stats_total_logs_pattern").format(count) }
    val avgMoodLabel = I18n.getText(resolvedLanguage, "stats_avg_mood_label")
    val distributionLabel = I18n.getText(resolvedLanguage, "stats_distribution_label")
    val driversLabel = I18n.getText(resolvedLanguage, "stats_drivers_label")
    val associatedLabel = { freq: Int -> I18n.getText(resolvedLanguage, "stats_associated_pattern").format(freq) }

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
                        contentDescription = I18n.getText(resolvedLanguage, "content_desc_empty_analysis"),
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
                    contentDescription = I18n.getText(resolvedLanguage, "content_desc_data_backup"),
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(22.dp)
                )
                Text(
                    text = I18n.getText(resolvedLanguage, "backup_title"),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Text(
                text = I18n.getText(resolvedLanguage, "backup_desc"),
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
                    Icon(imageVector = Icons.Default.KeyboardArrowUp, contentDescription = I18n.getText(resolvedLanguage, "content_desc_export"), modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(I18n.getText(resolvedLanguage, "btn_export"), fontWeight = FontWeight.Bold)
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
                    Icon(imageVector = Icons.Default.KeyboardArrowDown, contentDescription = I18n.getText(resolvedLanguage, "content_desc_import"), modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(I18n.getText(resolvedLanguage, "btn_import"), fontWeight = FontWeight.Bold)
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

    val cardTitle = I18n.getText(resolvedLanguage, "tag_manager_title")
    val cardDesc = I18n.getText(resolvedLanguage, "tag_manager_desc")

    val panelHeader = I18n.getText(resolvedLanguage, if (editingTag != null) "tag_panel_edit" else "tag_panel_add")
    val emojiLabel = I18n.getText(resolvedLanguage, "tag_label_emoji")
    val nameLabel = I18n.getText(resolvedLanguage, "tag_label_name")
    val namePlaceholder = I18n.getText(resolvedLanguage, "tag_placeholder_name")
    val deleteBtnText = I18n.getText(resolvedLanguage, "btn_tag_delete")

    val cancelText = I18n.getText(resolvedLanguage, "btn_tag_cancel")
    val saveBtnText = I18n.getText(resolvedLanguage, "btn_tag_save")
    val createBtnText = I18n.getText(resolvedLanguage, "btn_tag_add")

    val msgDeleted = I18n.getText(resolvedLanguage, "toast_tag_delete_success")
    val msgUpdated = I18n.getText(resolvedLanguage, "toast_tag_update_success")

    val msgUpdateFailed = I18n.getText(resolvedLanguage, "toast_tag_update_error")
    val msgCreated = I18n.getText(resolvedLanguage, "toast_tag_add_success")
    val msgCreateFailed = I18n.getText(resolvedLanguage, "toast_tag_add_error")

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
                    contentDescription = I18n.getText(resolvedLanguage, "content_desc_activity_tag_category"),
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
