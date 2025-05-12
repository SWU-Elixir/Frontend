package com.example.elixir.utils

import android.os.Build
import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.lang.reflect.Type
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class DBConverters {
    private val formatter = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        DateTimeFormatter.ISO_LOCAL_DATE_TIME
    } else {
        null
    }

    private val legacyFormatter = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", java.util.Locale.getDefault())

    @TypeConverter
    fun fromIngredientTagIdList(value: List<Int>): String {
        return Gson().toJson(value)
    }

    @TypeConverter
    fun toIngredientTagIdList(value: String): List<Int> {
        val listType: Type = object : TypeToken<List<Int>>() {}.type
        return Gson().fromJson(value, listType)
    }

    @TypeConverter
    fun fromLocalDateTime(value: LocalDateTime): String {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            value.format(formatter)
        } else {
            val dateTimeString = value.toString() // "yyyy-MM-ddTHH:mm:ss" 형식
            val parts = dateTimeString.split("T")
            val dateParts = parts[0].split("-")
            val timeParts = parts[1].split(":")

            val calendar = java.util.Calendar.getInstance()
            calendar.set(java.util.Calendar.YEAR, dateParts[0].toInt())
            calendar.set(java.util.Calendar.MONTH, dateParts[1].toInt() - 1)
            calendar.set(java.util.Calendar.DAY_OF_MONTH, dateParts[2].toInt())
            calendar.set(java.util.Calendar.HOUR_OF_DAY, timeParts[0].toInt())
            calendar.set(java.util.Calendar.MINUTE, timeParts[1].toInt())
            calendar.set(java.util.Calendar.SECOND, timeParts[2].toInt())
            legacyFormatter.format(calendar.time)
        }
    }

    @TypeConverter
    fun toLocalDateTime(value: String): String {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            LocalDateTime.parse(value, formatter).toString()
        } else {
            val date = legacyFormatter.parse(value)
            val calendar = java.util.Calendar.getInstance()
            calendar.time = date
            "${calendar.get(java.util.Calendar.YEAR)}-" +
                    "${calendar.get(java.util.Calendar.MONTH) + 1}-" +
                    "${calendar.get(java.util.Calendar.DAY_OF_MONTH)}T" +
                    "${calendar.get(java.util.Calendar.HOUR_OF_DAY)}:" +
                    "${calendar.get(java.util.Calendar.MINUTE)}:" +
                    "${calendar.get(java.util.Calendar.SECOND)}"
        }
    }
}