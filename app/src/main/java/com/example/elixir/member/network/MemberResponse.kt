package com.example.elixir.member.network

import com.example.elixir.member.data.MemberEntity
import com.example.elixir.member.data.ProfileEntity

data class SignupResponse(
    val status: Int,
    val code: String,
    val message: String
)

// 단일 응답용
data class MemberSingleResponse(
    val status: Int,
    val code: String,
    val message: String,
    val data: MemberEntity
)

data class ProfileResponse(
    val status: Int,
    val code: String,
    val message: String,
    val data: ProfileEntity
)

data class AchievementResponse(
    val status: Int,
    val code: String,
    val message: String,
    val data: List<AchievementData>
)

data class AchievementData(
    val year: Int,
    val month: Int,
    val achievementName: String,
    val achievementImageUrl: String,
    val challengeCompleted: Boolean
)

data class RecipeListResponse(
    val status: Int,
    val code: String,
    val message: String,
    val data: List<RecipeData>
)

data class RecipeData(
    val recipeId: Int,
    val imageUrl: String
)

data class FollowResponse(
    val status: Int,
    val code: String,
    val message: String,
    val data: List<FollowData>
)

data class FollowData(
    val followId: Int,
    val id: Int?,
    val nickname: String,
    val profileUrl: String,
    val title: String
)