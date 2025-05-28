package com.example.elixir.member.network

import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.Response
import retrofit2.http.DELETE
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface MemberApi {

    @Multipart
    @POST("/api/member/signup")
    suspend fun signup(
        @Part("dto") dto: RequestBody,
        @Part profileImage: MultipartBody.Part?
    ): SignupResponse

    @GET("/api/member")
    suspend fun getMember() : MemberSingleResponse

    @GET("/api/member/profile")
    suspend fun getProfile() : ProfileResponse

    @GET("/api/member/achievement")
    suspend fun getAchievements(): AchievementResponse

    @GET("/api/member/recipe")
    suspend fun getMyRecipes(): RecipeListResponse

    @GET("/api/member/recipe/scrap")
    suspend fun getScrapRecipes(): RecipeListResponse

    @GET("/api/member/following")
    suspend fun getFollowing(): FollowResponse

    @GET("/api/member/follower")
    suspend fun getFollower(): FollowResponse

    @GET("/api/member/{targetMemberId}/following")
    suspend fun getFollowing(@Path("targetMemberId") id: Int): FollowResponse

    @GET("/api/member/{targetMemberId}/follower")
    suspend fun getFollower(@Path("targetMemberId") id: Int): FollowResponse

    @POST("/api/member/{targetMemberId}/follow")
    suspend fun follow(@Path("targetMemberId") id: Int): Response<Unit>

    @DELETE("/api/member/{targetMemberId}/follow")
    suspend fun unfollow(@Path("targetMemberId") id: Int): Response<Unit>

    @GET("/api/member/achievement/top3")
    suspend fun getTop3Achievements(): AchievementResponse
}