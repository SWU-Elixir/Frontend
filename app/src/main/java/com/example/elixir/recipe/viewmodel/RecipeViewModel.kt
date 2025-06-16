package com.example.elixir.recipe.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.elixir.recipe.data.RecipeData
import com.example.elixir.recipe.data.RecipeRepository
import com.example.elixir.recipe.data.entity.RecipeEntity
import com.example.elixir.recipe.data.entity.toData
import kotlinx.coroutines.launch
import okhttp3.MultipartBody
import okhttp3.RequestBody
import org.threeten.bp.LocalDateTime
import org.threeten.bp.format.DateTimeFormatter
import java.io.File
import kotlin.coroutines.cancellation.CancellationException

class RecipeViewModel(
    private val repository: RecipeRepository
) : ViewModel() {

    private val _recipeList = MutableLiveData<List<RecipeData>>()
    val recipeList: LiveData<List<RecipeData>> = _recipeList

    private val _recipeDetail = MutableLiveData<RecipeData?>()
    val recipeDetail: LiveData<RecipeData?> = _recipeDetail

    private val _uploadResult = MutableLiveData<Result<RecipeData?>>()
    val uploadResult: LiveData<Result<RecipeData?>> = _uploadResult

    private val _updateResult = MutableLiveData<Result<RecipeEntity?>>()
    val updateResult: LiveData<Result<RecipeEntity?>> = _updateResult

    private val _deleteResult = MutableLiveData<Result<Boolean>>()
    val deleteResult: LiveData<Result<Boolean>> = _deleteResult

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> = _errorMessage

    // 레시피 목록 불러오기
    private var currentCategoryType: String? = null
    private var currentSlowAging: String? = null

    fun getRecipes(page: Int, size: Int, categoryType: String?, slowAging: String?) {
        viewModelScope.launch {
            // 필터가 바뀌면 기존 데이터 초기화
            if (categoryType != currentCategoryType || slowAging != currentSlowAging) {
                _recipeList.value = emptyList()
                currentCategoryType = categoryType
                currentSlowAging = slowAging
            }
            val newRecipes = repository.getRecipes(page, size, categoryType, slowAging)
            val current = _recipeList.value ?: emptyList()
            _recipeList.value = current + newRecipes // 누적
        }
    }


    fun searchRecipes(
        keyword: String,
        page: Int,
        size: Int,
        categoryType: String?,
        categorySlowAging: String?
    ) {
        viewModelScope.launch {
            val recipes = repository.searchRecipes(keyword, page, size, categoryType, categorySlowAging)
            _recipeList.value = recipes
        }
    }

    // 레시피 상세 불러오기
    fun getRecipeById(recipeId: Int) {
        viewModelScope.launch {
            try {
                Log.d("RecipeViewModel", "코루틴 시작: recipeId=$recipeId")
                // 데이터 요청 코드 (예시)
                val response = repository.getRecipeById(recipeId)
                _recipeDetail.value = response
            } catch (e: Exception) {
                // 예외 발생 시 로그
                Log.e("RecipeViewModel", "코루틴 에러: ${e.message}", e)
                if (e is CancellationException) {
                    Log.e("RecipeViewModel", "코루틴 취소됨: ${e.message}")
                }
            } finally {
                // 코루틴이 끝날 때 로그
                Log.d("RecipeViewModel", "코루틴 종료")
            }
        }
    }

    // 레시피 업로드
    fun uploadRecipe(entity: RecipeEntity, thumbnailFile: File?, stepImageFiles: List<File?> ) {
        viewModelScope.launch {
            try {
                // Repository는 Entity 반환 → ViewModel에서 Data로 변환
                val entityResult = repository.uploadRecipe(entity, thumbnailFile, stepImageFiles)
                val dataResult = entityResult?.toData()
                _uploadResult.value = Result.success(dataResult)
            } catch (e: Exception) {
                _uploadResult.value = Result.failure(e)
            }
        }
    }

    // 레시피 수정
    fun updateRecipe(recipeId: Int, recipeEntity: RecipeEntity,
                     thumbnailFile: File?, stepImageFiles: List<File?>) {
        viewModelScope.launch {
            try {
                val entityResult = repository.updateRecipe(recipeId, recipeEntity, thumbnailFile, stepImageFiles)
                Log.d("RecipeViewModel", "updateRecipe 결과: $entityResult")
                _updateResult.value = Result.success(entityResult)
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

    // 좋아요 추가
    fun addLike(recipeId: Int) = viewModelScope.launch {
        try {
            val success = repository.addLike(recipeId)
            if (!success) {
                _errorMessage.value = "좋아요 처리에 실패했습니다."
            }
        } catch (e: Exception) {
            _errorMessage.value = "네트워크 오류: ${e.message}"
        }
    }

    // 좋아요 취소
    fun deleteLike(recipeId: Int) = viewModelScope.launch {
        try {
            val success = repository.deleteLike(recipeId)
            if (!success) {
                _errorMessage.value = "좋아요 취소에 실패했습니다."
            }
        } catch (e: Exception) {
            _errorMessage.value = "네트워크 오류: ${e.message}"
        }
    }

    // 스크랩 추가
    fun addScrap(recipeId: Int) = viewModelScope.launch {
        try {
            val success = repository.addScrap(recipeId)
            if (!success) {
                _errorMessage.value = "스크랩 처리에 실패했습니다."
            }
        } catch (e: Exception) {
            _errorMessage.value = "네트워크 오류: ${e.message}"
        }
    }

    // 스크랩 취소
    fun deleteScrap(recipeId: Int) = viewModelScope.launch {
        try {
            val success = repository.deleteScrap(recipeId)
            if (!success) {
                _errorMessage.value = "스크랩 취소에 실패했습니다."
            }
        } catch (e: Exception) {
            _errorMessage.value = "네트워크 오류: ${e.message}"
        }
    }
}
