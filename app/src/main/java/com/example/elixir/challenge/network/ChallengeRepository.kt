package com.example.elixir.challenge.network

import android.util.Log
import androidx.room.Transaction
import com.example.elixir.challenge.data.ChallengeDao
import com.example.elixir.challenge.data.ChallengeDetailEntity

class ChallengeRepository(
    private val api: ChallengeApi,
    private val challengeDao: ChallengeDao
) {

    @Transaction
    suspend fun fetchAndSaveChallengesByYear(year: Int): List<ChallengeDetailEntity> {
        return try {
            val response = api.getChallengesByYear(year)
            if (response.status == 200) {
                // API에서 직접 ChallengeDetailEntity 리스트를 받아옴
                challengeDao.insertChallenges(response.data)
                response.data
            } else {
                Log.e("ChallengeRepository", "API 호출 실패 - status: ${response.status}, message: ${response.message}")
                getChallengesByYearFromDb(year)
            }
        } catch (e: retrofit2.HttpException) {
            Log.e("ChallengeRepository", "HTTP 오류 (${e.code()}): ${e.message()}", e)
            getChallengesByYearFromDb(year)
        } catch (e: Exception) {
            Log.e("ChallengeRepository", "데이터 저장 실패", e)
            getChallengesByYearFromDb(year)
        }
    }

    @Transaction
    suspend fun fetchAndSaveChallengeById(id: Int): List<ChallengeDetailEntity> {
        return try {
            val response = api.getChallengeById(id)
            if (response.status == 200) {
                // API에서 직접 ChallengeDetailEntity를 받아옴
                challengeDao.insertChallenges(listOf(response.data))
                listOf(response.data)
            } else {
                Log.e("ChallengeRepository", "API 호출 실패 - status: ${response.status}, message: ${response.message}")
                getChallengeByIdFromDb(id)
            }
        } catch (e: retrofit2.HttpException) {
            Log.e("ChallengeRepository", "HTTP 오류 (${e.code()}): ${e.message()}", e)
            getChallengeByIdFromDb(id)
        } catch (e: Exception) {
            Log.e("ChallengeRepository", "데이터 저장 실패", e)
            getChallengeByIdFromDb(id)
        }
    }

    @Transaction
    suspend fun fetchChallengeProgress(id: Int): ChallengeProgressData? {
        return try {
            val response = api.getChallengeProgress(id)
            if (response.status == 200) {
                val progress = response.data
                // DB 업데이트 시도
                try {
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
                } catch (dbException: Exception) {
                    Log.w("ChallengeRepository", "DB 업데이트 실패, API 응답만 반환", dbException)
                }
                progress
            } else {
                Log.e("ChallengeRepository", "progress API 응답 오류 - status: ${response.status}, message: ${response.message}")
                getProgressFromDb(id)
            }
        } catch (e: retrofit2.HttpException) {
            Log.e("ChallengeRepository", "HTTP 오류 (${e.code()}): ${e.message()}", e)
            getProgressFromDb(id)
        } catch (e: Exception) {
            Log.e("ChallengeRepository", "progress 조회 예외", e)
            getProgressFromDb(id)
        }
    }

    @Transaction
    suspend fun fetchChallengeCompletion(): ChallengeCompletionRawData {
        return try {
            val response = api.getChallengeCompletion()
            if (response.status == 200) {
                // completion 데이터를 직접 반환 (DB 저장하지 않음)
                ChallengeCompletionRawData(
                    challengeCompleted = response.data.challengeCompleted,
                    achievementName = response.data.achievementName,
                    achievementImageUrl = response.data.achievementImageUrl
                )
            } else {
                Log.e("ChallengeRepository", "completion API 오류 - status: ${response.status}, message: ${response.message}")
                // API 실패 시 기본값 반환
                ChallengeCompletionRawData()
            }
        } catch (e: retrofit2.HttpException) {
            Log.e("ChallengeRepository", "HTTP 오류 (${e.code()}): ${e.message()}", e)
            // HTTP 오류 시 기본값 반환
            ChallengeCompletionRawData()
        } catch (e: Exception) {
            Log.e("ChallengeRepository", "completion 조회 실패", e)
            // 예외 발생 시 기본값 반환
            ChallengeCompletionRawData()
        }
    }

    // DB에서 progress 정보 조회 (fallback용)
    private suspend fun getProgressFromDb(id: Int): ChallengeProgressData? {
        return try {
            val challenges = challengeDao.getChallengeById(id)
            if (challenges.isNotEmpty()) {
                val challenge = challenges.first()
                ChallengeProgressData(
                    challengeId = challenge.id,
                    step1Goal1Achieved = challenge.step1Goal1Achieved,
                    step1Goal2Achieved = challenge.step1Goal2Achieved,
                    step2Goal1Active = challenge.step2Goal1Active,
                    step2Goal2Active = challenge.step2Goal2Active,
                    step2Goal1Achieved = challenge.step2Goal1Achieved,
                    step2Goal2Achieved = challenge.step2Goal2Achieved,
                    step3Goal1Active = challenge.step3Goal1Active,
                    step3Goal2Active = challenge.step3Goal2Active,
                    step3Goal1Achieved = challenge.step3Goal1Achieved,
                    step3Goal2Achieved = challenge.step3Goal2Achieved,
                    step4Goal1Active = challenge.step4Goal1Active,
                    step4Goal2Active = challenge.step4Goal2Active,
                    step4Goal1Achieved = challenge.step4Goal1Achieved,
                    step4Goal2Achieved = challenge.step4Goal2Achieved,
                    challengeCompleted = challenge.challengeCompleted
                )
            } else {
                Log.w("ChallengeRepository", "DB에서 id $id 챌린지를 찾을 수 없음")
                null
            }
        } catch (e: Exception) {
            Log.e("ChallengeRepository", "DB progress 조회 실패", e)
            null
        }
    }

    // 더 이상 변환 함수가 필요하지 않음 - ChallengeDetailEntity 직접 사용

    suspend fun getChallengesByYearFromDb(year: Int): List<ChallengeDetailEntity> {
        return try {
            val challenges = challengeDao.getChallengesByYear(year)
            Log.d("ChallengeRepository", "Retrieved ${challenges.size} challenges for year $year from database")
            challenges
        } catch (e: Exception) {
            Log.e("ChallengeRepository", "DB 조회 실패", e)
            emptyList()
        }
    }

    suspend fun getChallengeByIdFromDb(id: Int): List<ChallengeDetailEntity> {
        return try {
            val challenges = challengeDao.getChallengeById(id)
            Log.d("ChallengeRepository", "Retrieved challenge with id $id from database")
            challenges
        } catch (e: Exception) {
            Log.e("ChallengeRepository", "DB 조회 실패", e)
            emptyList()
        }
    }

    suspend fun getChallengeCompletionFromDb(): List<ChallengeDetailEntity> {
        return try {
            val challenges = challengeDao.getAllChallenges()
            challenges
        } catch (e: Exception) {
            Log.e("ChallengeRepository", "DB 조회 실패", e)
            emptyList()
        }
    }

    // 단순화된 completion 조회 (이미 fetchChallengeCompletion으로 통합)
    suspend fun fetchChallengeCompletionRaw(): ChallengeCompletionRawData {
        return fetchChallengeCompletion()
    }
}