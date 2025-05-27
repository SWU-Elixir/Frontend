package com.example.elixir.calendar.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.elixir.calendar.network.db.DietLogRepository
import com.example.elixir.member.network.MemberRepository

class MealViewModelFactory(
    private val dietRepository: DietLogRepository,
    private val memberRepository: MemberRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MealViewModel::class.java)) {
            return MealViewModel(dietRepository, memberRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
