package com.example.elixir.member.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "member")
data class MemberEntity (
    @PrimaryKey val id: Int,
    val email: String,
    val nickname: String?,
    val gender: String?,
    val birthYear: Int?,
    val profileUrl: String?
)

@Entity(tableName = "profile")
data class ProfileEntity (
    @PrimaryKey val id: Int,
    val nickname: String?,
    val title: String?,
    val profileUrl: String?,
    val followerCount: Int,
    val followingCount: Int
)

@Entity(tableName = "challenge")
data class ChallengeEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val year: Int?,
    val month: Int?,
    val achievementName: String?,
    val achievementImageUrl: String?,
    val challengeCompleted: Boolean?
)

@Entity(tableName = "achievement")
data class AchievementEntity(
    @PrimaryKey val code: String,
    val achievementName: String,
    val description: String,
    val achievementImageUrl: String,
    val completed: Boolean,
    val level: Int,
    val type: String
)

@Entity(tableName = "recipe")
data class RecipeEntity(
    @PrimaryKey val recipeId: Int,
    val imageUrl: String
)

@Entity(tableName = "follow")
data class FollowEntity(
    @PrimaryKey val followId: Int,
    val id: Int?,
    val nickname: String?,
    val profileUrl: String?,
    val title: String?
)