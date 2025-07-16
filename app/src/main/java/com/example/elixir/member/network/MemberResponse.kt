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

data class ChallengeResponse(
    val status: Int,
    val code: String,
    val message: String,
    val data: List<ChallengeData>
)

data class ChallengeData(
    val year: Int,
    val month: Int,
    val achievementName: String,
    val achievementImageUrl: String,
    val challengeCompleted: Boolean
)

data class AchievementResponse(
    val status: Int,
    val code: String,
    val message: String,
    val data: List<AchievementData>
)

data class AchievementData(
    val achievementName: String,
    val achievementImageUrl: String,
    val completed: Boolean,
    val level: Int,
    val type: String,
    val code: String,
    val description: String
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

data class TitleResponse(
    val status: Int,
    val code: String,
    val message: String,
    val data: TitleData?
)

data class TitleData(
    val memberId: Int,
    val titles: List<String>
)

data class SurveyResponse(
    val status: Int,
    val code: String,
    val message: String,
    val data: SurveyData?
)

data class SurveyData(
    val memberId: Int,
    val allergies: List<String>,
    val mealStyles: List<String>,
    val recipeStyles: List<String>,
    val reasons: List<String>
)

data class EmailResponse(
    val status: Int,
    val code: String,
    val message: String,
    val data: Boolean
)