package com.example.elixir.recipe.network

import retrofit2.http.GET
import retrofit2.http.Part

interface RecipeAPI {
    @GET("/api/recipe")
    suspend fun getRecipe() {
        //@Part("page") page: Int,
        //@Part("size") size: Int,
        //@Part("categoryType") categoryType: String,
        //@Part("categorySlowAging") categorySlowAging: String
    }
}