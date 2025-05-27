package com.example.elixir.member.viewmodel

import com.example.elixir.member.data.AchievementEntity
import com.example.elixir.member.data.MemberEntity
import com.example.elixir.member.network.MemberRepository
import com.example.elixir.member.data.FollowEntity
import com.example.elixir.member.data.RecipeEntity
import com.example.elixir.signup.SignupRequest
import java.io.File

class MemberService (
    private val repository: MemberRepository
) {
    suspend fun getMember(): MemberEntity? {
        return repository.fetchAndSaveMember()
    }

    suspend fun getMemberFromDb(): MemberEntity? {
        return repository.getMemberFromDb()
    }

    suspend fun getAchievement(): List<AchievementEntity> {
        return repository.fetchAndSaveAchievements()
    }

    suspend fun getAchievementFromDb(): List<AchievementEntity> {
        return repository.getAchievementsFromDb()
    }

    suspend fun getTop3Achievements(): List<AchievementEntity> {
        return repository.fetchAndSaveTop3Achievements()
    }
    suspend fun getTop3AchievementsFromDb(): List<AchievementEntity> {
        return repository.getTop3AchievementsFromDb()
    }

    suspend fun getFollowing(targetMemberId: Int): List<FollowEntity> {
        return repository.fetchAndSaveIdFollowing(targetMemberId)
    }

    suspend fun getFollowingFromDb(targetMemberId: Int): List<FollowEntity> {
        return repository.getIdFollowingFromDb(targetMemberId)
    }

    suspend fun getFollower(targetMemberId: Int): List<FollowEntity> {
        return repository.fetchAndSaveIdFollower(targetMemberId)
    }

    suspend fun getFollowerFromDb(targetMemberId: Int): List<FollowEntity> {
        return repository.getIdFollowerFromDb(targetMemberId)
    }

    // targetMemberId는 API 응답의 id 필드를 사용해야 함
    suspend fun follow(targetMemberId: Int): Boolean {
        return repository.follow(targetMemberId)
    }

    suspend fun unfollow(targetMemberId: Int): Boolean {
        return repository.unfollow(targetMemberId)
    }

    suspend fun signup(signupRequest: SignupRequest, profileImageFile: File?): Any? {
        return repository.signup(signupRequest, profileImageFile)
    }

    suspend fun getMyRecipes() : List<RecipeEntity> {
        return repository.fetchAndSaveMyRecipes()
    }

    suspend fun getMyRecipesFromDb() : List<RecipeEntity> {
        return repository.getMyRecipesFromDb()
    }

    suspend fun getScrapRecipes() : List<RecipeEntity> {
        return repository.fetchAndSaveScrapRecipes()
    }

    suspend fun getScrapRecipesFromDb() : List<RecipeEntity> {
        return repository.getScrapRecipesFromDb()
    }

}