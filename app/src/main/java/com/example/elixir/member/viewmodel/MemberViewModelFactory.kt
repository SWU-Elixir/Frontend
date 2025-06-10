package com.example.elixir.member.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.elixir.member.network.MemberRepository

class MemberViewModelFactory(
    private val service: MemberService
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MemberViewModel::class.java)) {
            return MemberViewModel(service) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

