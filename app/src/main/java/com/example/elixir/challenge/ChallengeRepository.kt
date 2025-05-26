package com.example.elixir.challenge

import android.util.Log

class ChallengeRepository(
    private val api: ChallengeApi,
    private val challengeDao: ChallengeDao
) {
    suspend fun fetchAndSaveChallenges(): List<ChallengeEntity> {
        return try {
            val response = api.getAllChallenges()
            Log.d("ChallengeRepository", "API Response: status=${response.status}, message=${response.message}")
            Log.d("ChallengeRepository", "API Response data size: ${response.data.size}")
            if (response.data.isNotEmpty()) {
                Log.d("ChallengeRepository", "First challenge data: name=${response.data[0].name}, " +
                    "description=${response.data[0].description}, " +
                    "purpose=${response.data[0].purpose}, " +
                    "step1Goal1Desc=${response.data[0].step1Goal1Desc}")
            }
            
            if (response.status == 200) {
                if (response.data.isEmpty()) {
                    Log.w("ChallengeRepository", "API returned empty data list")
                    return getChallengesFromDb()
                }
                
                val challenges = response.data.map { challenge ->
                    // null 값이 있는 필드에 대해 기본값 설정
                    challenge.copy(
                        description = challenge.description ?: "",
                        purpose = challenge.purpose ?: "",
                        month = challenge.month ?: 1,
                        year = challenge.year ?: 2024,
                        step1Goal1Type = challenge.step1Goal1Type ?: "",
                        step1Goal2Type = challenge.step1Goal2Type ?: "",
                        step2Goal1Type = challenge.step2Goal1Type ?: "",
                        step2Goal2Type = challenge.step2Goal2Type ?: "",
                        step3Goal1Type = challenge.step3Goal1Type ?: "",
                        step3Goal2Type = challenge.step3Goal2Type ?: "",
                        step4Goal1Type = challenge.step4Goal1Type ?: "",
                        step4Goal2Type = challenge.step4Goal2Type ?: "",
                        step1Goal1Desc = challenge.step1Goal1Desc ?: "",
                        step1Goal2Desc = challenge.step1Goal2Desc ?: "",
                        step2Goal1Desc = challenge.step2Goal1Desc ?: "",
                        step2Goal2Desc = challenge.step2Goal2Desc ?: "",
                        step3Goal1Desc = challenge.step3Goal1Desc ?: "",
                        step3Goal2Desc = challenge.step3Goal2Desc ?: "",
                        step4Goal1Desc = challenge.step4Goal1Desc ?: "",
                        step4Goal2Desc = challenge.step4Goal2Desc ?: "",
                        achievementName = challenge.achievementName ?: "",
                        achievementImageUrl = challenge.achievementImageUrl ?: "",
                        grayAchievementImageUrl = challenge.grayAchievementImageUrl ?: ""
                    )
                }
                Log.d("ChallengeRepository", "Saving ${challenges.size} challenges to database")
                challengeDao.insertChallenges(challenges)
                Log.d("ChallengeRepository", "Successfully saved challenges to database")
                challenges
            } else {
                Log.e("ChallengeRepository", "API Error: ${response.message}")
                throw Exception("챌린지 API 실패: ${response.message}")
            }
        } catch (e: Exception) {
            Log.e("ChallengeRepository", "Error fetching challenges", e)
            return getChallengesFromDb()
        }
    }

    suspend fun fetchAndSaveChallengesByYear(year: Int): List<ChallengeEntity> {
        return try {
            val response = api.getChallengesByYear(year)
            Log.d("ChallengeRepository", "Year API Response: status=${response.status}, message=${response.message}")
            
            if (response.status == 200) {
                if (response.data.isEmpty()) {
                    Log.w("ChallengeRepository", "API returned empty data list for year $year")
                    return getChallengesByYearFromDb(year)
                }
                
                Log.d("ChallengeRepository", "Saving ${response.data.size} challenges for year $year to database")
                challengeDao.insertChallenges(response.data)
                Log.d("ChallengeRepository", "Successfully saved challenges for year $year to database")
                response.data
            } else {
                Log.e("ChallengeRepository", "Year API Error: ${response.message}")
                throw Exception("챌린지 연도 API 실패: ${response.message}")
            }
        } catch (e: Exception) {
            Log.e("ChallengeRepository", "Error fetching challenges for year $year", e)
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
            val challenges = challengeDao.getAllChallenges()
            Log.d("ChallengeRepository", "Retrieved ${challenges.size} challenges from database")
            challenges
        } catch (e: Exception) {
            Log.e("ChallengeRepository", "DB 조회 실패", e)
            emptyList()
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
                val challenges = response.data.map { challenge ->
                    challenge.copy(
                        description = challenge.description ?: "",
                        purpose = challenge.purpose ?: "",
                        month = challenge.month ?: 1,
                        year = challenge.year ?: 2024,
                        step1Goal1Type = challenge.step1Goal1Type ?: "",
                        step1Goal2Type = challenge.step1Goal2Type ?: "",
                        step2Goal1Type = challenge.step2Goal1Type ?: "",
                        step2Goal2Type = challenge.step2Goal2Type ?: "",
                        step3Goal1Type = challenge.step3Goal1Type ?: "",
                        step3Goal2Type = challenge.step3Goal2Type ?: "",
                        step4Goal1Type = challenge.step4Goal1Type ?: "",
                        step4Goal2Type = challenge.step4Goal2Type ?: "",
                        step1Goal1Desc = challenge.step1Goal1Desc ?: "",
                        step1Goal2Desc = challenge.step1Goal2Desc ?: "",
                        step2Goal1Desc = challenge.step2Goal1Desc ?: "",
                        step2Goal2Desc = challenge.step2Goal2Desc ?: "",
                        step3Goal1Desc = challenge.step3Goal1Desc ?: "",
                        step3Goal2Desc = challenge.step3Goal2Desc ?: "",
                        step4Goal1Desc = challenge.step4Goal1Desc ?: "",
                        step4Goal2Desc = challenge.step4Goal2Desc ?: "",
                        achievementName = challenge.achievementName ?: "",
                        achievementImageUrl = challenge.achievementImageUrl ?: "",
                        grayAchievementImageUrl = challenge.grayAchievementImageUrl ?: ""
                    )
                }
                challengeDao.insertChallenges(challenges)
                challenges
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
                val challenges = response.data.map { challenge ->
                    challenge.copy(
                        description = challenge.description ?: "",
                        purpose = challenge.purpose ?: "",
                        month = challenge.month ?: 1,
                        year = challenge.year ?: 2024,
                        step1Goal1Type = challenge.step1Goal1Type ?: "",
                        step1Goal2Type = challenge.step1Goal2Type ?: "",
                        step2Goal1Type = challenge.step2Goal1Type ?: "",
                        step2Goal2Type = challenge.step2Goal2Type ?: "",
                        step3Goal1Type = challenge.step3Goal1Type ?: "",
                        step3Goal2Type = challenge.step3Goal2Type ?: "",
                        step4Goal1Type = challenge.step4Goal1Type ?: "",
                        step4Goal2Type = challenge.step4Goal2Type ?: "",
                        step1Goal1Desc = challenge.step1Goal1Desc ?: "",
                        step1Goal2Desc = challenge.step1Goal2Desc ?: "",
                        step2Goal1Desc = challenge.step2Goal1Desc ?: "",
                        step2Goal2Desc = challenge.step2Goal2Desc ?: "",
                        step3Goal1Desc = challenge.step3Goal1Desc ?: "",
                        step3Goal2Desc = challenge.step3Goal2Desc ?: "",
                        step4Goal1Desc = challenge.step4Goal1Desc ?: "",
                        step4Goal2Desc = challenge.step4Goal2Desc ?: "",
                        achievementName = challenge.achievementName ?: "",
                        achievementImageUrl = challenge.achievementImageUrl ?: "",
                        grayAchievementImageUrl = challenge.grayAchievementImageUrl ?: ""
                    )
                }
                challengeDao.insertChallenges(challenges)
                challenges
            } else {
                throw Exception("챌린지 완료상태 API 실패: ${response.message}")
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return getChallengeByIdFromDb(challengeId)
        }
    }
}


