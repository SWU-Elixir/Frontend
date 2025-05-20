package com.example.elixir.challenge

class ChallengeService(private val repository: ChallengeRepository) {
    suspend fun getAllChallenges(): List<ChallengeEntity> {
        return repository.fetchAndSaveChallenges()
    }

    suspend fun getChallengesByYear(year: Int): List<ChallengeEntity> {
        return repository.fetchAndSaveChallengesByYear(year)
    }

    suspend fun updateChallenge(challenge: ChallengeEntity) {
        repository.updateChallenge(challenge)
    }
}

/*
class ChallengeService(private val repository: ChallengeRepository) {
    suspend fun getChallengeId(): List<ChallengeIdEntity> {
        return repository.fetchAndSaveChallengeId()
    }

    suspend fun getChallengeYear(): List<ChallengeYearEntity> {
        return repository.fetchAndSaveChallengeYear()
    }

    suspend fun getChallengeProgress(): List<ChallengeProgressEntity> {
        return repository.fetchAndSaveChallengeProgress()
    }

    suspend fun getChallengeCompletion(): List<ChallengeCompletionEntity> {
        return repository.fetchAndSaveChallengeCompletion()
    }
}
*/