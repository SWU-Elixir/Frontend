package com.example.elixir.recipe.repository

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.liveData
import com.example.elixir.recipe.data.RecipeData
import com.example.elixir.recipe.data.RecipeItemData
import com.example.elixir.recipe.data.RecipeListItemData
import com.example.elixir.recipe.data.SearchItemData
import com.example.elixir.recipe.data.dao.RecipeDao
import com.example.elixir.recipe.data.entity.RecipeEntity
import com.example.elixir.recipe.data.toDto
import com.example.elixir.recipe.data.toEntity
import com.example.elixir.recipe.network.api.RecipeApi
import com.example.elixir.recipe.ui.paging.RecipePagingSource
import com.example.elixir.recipe.ui.paging.SearchPagingSource
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import kotlin.math.max

class RecipeRepository(private val api: RecipeApi, private val dao: RecipeDao) {
    // 레시피 상세 조회 (API로 불러오기, 페이징 적용)
    fun getRecipes(type: String?, slowAging: String?): LiveData<PagingData<RecipeListItemData>> {
        Log.d("RecipeFragment", "Repository: type: $type, slowAging: $slowAging")
        return Pager(
            config = PagingConfig(pageSize = 10),
            pagingSourceFactory = { RecipePagingSource(api, type, slowAging) }
        ).liveData
    }

    // 레시피 검색
    fun searchRecipes(keyword: String?, categoryType: String?, categorySlowAging: String?
    ): LiveData<PagingData<SearchItemData>> {
        return Pager(
            config = PagingConfig(pageSize = 30),
            pagingSourceFactory = { SearchPagingSource(api, keyword, categoryType, categorySlowAging) }
        ).liveData
    }

    // 상세 레시피 가져오기
    suspend fun getRecipeById(recipeId: Int): RecipeData? = withContext(Dispatchers.IO) {
        try {
            // API에서 레시피 데이터 가져오기
            val response = api.getRecipeById(recipeId)

            // 데이터를 가져왔다면 데이터 반환. 실패 시 예외 처리
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

    // 레시피 업로드 (API → 성공 시 DB 저장)
    suspend fun uploadRecipe(entity: RecipeEntity, thumbnailFile: File?,
        stepImageFiles: List<File?>): RecipeEntity? = withContext(Dispatchers.IO) {
        // 1. Entity → Dto 변환
        val dto = entity.toDto()

        // 2. Dto를 JSON으로 변환 후 RequestBody로
        val dtoJson = Gson().toJson(dto)
        val dtoRequestBody = dtoJson.toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull())

        // 3. 썸네일 이미지 파일을 MultipartBody.Part로 변환
        val thumbRequestBody = thumbnailFile?.asRequestBody("image/*".toMediaTypeOrNull())
        val thumbPart = MultipartBody.Part.createFormData("image", thumbnailFile!!.name, thumbRequestBody!!)

        // 4. 조리 단계 이미지 파일 리스트를 MultipartBody.Part 리스트로 변환
        val stepImageParts = stepImageFiles.map { file ->
            val reqBody = file!!.asRequestBody("image/*".toMediaTypeOrNull())
            MultipartBody.Part.createFormData("recipeStepImages", file.name, reqBody)
        }

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
    suspend fun updateRecipe(recipeId: Int, entity: RecipeEntity, thumbnailFile: File?,
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
        val thumbPart = thumbnailFile?.let { file ->
            MultipartBody.Part.createFormData("image", file.name, thumbRequestBody!!)
        }

        // 4. 조리 단계 이미지 파일 리스트를 MultipartBody.Part 리스트로 변환
        val stepImageParts = stepImageFiles.mapNotNull { file ->
            file?.let {
                val reqBody = it.asRequestBody("image/*".toMediaTypeOrNull())
                MultipartBody.Part.createFormData("recipeStepImages", it.name, reqBody)
            }
        }

        // 5. API 호출
        val response = api.updateRecipe(recipeId, dtoRequestBody, thumbPart, stepImageParts)
        if (response.isSuccessful) {
            response.body()?.data?.toEntity()?.let {
                dao.updateRecipe(it) // DB 업데이트
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

    // 좋아요
    // 좋아요 추가
    suspend fun addLike(recipeId: Int): Boolean {
        return try {
            val response = api.addLike(recipeId)
            if (response.isSuccessful) {
                // 로컬 DB 업데이트
                val recipe = dao.getRecipeById(recipeId)
                recipe?.let {
                    dao.updateLikeStatus(
                        recipeId,
                        liked = true,
                        likes = it.likes + 1
                    )
                }
                true
            } else false
        } catch (e: Exception) {
            false
        }
    }

    // 좋아요 취소
    suspend fun deleteLike(recipeId: Int): Boolean {
        return try {
            val response = api.deleteLike(recipeId)
            if (response.isSuccessful) {
                val recipe = dao.getRecipeById(recipeId)
                recipe?.let {
                    dao.updateLikeStatus(
                        recipeId,
                        liked = false,
                        likes = max(0, it.likes - 1)
                    )
                }
                true
            } else false
        } catch (e: Exception) {
            false
        }
    }

    // 스크랩
    // 스크랩 추가
    suspend fun addScrap(recipeId: Int): Boolean {
        return try {
            val response = api.addScrap(recipeId)
            if (response.isSuccessful) {
                val recipe = dao.getRecipeById(recipeId)
                recipe?.let {
                    dao.updateScrapStatus(
                        recipeId,
                        scrapped = true
                    )
                }
                true
            } else false
        } catch (e: Exception) {
            false
        }
    }

    // 스크랩 취소
    suspend fun deleteScrap(recipeId: Int): Boolean {
        return try {
            val response = api.deleteScrap(recipeId)
            if (response.isSuccessful) {
                val recipe = dao.getRecipeById(recipeId)
                recipe?.let {
                    dao.updateScrapStatus(
                        recipeId,
                        scrapped = false
                    )
                }
                true
            } else false
        } catch (e: Exception) {
            false
        }
    }
}
