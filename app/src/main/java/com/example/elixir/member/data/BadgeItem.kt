package com.example.elixir.member.data

data class BadgeItem(
    val imageRes: Int,
    val title: String,
    val year: Int?,
    val month: Int?,
    val subtitle: String? = null
) 