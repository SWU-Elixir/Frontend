package com.example.elixir.member.data


data class FollowItem(
    val followId: Int,
    val targetMemberId: Int,
    val profileImageRes: String?,
    val memberTitle: String? = null,
    val memberNickname: String?,
    val isFollowing: Boolean = false
)