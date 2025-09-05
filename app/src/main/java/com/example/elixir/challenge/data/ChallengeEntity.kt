package com.example.elixir.challenge.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.example.elixir.challenge.network.IngredientsConverter
import com.google.gson.annotations.SerializedName

@Entity(tableName = "challenge_detail")
@TypeConverters(IngredientsConverter::class)
data class ChallengeDetailEntity(
    @PrimaryKey
    @SerializedName("id") val id: Int,
    @SerializedName("name") val name: String,
    @SerializedName("description") val description: String?,
    @SerializedName("purpose") val purpose: String?,
    @SerializedName("month") val month: Int?,
    @SerializedName("year") val year: Int?,
    // Goal types
    @SerializedName("step1Goal1Type") val step1Goal1Type: String?,
    @SerializedName("step1Goal2Type") val step1Goal2Type: String?,
    @SerializedName("step2Goal1Type") val step2Goal1Type: String?,
    @SerializedName("step2Goal2Type") val step2Goal2Type: String?,
    @SerializedName("step3Goal1Type") val step3Goal1Type: String?,
    @SerializedName("step3Goal2Type") val step3Goal2Type: String?,
    @SerializedName("step4Goal1Type") val step4Goal1Type: String?,
    @SerializedName("step4Goal2Type") val step4Goal2Type: String?,
    // Goal descriptions
    @SerializedName("step1Goal1Desc") val step1Goal1Desc: String?,
    @SerializedName("step1Goal2Desc") val step1Goal2Desc: String?,
    @SerializedName("step2Goal1Desc") val step2Goal1Desc: String?,
    @SerializedName("step2Goal2Desc") val step2Goal2Desc: String?,
    @SerializedName("step3Goal1Desc") val step3Goal1Desc: String?,
    @SerializedName("step3Goal2Desc") val step3Goal2Desc: String?,
    @SerializedName("step4Goal1Desc") val step4Goal1Desc: String?,
    @SerializedName("step4Goal2Desc") val step4Goal2Desc: String?,
    // Achievement info
    @SerializedName("achievementName") val achievementName: String?,
    @SerializedName("achievementImageUrl") val achievementImageUrl: String?,
    @SerializedName("grayAchievementImageUrl") val grayAchievementImageUrl: String?,
    // Progress tracking
    @SerializedName("step1Goal1Achieved") val step1Goal1Achieved: Boolean = false,
    @SerializedName("step1Goal2Achieved") val step1Goal2Achieved: Boolean = false,
    @SerializedName("step2Goal1Active") val step2Goal1Active: Boolean = false,
    @SerializedName("step2Goal2Active") val step2Goal2Active: Boolean = false,
    @SerializedName("step2Goal1Achieved") val step2Goal1Achieved: Boolean = false,
    @SerializedName("step2Goal2Achieved") val step2Goal2Achieved: Boolean = false,
    @SerializedName("step3Goal1Active") val step3Goal1Active: Boolean = false,
    @SerializedName("step3Goal2Active") val step3Goal2Active: Boolean = false,
    @SerializedName("step3Goal1Achieved") val step3Goal1Achieved: Boolean = false,
    @SerializedName("step3Goal2Achieved") val step3Goal2Achieved: Boolean = false,
    @SerializedName("step4Goal1Active") val step4Goal1Active: Boolean = false,
    @SerializedName("step4Goal2Active") val step4Goal2Active: Boolean = false,
    @SerializedName("step4Goal1Achieved") val step4Goal1Achieved: Boolean = false,
    @SerializedName("step4Goal2Achieved") val step4Goal2Achieved: Boolean = false,
    @SerializedName("challengeCompleted") val challengeCompleted: Boolean = false,
    // Additional fields from other entities
    @SerializedName("ingredients") val ingredients: List<String>? = null, // 배열로 변경
    @SerializedName("period") val period: String? = null,
    @SerializedName("completionMessage") val completionMessage: String? = null
)




