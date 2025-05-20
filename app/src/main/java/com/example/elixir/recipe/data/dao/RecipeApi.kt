package com.example.elixir.recipe.data.dao

import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface RecipeApi {
    @Multipart
    @POST("/api/recipe")
    suspend fun postRecipe(
        @Part("dto") dto: RequestBody,
        @Part image: MultipartBody.Part
    )
}