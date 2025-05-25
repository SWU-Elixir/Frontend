package com.example.elixir.challenge.viewmodel

import com.example.elixir.challenge.data.ChallengeEntity
import com.example.elixir.challenge.network.ChallengeProgress
import com.example.elixir.challenge.network.ChallengeRepository

class ChallengeService(private val repository: ChallengeRepository) {


    suspend fun getChallengesByYear(year: Int): List<ChallengeEntity> {
        return repository.fetchAndSaveChallengesByYear(year)
    }

    suspend fun getChallengesByYearFromDb(year: Int): List<ChallengeEntity> {
        return repository.getChallengesByYearFromDb(year)
    }

    suspend fun getChallengeById(id: Int): List<ChallengeEntity> {
        return repository.fetchAndSaveChallengeById(id)
    }

    suspend fun getChallengeProgress() {
        repository.fetchChallengeProgress()
    }

    suspend fun getChallengeCompletion(): List<ChallengeEntity> {
        return repository.fetchChallengeCompletion()
    }
}

