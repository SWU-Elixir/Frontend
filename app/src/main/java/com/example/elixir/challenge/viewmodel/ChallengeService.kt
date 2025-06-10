package com.example.elixir.challenge.viewmodel

import com.example.elixir.challenge.data.ChallengeEntity
import com.example.elixir.challenge.network.ChallengeRepository
import android.util.Log

class ChallengeService(private val repository: ChallengeRepository) {
    companion object {
        private const val TAG = "ChallengeService"
    }

    /**
     * API에서 데이터를 가져오고 실패 시 DB에서 가져오는 공통 로직
     */
    private suspend fun <T> fetchWithFallback(
        apiCall: suspend () -> T,
        dbCall: suspend () -> T,
        errorMessage: String
    ): T {
        return try {
            apiCall()
        } catch (e: Exception) {
            Log.e(TAG, errorMessage, e)
            dbCall()
        }
    }

    suspend fun getChallengesByYear(year: Int): List<ChallengeEntity> {
        return fetchWithFallback(
            apiCall = { repository.fetchAndSaveChallengesByYear(year) },
            dbCall = { repository.getChallengesByYearFromDb(year) },
            errorMessage = "연도별 챌린지 로드 실패"
        )
    }

    suspend fun getChallengesByYearFromDb(year: Int): List<ChallengeEntity> {
        return repository.getChallengesByYearFromDb(year)
    }

    suspend fun getChallengeById(id: Int): List<ChallengeEntity> {
        return fetchWithFallback(
            apiCall = { repository.fetchAndSaveChallengeById(id) },
            dbCall = { repository.getChallengeByIdFromDb(id) },
            errorMessage = "챌린지 상세 정보 로드 실패"
        )
    }

    suspend fun getChallengeByIdFromDb(id: Int): List<ChallengeEntity> {
        return repository.getChallengeByIdFromDb(id)
    }

    suspend fun getChallengeProgress(id: Int): com.example.elixir.challenge.network.ChallengeProgressData {
        return try {
            repository.fetchChallengeProgress(id)
        } catch (e: Exception) {
            Log.e(TAG, "챌린지 진행도 로드 실패", e)
            throw e // 진행도는 DB에 저장되지 않으므로 재시도하지 않음
        }
    }

    suspend fun getChallengeCompletion(): List<ChallengeEntity> {
        return fetchWithFallback(
            apiCall = { repository.fetchChallengeCompletion() },
            dbCall = { repository.getChallengeCompletionFromDb() },
            errorMessage = "챌린지 완료 정보 로드 실패"
        )
    }

    suspend fun getChallengeCompletionFromDb(): List<ChallengeEntity> {
        return repository.getChallengeCompletionFromDb()
    }

    suspend fun getChallengeCompletionRaw(): com.example.elixir.challenge.network.ChallengeCompletionRawData {
        return try {
            repository.fetchChallengeCompletionRaw()
        } catch (e: Exception) {
            Log.e(TAG, "챌린지 완료 원본 데이터 로드 실패", e)
            throw e // 완료 데이터는 DB에 저장되지 않으므로 재시도하지 않음
        }
    }
}

