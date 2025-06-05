package com.example.elixir.recipe.network

import com.example.elixir.chatbot.RecipeListResponse
import com.example.elixir.recipe.network.response.GetRecipeListResponse
import com.example.elixir.recipe.network.response.GetRecipeResponse
import com.example.elixir.recipe.network.response.GetSearchResponse
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

interface RecipeAPI {
    @GET("/api/recipe")
    suspend fun getRecipe(
        @Query("page") page: Int,
        @Query("size") size: Int,
        @Query("categoryType") categoryType: String,
        @Query("categorySlowAging") categorySlowAging: String
    ) : Response<GetRecipeListResponse>

    @GET("/api/recipe/{recipeId}")
    suspend fun getRecipeById(
        @Path("recipeId") recipeId: Int
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
        @Query("categoryType") categoryType: String,
        @Query("categorySlowAging") categorySlowAging: String
    ): Response<GetRecipeListResponse>

    @GET("/api/recipe/search/keyword")
    suspend fun getRecipeByKeyword()
    : Response<GetSearchResponse>

    @GET("/api/recipe/recommend/search/keyword")
    suspend fun getRecipeByRecommendKeyword()
            : Response<GetSearchResponse>

    @GET("/api/recipe/recommend")
    suspend fun getRecipeByRecommend()
            : Response<GetRecipeListResponse>

    @Multipart
    @POST("/api/recipe")
    suspend fun uploadRecipe(
        @Part("dto") dto: RequestBody,
        @Part image: MultipartBody.Part,
        @Part recipeStepImages: List<MultipartBody.Part>
    ): Response<GetRecipeResponse>

    @Multipart
    @PATCH("/api/recipe/{recipeId}")
    suspend fun updateRecipe(
        @Path("recipeId") recipeId: Int,
        @Part("dto") dto: RequestBody,
        @Part image: MultipartBody.Part
    ): Response<GetRecipeResponse>

    @DELETE("/api/recipe/{recipeId}")
    suspend fun deleteRecipe(
        @Path("recipeId") recipeId: Int
    ): Response<GetRecipeResponse>
}