package com.example.elixir.calendar.data

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.elixir.calendar.network.DietLogDao
import com.example.elixir.utils.DBConverters

@Database(entities = [DietLogEntity::class], version = 1)
@TypeConverters(DBConverters::class)
abstract class DietLogDB : RoomDatabase() {
    abstract fun dietLogDao(): DietLogDao
}