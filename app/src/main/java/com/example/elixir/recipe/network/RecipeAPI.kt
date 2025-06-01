package com.example.elixir.recipe.network

import com.example.elixir.recipe.network.response.GetRecipeListResponse
import com.example.elixir.recipe.network.response.GetRecipeResponse
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

interface RecipeAPI {
    @GET("/api/recipe")
    suspend fun getRecipe(
        @Part("page") page: Int,
        @Part("size") size: Int,
        @Part("categoryType") categoryType: String,
        @Part("categorySlowAging") categorySlowAging: String
    ) : Response<GetRecipeListResponse>

    @GET("/api/recipe/{recipeId}")
    suspend fun getRecipeById(
        @Path("recipeId") recipeId: Int
    ): Response<GetRecipeResponse>

    @GET("/api/recipe/search")
    suspend fun searchRecipe(
        @Part("keyword") keyword: String,
        @Part("page") page: Int,
        @Part("size") size: Int,
        @Part("categoryType") categoryType: String,
        @Part("categorySlowAging") categorySlowAging: String
    ): Response<GetRecipeListResponse>

    @GET("/api/recipe/search/keyword")
    suspend fun getRecipeByKeyword(): Response<GetRecipeResponse>

    @Multipart
    @POST("/api/recipe")
    suspend fun uploadRecipe(
        @Part("dto") dto: RequestBody,
        @Part image: MultipartBody.Part
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