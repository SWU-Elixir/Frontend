package com.example.elixir.challenge.network

import android.util.Log
import androidx.room.Transaction
import com.example.elixir.challenge.data.ChallengeDao
import com.example.elixir.challenge.data.ChallengeEntity

class ChallengeRepository(
    private val api: ChallengeApi,
    private val challengeDao: ChallengeDao
) {

    @Transaction
    suspend fun fetchAndSaveChallengesByYear(year: Int): List<ChallengeEntity> {
        return try {
            val response = api.getChallengesByYear(year)
            if (response.status == 200) {
                challengeDao.insertChallenges(response.data)
                response.data
            } else {
                Log.e("ChallengeRepository", "API 호출 실패: ${response.message}")
                getChallengesByYearFromDb(year)
            }
        } catch (e: Exception) {
            Log.e("ChallengeRepository", "데이터 저장 실패", e)
            getChallengesByYearFromDb(year)
        }
    }

    @Transaction
    suspend fun fetchAndSaveChallengeById(id: Int): List<ChallengeEntity> {
        return try {
            val response = api.getChallengeById(id)
            if (response.status == 200) {
                challengeDao.insertChallenges(listOf(response.data))
                listOf(response.data)
            } else {
                Log.e("ChallengeRepository", "API 호출 실패: ${response.message}")
                getChallengeByIdFromDb(id)
            }
        } catch (e: Exception) {
            Log.e("ChallengeRepository", "데이터 저장 실패", e)
            getChallengeByIdFromDb(id)
        }
    }

    @Transaction
    suspend fun fetchChallengeProgress(id: Int): com.example.elixir.challenge.network.ChallengeProgressData {
        return try {
            val response = api.getChallengeProgress(id)
            if (response.status == 200) {
                val progress = response.data
                challengeDao.updateProgressFields(
                    id = progress.challengeId,
                    step1Goal1Achieved = progress.step1Goal1Achieved,
                    step1Goal2Achieved = progress.step1Goal2Achieved,
                    step2Goal1Active = progress.step2Goal1Active,
                    step2Goal2Active = progress.step2Goal2Active,
                    step2Goal1Achieved = progress.step2Goal1Achieved,
                    step2Goal2Achieved = progress.step2Goal2Achieved,
                    step3Goal1Active = progress.step3Goal1Active,
                    step3Goal2Active = progress.step3Goal2Active,
                    step3Goal1Achieved = progress.step3Goal1Achieved,
                    step3Goal2Achieved = progress.step3Goal2Achieved,
                    step4Goal1Active = progress.step4Goal1Active,
                    step4Goal2Active = progress.step4Goal2Active,
                    step4Goal1Achieved = progress.step4Goal1Achieved,
                    step4Goal2Achieved = progress.step4Goal2Achieved
                )
                progress
            } else {
                Log.e("ChallengeRepository", "progress 업데이트 실패: ${response.message}")
                throw Exception(response.message)
            }
        } catch (e: Exception) {
            Log.e("ChallengeRepository", "progress 업데이트 예외", e)
            throw e
        }
    }

    @Transaction
    suspend fun fetchChallengeCompletion(): List<ChallengeEntity> {
        return try {
            val response = api.getChallengeCompletion()
            if (response.status == 200) {
                // completion 응답은 ChallengeEntity와 구조가 다르므로 DB에 저장하지 않음
                // UI에서만 사용하고, DB에는 저장하지 않는다
                emptyList() // 또는 필요하다면 적절히 변환해서 반환
            } else {
                Log.e("ChallengeRepository", "API 호출 실패: ${response.message}")
                getChallengeCompletionFromDb()
            }
        } catch (e: Exception) {
            Log.e("ChallengeRepository", "데이터 저장 실패", e)
            getChallengeCompletionFromDb()
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


    suspend fun getChallengeCompletionFromDb(): List<ChallengeEntity> {
        return try {
            val challenges = challengeDao.getAllChallenges()
            challenges
        } catch (e: Exception) {
            Log.e("ChallengeRepository", "DB 조회 실패", e)
            emptyList()
        }
    }

    suspend fun fetchChallengeCompletionRaw(): com.example.elixir.challenge.network.ChallengeCompletionRawData {
        val response = api.getChallengeCompletion()
        // ChallengeCompletionRawData는 completion API의 data 구조와 일치해야 함
        return com.example.elixir.challenge.network.ChallengeCompletionRawData(
            challengeCompleted = response.data.challengeCompleted,
            achievementName = response.data.achievementName,
            achievementImageUrl = response.data.achievementImageUrl
        )
    }

}


