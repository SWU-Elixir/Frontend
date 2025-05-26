package com.example.elixir.challenge.network

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class IngredientsConverter {
    private val gson = Gson()

    @TypeConverter
    fun fromString(value: String?): List<String>? {
        if (value == null) return null
        return try {
            gson.fromJson(value, object : TypeToken<List<String>>() {}.type)
        } catch (e: Exception) {
            null
        }
    }

    @TypeConverter
    fun toString(list: List<String>?): String? {
        if (list == null) return null
        return try {
            gson.toJson(list)
        } catch (e: Exception) {
            null
        }
    }
}