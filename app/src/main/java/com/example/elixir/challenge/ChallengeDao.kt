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
    suspend fun getChallengeById(id: Int): ChallengeEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChallenges(challenges: List<ChallengeEntity>)

    @Update
    suspend fun updateChallenge(challenge: ChallengeEntity)

    @Delete
    suspend fun deleteChallenge(challenge: ChallengeEntity)
}

/*
@Dao
interface ChallengeIdDao {
    @Query("SELECT * FROM challengeId")
    suspend fun getChallengeId(): List<ChallengeIdEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChallengeId(challenge: List<ChallengeIdEntity>)
}

@Dao
interface ChallengeYearDao {
    @Query("SELECT * FROM challengeYear")
    suspend fun getChallengeYear(): List<ChallengeYearEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChallengeYear(challenge: List<ChallengeYearEntity>)
}

@Dao
interface ChallengeProgressDao {
    @Query("SELECT * FROM challengeProgress")
    suspend fun getChallengeProgress(): List<ChallengeProgressEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChallengeProgress(challenge: List<ChallengeProgressEntity>)
}

@Dao
interface ChallengeCompletionDao {
    @Query("SELECT * FROM challengeCompletion")
    suspend fun getChallengeCompletion(): List<ChallengeCompletionEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChallengeCompletion(challenge: List<ChallengeCompletionEntity>)
}
*/