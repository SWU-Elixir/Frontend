package com.example.elixir.calendar.network.db

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.elixir.calendar.data.DietLogEntity
import java.time.LocalDateTime

@Dao
interface DietLogDao {
    // 식단 추가
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDietLog(dietLogEntity: DietLogEntity)

    // id로 식단 기록 가져오기
    @Query("SELECT * FROM diet_table WHERE id = :id")
    suspend fun getDietLogById(id: Int): DietLogEntity

    // 이름으로 식단 기록 가져오기
    @Query("SELECT * FROM diet_table WHERE name = :name")
    suspend fun getDietLogByName(name: String): DietLogEntity

    // 전체 식단 기록 가져오기
    @Query("SELECT * FROM diet_table")
    suspend fun getAllDietLogs(): List<DietLogEntity>

    @Query("SELECT * FROM diet_table WHERE date(time) = :date")
    fun getDietLogsByDate(date: String): LiveData<List<DietLogEntity>>

    // 식단 기록 가져오기 (날짜 기준)
    //@Query("SELECT * FROM diet_table WHERE time == :time")
    //suspend fun getDietLogsByDate(time: LocalDateTime): List<DietLogEntity>

    // 식단 기록 삭제
    @Delete
    suspend fun deleteDietLog(dietLogEntity: DietLogEntity)

}