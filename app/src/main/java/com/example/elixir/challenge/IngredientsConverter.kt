package com.example.elixir.challenge

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class IngredientsConverter {
    @TypeConverter
    fun fromString(value: String?): List<String>? {
        return value?.let {
            Gson().fromJson(it, object : TypeToken<List<String>>() {}.type)
        }
    }

    @TypeConverter
    fun listToString(list: List<String>?): String? {
        return Gson().toJson(list)
    }
}