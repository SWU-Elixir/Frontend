package com.example.elixir.utils

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.lang.reflect.Type
import org.threeten.bp.LocalDateTime
import org.threeten.bp.format.DateTimeFormatter

object DBConverters {
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
        return value.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
    }

    @TypeConverter
    fun toLocalDateTime(value: String?): LocalDateTime? {
        return value?.let {
            LocalDateTime.parse(it, DateTimeFormatter.ISO_LOCAL_DATE_TIME)
        }
    }
}