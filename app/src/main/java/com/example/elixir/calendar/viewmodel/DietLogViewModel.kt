package com.example.elixir.calendar.viewmodel

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.elixir.calendar.data.DietLogData
import com.example.elixir.calendar.data.DietLogEntity
import com.example.elixir.calendar.network.db.DietLogRepository
import kotlinx.coroutines.launch

class DietLogViewModel(private val repository: DietLogRepository) : ViewModel() {
    private val _dietLogData = MutableLiveData<DietLogData>()
    private lateinit var dietLogEntity: DietLogEntity           // Room DB에 저장할 객체

    // 식단 기록을 DB에 업로드하는 메서드
    fun saveDietLogDB(data: DietLogData) {
        // 데이터 전송 객체를 Room DB에 저장할 수 있는 객체로 변환
        dietLogEntity = DietLogEntity(
            name = data.dietTitle,
            type = data.dietCategory,
            score = data.score,
            ingredientTagId = data.ingredientTags.map { it.toInt() },
            time = data.time,
            imageUrl = data.dietImg
        )

        // Room DB에 저장
        viewModelScope.launch {
            repository.insertDietLog(dietLogEntity)
            Log.d("RoomTest", "저장된 데이터: $dietLogEntity")
        }
    }
}
