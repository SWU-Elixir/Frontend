package com.example.elixir.recipe.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.elixir.recipe.data.FlavoringData
import com.example.elixir.recipe.data.RecipeData
import com.example.elixir.recipe.data.RecipeRepository
import com.example.elixir.recipe.data.RecipeStepData
import com.example.elixir.recipe.data.entity.RecipeEntity
import com.example.elixir.recipe.data.entity.toData
import kotlinx.coroutines.launch
import okhttp3.MultipartBody
import okhttp3.RequestBody
import org.threeten.bp.LocalDateTime

class RecipeViewModel(
    private val repository: RecipeRepository
) : ViewModel() {

    private val _recipeList = MutableLiveData<List<RecipeData>>()
    val recipeList: LiveData<List<RecipeData>> = _recipeList

    private val _recipeDetail = MutableLiveData<RecipeEntity?>()
    val recipeDetail: LiveData<RecipeEntity?> = _recipeDetail

    private val _uploadResult = MutableLiveData<Result<RecipeEntity?>>()
    val uploadResult: LiveData<Result<RecipeEntity?>> = _uploadResult

    private val _updateResult = MutableLiveData<Result<RecipeEntity?>>()
    val updateResult: LiveData<Result<RecipeEntity?>> = _updateResult

    private val _deleteResult = MutableLiveData<Result<Boolean>>()
    val deleteResult: LiveData<Result<Boolean>> = _deleteResult

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    // 레시피 목록 불러오기
    fun getRecipes(page: Int, size: Int, categoryType: String, categorySlowAging: String) {
        viewModelScope.launch {
            try {
                val entityList = repository.getRecipes(page, size, categoryType, categorySlowAging)
                val dataList = entityList.map { it.toData() } // 변환 함수 필요
                _recipeList.value = dataList
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }

    // 레시피 상세 불러오기
    fun getRecipeById(recipeId: Int) {
        viewModelScope.launch {
            try {
                val detail = repository.getRecipeById(recipeId)
                _recipeDetail.value = detail
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }
/*
    // 레시피 검색
    fun searchRecipes(keyword: String, page: Int, size: Int, categoryType: String, categorySlowAging: String) {
        viewModelScope.launch {
            try {
                val result =
                    repository.searchRecipes(keyword, page, size, categoryType, categorySlowAging)
                _recipeList.value = result
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }*/

    // 레시피 업로드
    fun uploadRecipe(dto: RequestBody, image: MultipartBody.Part) {
        viewModelScope.launch {
            try {
                val uploaded = repository.uploadRecipe(dto, image)
                _uploadResult.value = Result.success(uploaded)
            } catch (e: Exception) {
                _uploadResult.value = Result.failure(e)
            }
        }
    }

    // 레시피 수정
    fun updateRecipe(recipeId: Int, dto: RequestBody, image: MultipartBody.Part) {
        viewModelScope.launch {
            try {
                val updated = repository.updateRecipe(recipeId, dto, image)
                _updateResult.value = Result.success(updated)
            } catch (e: Exception) {
                _updateResult.value = Result.failure(e)
            }
        }
    }

    // 레시피 삭제
    fun deleteRecipe(recipeId: Int) {
        viewModelScope.launch {
            try {
                val deleted = repository.deleteRecipe(recipeId)
                _deleteResult.value = Result.success(deleted)
            } catch (e: Exception) {
                _deleteResult.value = Result.failure(e)
            }
        }
    }
}
