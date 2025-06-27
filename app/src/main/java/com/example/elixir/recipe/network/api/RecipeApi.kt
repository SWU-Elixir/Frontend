package com.example.elixir.recipe.network.api

import com.example.elixir.chatbot.RecipeListResponse
import com.example.elixir.network.GetStringResponse
import com.example.elixir.recipe.network.response.GetRecipeListResponse
import com.example.elixir.recipe.network.response.GetRecipePostResponse
import com.example.elixir.recipe.network.response.GetRecipeResponse
import com.example.elixir.recipe.network.response.GetSearchResponse
import com.example.elixir.recipe.network.response.GetRecipeRecommendResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query

interface RecipeApi {
    @GET("/api/recipe")
    suspend fun getRecipe(
        @Query("page") page: Int,
        @Query("size") size: Int,
        @Query("categoryType") categoryType: String?,
        @Query("categorySlowAging") categorySlowAging: String?
    ) : Response<GetRecipeListResponse>

    @GET("/api/recipe/{recipeId}")
    suspend fun getRecipeById(
        @Path("recipeId") id: Int
    ): Response<GetRecipeResponse>

    @GET("/api/recipe/my")
    suspend fun getRecipeMy(
        @Query("size") size: Int
    ): Response<RecipeListResponse>

    @GET("/api/recipe/search")
    suspend fun searchRecipe(
        @Query("keyword") keyword: String,
        @Query("page") page: Int,
        @Query("size") size: Int,
        @Query("categoryType") categoryType: String?,
        @Query("categorySlowAging") categorySlowAging: String?
    ): Response<GetRecipeListResponse>

    @GET("/api/recipe/search/keyword")
    suspend fun getRecipeByKeyword()
    : Response<GetSearchResponse>

    @GET("/api/recipe/recommend/search/keyword")
    suspend fun getRecipeByRecommendKeyword()
            : Response<GetSearchResponse>

    @GET("/api/recipe/recommend")
    suspend fun getRecipeByRecommend()
            : Response<GetRecipeRecommendResponse>

    @Multipart
    @POST("/api/recipe")
    suspend fun uploadRecipe(
        @Part("dto") dto: RequestBody,
        @Part image: MultipartBody.Part,
        @Part recipeStepImages: List<MultipartBody.Part>
    ): Response<GetRecipePostResponse>

    @Multipart
    @PATCH("/api/recipe/{recipeId}")
    suspend fun updateRecipe(
        @Path("recipeId") recipeId: Int,
        @Part("dto") dto: RequestBody,
        @Part image: MultipartBody.Part?,
        @Part recipeStepImages: List<MultipartBody.Part>?
    ): Response<GetRecipePostResponse>

    @DELETE("/api/recipe/{recipeId}")
    suspend fun deleteRecipe(
        @Path("recipeId") recipeId: Int
    ): Response<GetStringResponse>

    // 좋아요, 스크랩
    @POST("/api/recipe/{recipeId}/like")
    suspend fun addLike(
        @Path("recipeId") recipeId: Int
    ): Response<GetStringResponse>

    @POST("/api/recipe/{recipeId}/scrap")
    suspend fun addScrap(
        @Path("recipeId") recipeId: Int
    ): Response<GetStringResponse>

    @DELETE("/api/recipe/{recipeId}/like")
    suspend fun deleteLike(
        @Path("recipeId") recipeId: Int
    ): Response<GetStringResponse>

    @DELETE("/api/recipe/{recipeId}/scrap")
    suspend fun deleteScrap(
        @Path("recipeId") recipeId: Int
    ): Response<GetStringResponse>
}