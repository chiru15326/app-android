package com.example.mystudyplanner

import android.content.Context
import java.time.LocalTime

class StudyPrefs(context: Context) {
    private val prefs = context.getSharedPreferences("study_planner", Context.MODE_PRIVATE)

    fun isCompleted(date: String): Boolean = prefs.getBoolean("done_$date", false)
    fun setCompleted(date: String, value: Boolean) {
        prefs.edit().putBoolean("done_$date", value).apply()
    }
    fun isDarkMode(): Boolean = prefs.getBoolean("dark_mode", false)
    fun setDarkMode(value: Boolean) { prefs.edit().putBoolean("dark_mode", value).apply() }
    fun remindersEnabled(): Boolean = prefs.getBoolean("reminders", true)
    fun setRemindersEnabled(value: Boolean) { prefs.edit().putBoolean("reminders", value).apply() }
    fun reminderHour(): Int = prefs.getInt("reminder_hour", 7)
    fun reminderMinute(): Int = prefs.getInt("reminder_minute", 0)
    fun setReminderTime(hour: Int, minute: Int) {
        prefs.edit().putInt("reminder_hour", hour).putInt("reminder_minute", minute).apply()
    }
    fun completedCount(): Int = prefs.all.count { (key, value) -> key.startsWith("done_") && value == true }
    fun reminderTime(): LocalTime = LocalTime.of(reminderHour(), reminderMinute())
}
