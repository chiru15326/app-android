package com.example.mystudyplanner

import android.content.Context
import org.json.JSONArray
import java.time.LocalDate

data class StudyDay(
    val date: LocalDate,
    val day: String,
    val week: String,
    val lesson: String,
    val topic: String,
    val availableTime: String,
    val hours: Int,
    val plan: String,
    val output: String
)

object StudyData {
    fun load(context: Context): List<StudyDay> {
        val json = context.assets.open("schedule.json").bufferedReader().use { it.readText() }
        val arr = JSONArray(json)
        return (0 until arr.length()).map { i ->
            val o = arr.getJSONObject(i)
            StudyDay(
                date = LocalDate.parse(o.getString("date")),
                day = o.getString("day"),
                week = o.getString("week"),
                lesson = o.getString("lesson"),
                topic = o.getString("topic"),
                availableTime = o.getString("availableTime"),
                hours = o.getInt("hours"),
                plan = o.getString("plan"),
                output = o.getString("output")
            )
        }
    }
}
