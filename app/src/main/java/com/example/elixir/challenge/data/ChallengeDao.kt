package com.example.elixir.challenge.data

import androidx.room.*

@Dao
interface ChallengeDao {
    @Query("SELECT * FROM challenge_detail")
    suspend fun getAllChallenges(): List<ChallengeDetailEntity>

    @Query("SELECT * FROM challenge_detail WHERE year = :year")
    suspend fun getChallengesByYear(year: Int): List<ChallengeDetailEntity>

    @Query("SELECT * FROM challenge_detail WHERE id = :id")
    suspend fun getChallengeById(id: Int): List<ChallengeDetailEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChallenges(challenges: List<ChallengeDetailEntity>)

    @Update
    suspend fun updateChallenge(challenge: ChallengeDetailEntity)

    @Query("""
        UPDATE challenge_detail
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
    suspend fun deleteChallenge(challenge: ChallengeDetailEntity)
}
