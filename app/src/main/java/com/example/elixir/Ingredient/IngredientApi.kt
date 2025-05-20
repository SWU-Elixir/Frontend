package com.example.elixir.Ingredient

import retrofit2.http.GET

interface IngredientApi {
    @GET("api/ingredient")
    suspend fun getAllIngredients(): IngredientResponse

    @GET("api/ingredient/challenge")
    suspend fun getChallengeIngredients(): IngredientResponse
}