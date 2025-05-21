package com.example.elixir.challenge

import android.util.Log

class ChallengeRepository(
    private val api: ChallengeApi,
    private val challengeDao: ChallengeDao
) {
    suspend fun fetchAndSaveChallenges(): List<ChallengeEntity> {
        return try {
            val response = api.getAllChallenges()
            if (response.status == 200) {
                challengeDao.insertChallenges(response.data)
                response.data
            } else {
                throw Exception("챌린지 API 실패: ${response.message}")
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return getChallengesFromDb()
        }
    }

    suspend fun fetchAndSaveChallengesByYear(year: Int): List<ChallengeEntity> {
        return try {
            val response = api.getChallengesByYear(year)
            if (response.status == 200) {
                challengeDao.insertChallenges(response.data)
                response.data
            } else {
                throw Exception("챌린지 연도 API 실패: ${response.message}")
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return getChallengesByYearFromDb(year)
        }
    }

    suspend fun fetchAndSaveChallengeById(id: Int): List<ChallengeEntity> {
        return try {
            val response = api.getChallengeById(id)
            if (response.status == 200) {
                challengeDao.insertChallenges(response.data)
                response.data
            } else {
                throw Exception("챌린지 연도 API 실패: ${response.message}")
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return getChallengeByIdFromDb(id)
        }
    }

    suspend fun getChallengesFromDb(): List<ChallengeEntity> {
        return try {
            challengeDao.getAllChallenges()
        } catch (e: Exception) {
            Log.e("ChallengeRepository", "DB 조회 실패", e)
            emptyList()
        }
    }

    suspend fun getChallengesByYearFromDb(year: Int): List<ChallengeEntity> {
        return try {
            challengeDao.getChallengesByYear(year)
        } catch (e: Exception) {
            Log.e("ChallengeRepository", "DB 조회 실패", e)
            emptyList()
        }
    }

    suspend fun getChallengeByIdFromDb(id: Int): List<ChallengeEntity> {
        return try {
            challengeDao.getChallengeById(id)
        } catch (e: Exception) {
            Log.e("ChallengeRepository", "DB 조회 실패", e)
            emptyList()
        }
    }

    suspend fun updateChallenge(challenge: ChallengeEntity) {
        try {
            challengeDao.updateChallenge(challenge)
        } catch (e: Exception) {
            Log.e("ChallengeRepository", "챌린지 업데이트 실패", e)
        }
    }

    suspend fun fetchChallengeProgress(challengeId: Int): List<ChallengeEntity> {
        return try {
            val response = api.getChallengeProgress(challengeId)
            if (response.status == 200) {
                challengeDao.insertChallenges(response.data)
                response.data
            } else {
                throw Exception("챌린지 진행상황 API 실패: ${response.message}")
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return getChallengeByIdFromDb(challengeId)
        }
    }

    suspend fun fetchChallengeCompletion(challengeId: Int): List<ChallengeEntity> {
        return try {
            val response = api.getChallengeCompletion(challengeId)
            if (response.status == 200) {
                challengeDao.insertChallenges(response.data)
                response.data
            } else {
                throw Exception("챌린지 완료상태 API 실패: ${response.message}")
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return getChallengeByIdFromDb(challengeId)
        }
    }
}


