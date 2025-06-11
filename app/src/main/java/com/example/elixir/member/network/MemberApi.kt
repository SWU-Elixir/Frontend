package com.example.elixir.member.network

import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.*

interface MemberApi {

    // ---------------------
    // 회원가입 및 인증 관련
    // ---------------------

    @Multipart
    @POST("/api/member/signup")
    suspend fun signup(
        @Part("dto") dto: RequestBody,
        @Part profileImage: MultipartBody.Part?
    ): SignupResponse

    @POST("/api/member/email-verification")
    suspend fun emailVerification(
        @Body body: RequestBody
    ): SignupResponse

    @POST("/api/member/email-verification/verify")
    suspend fun emailVerify(
        @Body body: RequestBody
    ): SignupResponse

    @GET("/api/member/check-email")
    suspend fun getCheckEmail(
        @Query("email") email: String
    ): EmailResponse

    @PUT("/api/member/update-password")
    suspend fun putUpdatePassword(
        @Body body: RequestBody
    ): Response<Unit>

    @DELETE("/api/member/withdrawal")
    suspend fun withdrawal(): Response<Unit>


    // ---------------------
    // 회원 정보 관련
    // ---------------------

    @GET("/api/member")
    suspend fun getMember(): MemberSingleResponse

    @GET("/api/member/profile")
    suspend fun getProfile(): ProfileResponse

    @GET("/api/member/{memberId}/profile")
    suspend fun getProfile(
        @Path("memberId") id: Int
    ): ProfileResponse

    @Multipart
    @PATCH("/api/member/profile")
    suspend fun patchProfile(
        @Part("dto") dto: RequestBody,
        @Part profileImage: MultipartBody.Part?
    ): Response<Unit>


    // ---------------------
    // 설문 관련
    // ---------------------

    @GET("/api/member/survey")
    suspend fun getSurvey(): SurveyResponse

    @PUT("/api/member/survey")
    suspend fun putSurvey(
        @Body body: RequestBody
    ): SurveyResponse


    // ---------------------
    // 업적 관련
    // ---------------------

    @GET("/api/member/achievement")
    suspend fun getAchievements(): AchievementResponse

    @GET("/api/member/achievement/top3")
    suspend fun getTop3Achievements(): AchievementResponse

    @GET("/api/member/{memberId}/achievements")
    suspend fun getAchievements(
        @Path("memberId") id: Int
    ): AchievementResponse

    @GET("/api/member/{memberId}/achievements/top3")
    suspend fun getTop3Achievements(
        @Path("memberId") id: Int
    ): AchievementResponse

    @GET("/api/member/achievement/title")
    suspend fun getTitle(): TitleResponse


    // ---------------------
    // 레시피 관련
    // ---------------------

    @GET("/api/member/recipe")
    suspend fun getMyRecipes(): RecipeListResponse

    @GET("/api/member/{memberId}/recipes")
    suspend fun getMyRecipes(
        @Path("memberId") id: Int
    ): RecipeListResponse

    @GET("/api/member/recipe/scrap")
    suspend fun getScrapRecipes(): RecipeListResponse


    // ---------------------
    // 팔로우 관련
    // ---------------------

    @GET("/api/member/following")
    suspend fun getFollowing(): FollowResponse

    @GET("/api/member/follower")
    suspend fun getFollower(): FollowResponse

    @GET("/api/member/{targetMemberId}/following")
    suspend fun getFollowing(
        @Path("targetMemberId") id: Int
    ): FollowResponse

    @GET("/api/member/{targetMemberId}/follower")
    suspend fun getFollower(
        @Path("targetMemberId") id: Int
    ): FollowResponse

    @POST("/api/member/{targetMemberId}/follow")
    suspend fun follow(
        @Path("targetMemberId") id: Int
    ): Response<Unit>

    @DELETE("/api/member/{targetMemberId}/follow")
    suspend fun unfollow(
        @Path("targetMemberId") id: Int
    ): Response<Unit>
}
