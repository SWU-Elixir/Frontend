package com.example.elixir.calendar.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.elixir.calendar.network.db.DietLogRepository

class DietLogViewModelFactory(private val repository: DietLogRepository): ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DietLogViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return DietLogViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}