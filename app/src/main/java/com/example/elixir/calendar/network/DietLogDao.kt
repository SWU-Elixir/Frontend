package com.example.elixir.calendar.network

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.elixir.calendar.data.DietLogEntity

@Dao
interface DietLogDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDietLog(dietLogEntity: DietLogEntity)

    @Query("SELECT * FROM diet_table WHERE id = :id")
    suspend fun getDietLogById(id: Int): DietLogEntity
}