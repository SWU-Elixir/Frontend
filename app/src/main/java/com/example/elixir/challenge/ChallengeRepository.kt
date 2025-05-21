package com.example.elixir.challenge

import android.util.Log
import androidx.room.Transaction

class ChallengeRepository(
    private val api: ChallengeApi,
    private val challengeDao: ChallengeDao
) {

    @Transaction
    suspend fun fetchAndSaveChallengesByYear(year: Int): List<ChallengeEntity> {
        try {
            val response = api.getChallengesByYear(year)
            if (response.status == 200) {
                fetchAndSaveChallengesByYear(year)
                challengeDao.insertChallenges(response.data)
                return response.data
            }
            throw Exception("API 호출 실패: ${response.message}")
        } catch (e: Exception) {
            Log.e("ChallengeRepository", "데이터 저장 실패", e)
            // DB에 저장된 데이터 반환
            return getChallengesByYearFromDb(year)
        }
    }

    @Transaction
    suspend fun fetchAndSaveChallengeById(id: Int): List<ChallengeEntity> {
        try {
            val response = api.getChallengeById(id)
            if (response.status == 200) {
                fetchAndSaveChallengeById(id)
                challengeDao.insertChallenges(response.data)
                return response.data
            }
            throw Exception("API 호출 실패: ${response.message}")
        } catch (e: Exception) {
            Log.e("ChallengeRepository", "데이터 저장 실패", e)
            // DB에 저장된 데이터 반환
            return emptyList()
        }
    }

    @Transaction
    suspend fun fetchChallengeProgress(id: Int): List<ChallengeEntity> {
        try {
            val response = api.getChallengeProgress(id)
            if (response.status == 200) {
                fetchChallengeProgress(id)
                challengeDao.insertChallenges(response.data)
                return response.data
            }
            throw Exception("API 호출 실패: ${response.message}")
        } catch (e: Exception) {
            Log.e("ChallengeRepository", "데이터 저장 실패", e)
            // DB에 저장된 데이터 반환
            return emptyList()
        }
    }


    suspend fun getChallengesByYearFromDb(year: Int): List<ChallengeEntity> {
        return try {
            val challenges = challengeDao.getChallengesByYear(year)
            Log.d("ChallengeRepository", "Retrieved ${challenges.size} challenges for year $year from database")
            challenges
        } catch (e: Exception) {
            Log.e("ChallengeRepository", "DB 조회 실패", e)
            emptyList()
        }
    }

    suspend fun getChallengeByIdFromDb(id: Int): List<ChallengeEntity> {
        return try {
            val challenges = challengeDao.getChallengeById(id)
            Log.d("ChallengeRepository", "Retrieved challenge with id $id from database")
            challenges
        } catch (e: Exception) {
            Log.e("ChallengeRepository", "DB 조회 실패", e)
            emptyList()
        }
    }

    suspend fun updateChallenge(challenge: ChallengeEntity) {
        try {
            Log.d("ChallengeRepository", "Updating challenge ${challenge.id}")
            challengeDao.updateChallenge(challenge)
            Log.d("ChallengeRepository", "Successfully updated challenge ${challenge.id}")
        } catch (e: Exception) {
            Log.e("ChallengeRepository", "챌린지 업데이트 실패", e)
            throw e
        }
    }
}


