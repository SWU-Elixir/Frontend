package com.example.elixir.recipe.data

import android.util.Log
import com.example.elixir.recipe.data.dao.RecipeDao
import com.example.elixir.recipe.data.entity.RecipeEntity
import com.example.elixir.recipe.data.entity.toDto
import com.example.elixir.recipe.network.RecipeAPI
import com.google.gson.Gson

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File

class RecipeRepository(private val api: RecipeAPI, private val dao: RecipeDao) {
    // 레시피 목록 가져오기 (API → DB 저장 → DB 반환)
    suspend fun getRecipes(
        page: Int,
        size: Int,
        categoryType: String,
        categorySlowAging: String
    ): List<RecipeData> = withContext(Dispatchers.IO) {
        try {
            val response = api.getRecipe(page, size, categoryType, categorySlowAging)
            if (response.isSuccessful) {
                val body = response.body()
                val recipes = body?.data?.content?.map { it.toRecipeData() } ?: emptyList()
                recipes.map { it.toEntity() }
                return@withContext recipes
            }
        } catch (e: Exception) {
            Log.e("RecipeRepository", "API 호출 중 예외 발생", e)
        }
        return@withContext emptyList()
    }

    suspend fun getRecipeById(recipeId: Int): RecipeData? = withContext(Dispatchers.IO) {
        try {
            Log.d("RecipeRepository", "네트워크 요청 시작: recipeId=$recipeId")
            val response = api.getRecipeById(recipeId)
            Log.d("RecipeRepository", "네트워크 요청 완료: recipeId=$recipeId")
            if (response.isSuccessful) {
                val apiResponse = response.body()
                if (apiResponse?.data == null) {
                    Log.e("RecipeRepository", "상세조회 결과가 null입니다.")
                }
                return@withContext apiResponse?.data
            } else {
                Log.e("RecipeRepository", "상세조회 실패: ${response.errorBody()?.string()}")
            }
        } catch (e: Exception) {
            Log.e("RecipeRepository", "상세조회 예외 발생", e)
        }
        return@withContext null
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
            //response.body()?.data?.toEntities()?.let { dao.insertRecipes(it) }
        }
        return@withContext dao.searchRecipes(keyword, page, size, categoryType, categorySlowAging)
    }


    // 레시피 업로드 (API → 성공 시 DB 저장)
    suspend fun uploadRecipe(entity: RecipeEntity, thumbnailFile: File?,
        stepImageFiles: List<File?>): RecipeEntity? = withContext(Dispatchers.IO) {
        // 1. Entity → Dto 변환
        val dto = entity.toDto()

        // 2. Dto를 JSON으로 변환 후 RequestBody로
        val dtoJson = Gson().toJson(dto)
        val dtoRequestBody = dtoJson.toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull())
        Log.d("RecipeDto", "전송 JSON: $dtoJson")
        Log.d("RecipeDto", "전송 Request: $dtoRequestBody")

        // 3. 썸네일 이미지 파일을 MultipartBody.Part로 변환
        val thumbRequestBody = thumbnailFile?.asRequestBody("image/*".toMediaTypeOrNull())
        val thumbPart = MultipartBody.Part.createFormData("image", thumbnailFile!!.name, thumbRequestBody!!)

        Log.d("RecipeDto", "전송 이미지: $thumbnailFile")


        // 4. 조리 단계 이미지 파일 리스트를 MultipartBody.Part 리스트로 변환
        val stepImageParts = stepImageFiles.map { file ->
            val reqBody = file!!.asRequestBody("image/*".toMediaTypeOrNull())
            MultipartBody.Part.createFormData("recipeStepImages", file.name, reqBody)
        }


        Log.d("RecipeDto", "전송 이미지 리스트: $stepImageFiles")

        // 5. API 호출
        val response = api.uploadRecipe(dtoRequestBody, thumbPart, stepImageParts)
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
