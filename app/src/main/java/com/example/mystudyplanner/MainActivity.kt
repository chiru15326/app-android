package com.example.mystudyplanner

import android.Manifest
import android.app.TimePickerDialog
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class MainActivity : ComponentActivity() {
    private val notificationPermission = registerForActivityResult(ActivityResultContracts.RequestPermission()) { }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ReminderScheduler.createChannel(this)
        if (Build.VERSION.SDK_INT >= 33 &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            notificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
        setContent { StudyPlannerApp() }
        // Ensure the default daily reminder is scheduled on the first app launch.
        ReminderScheduler.scheduleNext(this)
    }
}

private enum class Screen { HOME, SCHEDULE, SETTINGS }

@Composable
fun StudyPlannerApp() {
    val context = LocalContext.current
    val prefs = remember { StudyPrefs(context) }
    var dark by remember { mutableStateOf(prefs.isDarkMode()) }
    var screen by remember { mutableStateOf(Screen.HOME) }
    val days = remember { StudyData.load(context) }
    var refresh by remember { mutableIntStateOf(0) }

    val colors = if (dark) darkColorScheme(
        primary = androidx.compose.ui.graphics.Color(0xFF9DBBFF),
        secondary = androidx.compose.ui.graphics.Color(0xFF80CBC4),
        background = androidx.compose.ui.graphics.Color(0xFF101827),
        surface = androidx.compose.ui.graphics.Color(0xFF182235)
    ) else lightColorScheme(
        primary = androidx.compose.ui.graphics.Color(0xFF2457A7),
        secondary = androidx.compose.ui.graphics.Color(0xFF147D75),
        background = androidx.compose.ui.graphics.Color(0xFFF4F7FC),
        surface = androidx.compose.ui.graphics.Color.White
    )

    MaterialTheme(colorScheme = colors) {
        Scaffold(
            containerColor = MaterialTheme.colorScheme.background,
            bottomBar = {
                NavigationBar {
                    NavigationBarItem(selected = screen == Screen.HOME, onClick = { screen = Screen.HOME },
                        icon = { Icon(Icons.Default.Home, null) }, label = { Text("Home") })
                    NavigationBarItem(selected = screen == Screen.SCHEDULE, onClick = { screen = Screen.SCHEDULE },
                        icon = { Icon(Icons.Default.CalendarMonth, null) }, label = { Text("Schedule") })
                    NavigationBarItem(selected = screen == Screen.SETTINGS, onClick = { screen = Screen.SETTINGS },
                        icon = { Icon(Icons.Default.Settings, null) }, label = { Text("Settings") })
                }
            }
        ) { padding ->
            when (screen) {
                Screen.HOME -> HomeScreen(days, prefs, refresh, { refresh++ }, Modifier.padding(padding))
                Screen.SCHEDULE -> ScheduleScreen(days, prefs, refresh, { refresh++ }, Modifier.padding(padding))
                Screen.SETTINGS -> SettingsScreen(prefs, dark, { dark = it; prefs.setDarkMode(it) }, Modifier.padding(padding))
            }
        }
    }
}

