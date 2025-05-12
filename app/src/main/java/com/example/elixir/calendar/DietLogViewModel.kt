package com.example.elixir.calendar

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.elixir.calendar.data.DietLogData

class DietLogViewModel : ViewModel() {
    private val _dietLogData = MutableLiveData<DietLogData>()
    val dietLogData: LiveData<DietLogData> get() = _dietLogData

    fun saveDietLog(data: DietLogData) {
        _dietLogData.value = data
    }
}
