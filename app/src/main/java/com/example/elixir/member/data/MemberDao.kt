package com.example.elixir.member.data

import androidx.room.*

@Dao
interface MemberDao {
    @Query("SELECT * FROM member")
    suspend fun getMember() : MemberEntity

    @Query("SELECT * FROM profile")
    suspend fun getProfile() : ProfileEntity

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMember(member: MemberEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProfile(member: ProfileEntity)

    @Query("SELECT * FROM achievement")
    suspend fun getAchievements(): List<AchievementEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAchievements(achievements: List<AchievementEntity>)

    @Query("SELECT * FROM recipe")
    suspend fun getRecipes(): List<RecipeEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecipes(recipes: List<RecipeEntity>)

    @Query("SELECT * FROM follow")
    suspend fun getFollow(): List<FollowEntity>

    @Query("SELECT * FROM follow WHERE id = :memberId")
    suspend fun getFollowByMemberId(memberId: Int): List<FollowEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFollows(follows: List<FollowEntity>)
}