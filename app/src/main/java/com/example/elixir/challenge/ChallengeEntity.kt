package com.example.elixir.challenge

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "challenge")
data class ChallengeEntity(
    @PrimaryKey val id: Int,
    val name: String,
    val description: String,
    val purpose: String,
    val month: Int,
    val year: Int,
    // Goal types
    val step1Goal1Type: String,
    val step1Goal2Type: String,
    val step2Goal1Type: String,
    val step2Goal2Type: String,
    val step3Goal1Type: String,
    val step3Goal2Type: String,
    val step4Goal1Type: String,
    val step4Goal2Type: String,
    // Goal descriptions
    val step1Goal1Desc: String,
    val step1Goal2Desc: String,
    val step2Goal1Desc: String,
    val step2Goal2Desc: String,
    val step3Goal1Desc: String,
    val step3Goal2Desc: String,
    val step4Goal1Desc: String,
    val step4Goal2Desc: String,
    // Achievement info
    val achievementName: String,
    val achievementImageUrl: String,
    val grayAchievementImageUrl: String,
    // Progress tracking
    val step1Goal1Achieved: Boolean = false,
    val step1Goal2Achieved: Boolean = false,
    val step2Goal1Active: Boolean = false,
    val step2Goal2Active: Boolean = false,
    val step2Goal1Achieved: Boolean = false,
    val step2Goal2Achieved: Boolean = false,
    val step3Goal1Active: Boolean = false,
    val step3Goal2Active: Boolean = false,
    val step3Goal1Achieved: Boolean = false,
    val step3Goal2Achieved: Boolean = false,
    val step4Goal1Active: Boolean = false,
    val step4Goal2Active: Boolean = false,
    val step4Goal1Achieved: Boolean = false,
    val step4Goal2Achieved: Boolean = false,
    val challengeCompleted: Boolean = false,
    // Additional fields from other entities
    val ingredients: String? = null, // JSON String으로 저장 (TypeConverter 필요)
    val period: String? = null,
    val completionMessage: String? = null
)

/*
@Entity(tableName = "challengeYear")
data class ChallengeYearEntity(
    @PrimaryKey val id: Int,
    val name: String,
    val month: Int,
    val year: Int,
    val achievementName: String
)

@Entity(tableName = "challengeId")
data class ChallengeIdEntity(
    val ingredients: String?, // JSON String으로 저장 (TypeConverter 필요)
    val name: String,
    val period: String?,
    val description: String?,
    val purpose: String?,
    val step1Goal1Desc: String?,
    val step1Goal2Desc: String?,
    val step2Goal1Desc: String?,
    val step2Goal2Desc: String?,
    val step3Goal1Desc: String?,
    val step3Goal2Desc: String?,
    val step4Goal1Desc: String?,
    val step4Goal2Desc: String?
)

@Entity(tableName = "challengeProgress")
data class ChallengeProgressEntity(
    val challengeId: Int,
    val name: String,
    val year: Int,
    val month: Int,
    val step1Goal1Achieved: Boolean,
    val step1Goal2Achieved: Boolean,
    val step2Goal1Active: Boolean,
    val step2Goal2Active: Boolean,
    val step2Goal1Achieved: Boolean,
    val step2Goal2Achieved: Boolean,
    val step3Goal1Active: Boolean,
    val step3Goal2Active: Boolean,
    val step3Goal1Achieved: Boolean,
    val step3Goal2Achieved: Boolean,
    val step4Goal1Active: Boolean,
    val step4Goal2Active: Boolean,
    val step4Goal1Achieved: Boolean,
    val step4Goal2Achieved: Boolean,
    val challengeCompleted: Boolean
)

@Entity(tableName = "challengeCompletion")
data class ChallengeCompletionEntity(
    val achievementName: String,
    val message: String,
    val achievementImageUrl: String,
    val challengeCompleted: Boolean
)
*/



