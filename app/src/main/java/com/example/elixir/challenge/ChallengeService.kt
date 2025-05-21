package com.example.elixir.challenge

class ChallengeService(private val repository: ChallengeRepository) {
//    suspend fun getAllChallenges(): List<ChallengeEntity> {
//        return repository.fetchAndSaveChallenges()
//    }

    suspend fun getChallengesByYear(year: Int): List<ChallengeEntity> {
        return repository.fetchAndSaveChallengesByYear(year)
    }

    suspend fun getChallengeById(id: Int): List<ChallengeEntity> {
        return repository.fetchAndSaveChallengeById(id)
    }

    suspend fun updateChallenge(challenge: ChallengeEntity) {
        repository.updateChallenge(challenge)
    }

    suspend fun getChallengeProgress(challengeId: Int): List<ChallengeEntity> {
        return repository.fetchChallengeProgress(challengeId)
    }

//    suspend fun getChallengeCompletion(challengeId: Int): List<ChallengeEntity> {
//        return repository.fetchChallengeCompletion(challengeId)
//    }
}

