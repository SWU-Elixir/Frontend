package com.example.elixir

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class MapConverter {
    @TypeConverter
    fun fromMap(map: Map<String, String>?): String {
        return Gson().toJson(map)
    }

    @TypeConverter
    fun toMap(json: String): Map<String, String>? {
        val type = object : TypeToken<Map<String, String>>() {}.type
        return Gson().fromJson(json, type)
    }
}