package com.example.elixir.utils

import androidx.room.TypeConverter
import com.example.elixir.recipe.data.FlavoringItem
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.lang.reflect.Type
import org.threeten.bp.LocalDateTime
import org.threeten.bp.format.DateTimeFormatter

object DBConverters {
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

    @TypeConverter
    fun fromMap(map: Map<String, String>?): String {
        return Gson().toJson(map)
    }

    @TypeConverter
    fun fromList(list: List<String>?): String {
        return Gson().toJson(list)
    }

    @TypeConverter
    fun toList(json: String): List<String>? {
        val type = object : TypeToken<List<String>>() {}.type
        return Gson().fromJson(json, type)
    }

    @TypeConverter
    fun fromIntList(list: List<Int>?): String = list?.joinToString(",") ?: ""

    @TypeConverter
    fun toIntList(data: String): List<Int> = if (data.isEmpty()) emptyList() else data.split(",").map { it.toInt() }

    @TypeConverter
    fun toStringList(data: String): List<String> = if (data.isEmpty()) emptyList() else data.split(";;")

    @TypeConverter
    fun fromFlavoringDataList(list: List<FlavoringItem>?): String {
        return Gson().toJson(list)
    }

    @TypeConverter
    fun toFlavoringDataList(data: String?): List<FlavoringItem> {
        if (data.isNullOrEmpty()) return emptyList()
        val listType = object : TypeToken<List<FlavoringItem>>() {}.type
        return Gson().fromJson(data, listType)
    }
}