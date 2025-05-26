package com.example.elixir.challenge

import androidx.room.*
import kotlinx.coroutines.flow.Flow

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

    @Delete
    suspend fun deleteChallenge(challenge: ChallengeEntity)
}
