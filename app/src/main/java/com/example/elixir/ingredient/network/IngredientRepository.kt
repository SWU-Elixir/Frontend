package com.example.elixir.ingredient.network

import android.util.Log
import androidx.room.Transaction
import com.example.elixir.ingredient.data.IngredientDao
import com.example.elixir.ingredient.data.IngredientItem

class IngredientRepository(
    private val api: IngredientApi,
    private val dao: IngredientDao
) {
    // API에서 받아와서 DB에 저장
    @Transaction
    suspend fun fetchAndSaveIngredients(): List<IngredientItem> {
        try {
            val response = api.getAllIngredients()
            if (response.status == 200) {
                // 챌린지 식재료 API 호출
                fetchAndSaveChallengeIngredients()
                dao.insertAll(response.data)
                return response.data
            }
            throw Exception("API 호출 실패: ${response.message}")
        } catch (e: Exception) {
            Log.e("IngredientRepository", "데이터 저장 실패", e)
            // DB에 저장된 데이터 반환
            return getIngredientsFromDb()
        }
    }

    // 챌린지 식재료 API에서 받아와서 DB에 저장
    @Transaction
    suspend fun fetchAndSaveChallengeIngredients(): List<IngredientItem> {
        try {
            val response = api.getChallengeIngredients()
            if (response.status == 200) {
                dao.insertAll(response.data)
                return response.data
            }
            throw Exception("API 호출 실패: ${response.message}")
        } catch (e: Exception) {
            Log.e("IngredientRepository", "챌린지 데이터 저장 실패", e)
            return emptyList()
        }
    }

    // DB에서 불러오기
    suspend fun getIngredientsFromDb(): List<IngredientItem> {
        return try {
            dao.getAll()
        } catch (e: Exception) {
            Log.e("IngredientRepository", "DB 조회 실패", e)
            emptyList()
        }
    }
}