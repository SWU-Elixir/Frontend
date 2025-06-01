package com.example.elixir.recipe.data

import androidx.lifecycle.LiveData
import com.example.elixir.recipe.data.dao.RecipeDao
import com.example.elixir.recipe.data.entity.RecipeEntity
import com.example.elixir.recipe.network.RecipeAPI

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MultipartBody
import okhttp3.RequestBody

class RecipeRepository(private val api: RecipeAPI, private val dao: RecipeDao) {
    // 레시피 목록 가져오기 (API → DB 저장 → DB 반환)
    suspend fun getRecipes(
        page: Int,
        size: Int,
        categoryType: String,
        categorySlowAging: String
    ): List<RecipeEntity> = withContext(Dispatchers.IO) {
        val response = api.getRecipe(page, size, categoryType, categorySlowAging)
        if (response.isSuccessful) {
            // response.body()의 타입이 GetRecipeListResponse라고 가정
            val recipes = response.body()?.data?.toEntities() ?: emptyList()
            dao.insertRecipes(recipes)
        }
        return@withContext dao.getRecipes(page, size, categoryType, categorySlowAging)
    }

    // 레시피 단건 조회 (API → DB 저장 → DB 반환)
    suspend fun getRecipeById(recipeId: Int): RecipeEntity? = withContext(Dispatchers.IO) {
        val response = api.getRecipeById(recipeId)
        if (response.isSuccessful) {
            response.body()?.data?.toEntity()?.let { dao.insertRecipe(it) }
        }
        return@withContext dao.getRecipeById(recipeId)
    }


    // 레시피 검색 (API → DB 저장 → DB 반환)
    suspend fun searchRecipes(
        keyword: String,
        page: Int,
        size: Int,
        categoryType: String,
        categorySlowAging: String
    ): List<RecipeEntity> = withContext(Dispatchers.IO) {
        val response = api.searchRecipe(keyword, page, size, categoryType, categorySlowAging)
        if (response.isSuccessful) {
            // data가 List<RecipeData>인 경우
            response.body()?.data?.toEntities()?.let { dao.insertRecipes(it) }
        }
        return@withContext dao.searchRecipes(keyword, page, size, categoryType, categorySlowAging)
    }


    // 레시피 업로드 (API → 성공 시 DB 저장)
    suspend fun uploadRecipe(dto: RequestBody, image: MultipartBody.Part): RecipeEntity? = withContext(Dispatchers.IO) {
        val response = api.uploadRecipe(dto, image)
        if (response.isSuccessful) {
            response.body()?.data?.toEntity()?.let {
                dao.insertRecipe(it)
                return@withContext it
            }
        }
        return@withContext null
    }


    // 레시피 수정 (API → 성공 시 DB 갱신)
    suspend fun updateRecipe(recipeId: Int, dto: RequestBody, image: MultipartBody.Part): RecipeEntity? = withContext(Dispatchers.IO) {
        val response = api.updateRecipe(recipeId, dto, image)
        if (response.isSuccessful) {
            response.body()?.data?.toEntity()?.let {
                dao.updateRecipe(it)
                return@withContext it
            }
        }
        return@withContext null
    }

    // 레시피 삭제 (API → 성공 시 DB 삭제)
    suspend fun deleteRecipe(recipeId: Int): Boolean = withContext(Dispatchers.IO) {
        val response = api.deleteRecipe(recipeId)
        if (response.isSuccessful) {
            dao.deleteRecipeById(recipeId)
            return@withContext true
        }
        return@withContext false
    }

    // 로컬 DB에서만 전체 레시피 조회
    suspend fun getAllLocalRecipes(): List<RecipeEntity> = withContext(Dispatchers.IO) {
        dao.getAllRecipes()
    }
}
