package com.example.elixir.calendar.network.db

import androidx.lifecycle.LiveData
import com.example.elixir.calendar.data.DietLogEntity

class DietLogRepository (private val dietLogDao: DietLogDao) {
    // 식단 기록을 DB에 업로드하는 메서드
    suspend fun insertDietLog(dietLogEntity: DietLogEntity) {
        dietLogDao.insertDietLog(dietLogEntity)
    }

    // 모든 식단 기록을 가져오는 메서드
    suspend fun getAllDietLogs(): List<DietLogEntity> {
        return dietLogDao.getAllDietLogs()
    }

    // DietLogRepository.kt
    fun getDietLogsByDate(date: String): LiveData<List<DietLogEntity>> {
        return dietLogDao.getDietLogsByDate(date)
    }
}