@Composable
private fun HomeScreen(days: List<StudyDay>, prefs: StudyPrefs, refresh: Int, onRefresh: () -> Unit, modifier: Modifier = Modifier) {
    val today = LocalDate.now()
    val current = days.firstOrNull { it.date == today } ?: days.firstOrNull { !it.date.isBefore(today) } ?: days.last()
    val completed = days.count { prefs.isCompleted(it.date.toString()) }
    val progress = completed.toFloat() / days.size.coerceAtLeast(1)
    LazyColumn(modifier.fillMaxSize(), contentPadding = PaddingValues(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        item {
            Text("MY STUDY PLANNER", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
            Text("Build your next level.", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            Text("Senior .NET Full Stack • 26-week roadmap", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        item {
            Card(shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
                Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("COURSE PROGRESS", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                    Text("${(progress * 100).toInt()}%", style = MaterialTheme.typography.displaySmall, fontWeight = FontWeight.Bold)
                    LinearProgressIndicator(progress = { progress }, modifier = Modifier.fillMaxWidth())
                    Text("$completed of ${days.size} study days completed")
                }
            }
        }
        item {
            Text("TODAY'S PLAN", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Card(shape = RoundedCornerShape(20.dp)) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(current.date.format(DateTimeFormatter.ofPattern("EEE, dd MMM yyyy")), color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                    Text(current.lesson, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text(current.topic, style = MaterialTheme.typography.bodyLarge)
                    Text("${current.hours} study hours • ${current.availableTime}", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(current.plan, style = MaterialTheme.typography.bodyMedium)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(
                            checked = prefs.isCompleted(current.date.toString()),
                            onCheckedChange = { prefs.setCompleted(current.date.toString(), it); onRefresh() }
                        )
                        Text("Mark today's study complete")
                    }
                }
            }
        }
        item {
            Text("WEEKLY TARGET", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                StatCard("20 hrs", "per week", Modifier.weight(1f))
                StatCard("26 weeks", "course plan", Modifier.weight(1f))
                StatCard("12", "modules", Modifier.weight(1f))
            }
        }
        item {
            Text("NEXT UP", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        }
        items(days.filter { it.date.isAfter(current.date) }.take(3)) { day ->
            DayCard(day, prefs, refresh) { prefs.setCompleted(day.date.toString(), it); onRefresh() }
        }
    }
}

@Composable
private fun ScheduleScreen(days: List<StudyDay>, prefs: StudyPrefs, refresh: Int, onRefresh: () -> Unit, modifier: Modifier = Modifier) {
    var selectedWeek by remember { mutableStateOf("All") }
    val weeks = listOf("All") + days.map { it.week }.distinct()
    Column(modifier.fillMaxSize().padding(horizontal = 16.dp)) {
        Text("Study schedule", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 16.dp, bottom = 8.dp))
        Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            weeks.forEach { week ->
                FilterChip(selected = selectedWeek == week, onClick = { selectedWeek = week }, label = { Text(week) })
            }
        }
        LazyColumn(contentPadding = PaddingValues(bottom = 20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(days.filter { selectedWeek == "All" || it.week == selectedWeek }) { day ->
                DayCard(day, prefs, refresh) { prefs.setCompleted(day.date.toString(), it); onRefresh() }
            }
        }
    }
}

@Composable
private fun DayCard(day: StudyDay, prefs: StudyPrefs, refresh: Int, onToggle: (Boolean) -> Unit) {
    val done = prefs.isCompleted(day.date.toString())
    Card(shape = RoundedCornerShape(18.dp)) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(day.date.format(DateTimeFormatter.ofPattern("EEE, dd MMM")), color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                Text("${day.hours}h", color = MaterialTheme.colorScheme.onSurfaceVariant)
                Checkbox(checked = done, onCheckedChange = onToggle)
            }
            Text(day.lesson, fontWeight = FontWeight.SemiBold)
            Text(day.topic, style = MaterialTheme.typography.bodyMedium)
            Text(day.availableTime, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(day.plan, style = MaterialTheme.typography.bodySmall)
            Text("Target: ${day.output}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.secondary)
        }
    }
}

@Composable
private fun StatCard(value: String, label: String, modifier: Modifier = Modifier) {
    Card(modifier = modifier, shape = RoundedCornerShape(16.dp)) {
        Column(Modifier.padding(12.dp)) {
            Text(value, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleLarge)
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun SettingsScreen(prefs: StudyPrefs, dark: Boolean, onDarkChange: (Boolean) -> Unit, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    var reminders by remember { mutableStateOf(prefs.remindersEnabled()) }
    var reminderTime by remember { mutableStateOf(prefs.reminderTime().format(DateTimeFormatter.ofPattern("hh:mm a"))) }
    Column(modifier.fillMaxSize().padding(20.dp), verticalArrangement = Arrangement.spacedBy(18.dp)) {
        Text("Settings", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Card {
            Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("Dark mode", fontWeight = FontWeight.SemiBold)
                    Text("Switch between light and dark appearance", style = MaterialTheme.typography.bodySmall)
                }
                Switch(checked = dark, onCheckedChange = onDarkChange)
            }
        }
        Card {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Daily study reminder", fontWeight = FontWeight.SemiBold)
                Text("Choose a reminder time. Android may deliver reminders a little later to save battery.")
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Enable reminders", modifier = Modifier.weight(1f))
                    Switch(checked = reminders, onCheckedChange = {
                        reminders = it
                        prefs.setRemindersEnabled(it)
                        ReminderScheduler.scheduleNext(context)
                    })
                }
                OutlinedButton(onClick = {
                    android.app.TimePickerDialog(
                        context,
                        { _, h, m ->
                            prefs.setReminderTime(h, m)
                            reminderTime = java.time.LocalTime.of(h, m).format(DateTimeFormatter.ofPattern("hh:mm a"))
                            if (reminders) ReminderScheduler.scheduleNext(context)
                        },
                        prefs.reminderHour(), prefs.reminderMinute(), false
                    ).show()
                }) { Text("Reminder time: $reminderTime") }
            }
        }
        Text("Your timetable and completion progress are stored locally on this phone. No account or internet connection is required.", style = MaterialTheme.typography.bodySmall)
    }
}
