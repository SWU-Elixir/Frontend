package com.example.elixir.challenge.data

import androidx.room.*

@Dao
interface ChallengeDao {
    @Query("SELECT * FROM challenge")
    suspend fun getAllChallenges(): List<ChallengeEntity>

    @Query("SELECT * FROM challenge WHERE year = :year")
    suspend fun getChallengesByYear(year: Int): List<ChallengeEntity>

    @Query("SELECT * FROM challenge WHERE id = :id")
    suspend fun getChallengeById(id: Int): List<ChallengeEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChallenges(challenges: List<ChallengeEntity>)

    @Update
    suspend fun updateChallenge(challenge: ChallengeEntity)

    @Query("""
        UPDATE challenge 
        SET 
            step1Goal1Achieved = :step1Goal1Achieved,
            step1Goal2Achieved = :step1Goal2Achieved,
            step2Goal1Active = :step2Goal1Active,
            step2Goal2Active = :step2Goal2Active,
            step2Goal1Achieved = :step2Goal1Achieved,
            step2Goal2Achieved = :step2Goal2Achieved,
            step3Goal1Active = :step3Goal1Active,
            step3Goal2Active = :step3Goal2Active,
            step3Goal1Achieved = :step3Goal1Achieved,
            step3Goal2Achieved = :step3Goal2Achieved,
            step4Goal1Active = :step4Goal1Active,
            step4Goal2Active = :step4Goal2Active,
            step4Goal1Achieved = :step4Goal1Achieved,
            step4Goal2Achieved = :step4Goal2Achieved
        WHERE id = :id
    """)
    suspend fun updateProgressFields(
        id: Int,
        step1Goal1Achieved: Boolean,
        step1Goal2Achieved: Boolean,
        step2Goal1Active: Boolean,
        step2Goal2Active: Boolean,
        step2Goal1Achieved: Boolean,
        step2Goal2Achieved: Boolean,
        step3Goal1Active: Boolean,
        step3Goal2Active: Boolean,
        step3Goal1Achieved: Boolean,
        step3Goal2Achieved: Boolean,
        step4Goal1Active: Boolean,
        step4Goal2Active: Boolean,
        step4Goal1Achieved: Boolean,
        step4Goal2Achieved: Boolean,
    )


    @Delete
    suspend fun deleteChallenge(challenge: ChallengeEntity)
}
