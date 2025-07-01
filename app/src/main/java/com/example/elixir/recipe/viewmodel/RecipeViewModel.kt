package com.example.elixir.recipe.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import com.example.elixir.recipe.data.RecipeData
import com.example.elixir.recipe.data.RecipeListItemData
import com.example.elixir.recipe.data.RecipeRepository
import com.example.elixir.recipe.data.entity.RecipeEntity
import com.example.elixir.recipe.data.toData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch
import java.io.File
import kotlin.coroutines.cancellation.CancellationException

class RecipeViewModel(
    private val repository: RecipeRepository
) : ViewModel() {

    private val _recipeList = MutableLiveData<List<RecipeData>>()
    val recipeList: LiveData<List<RecipeData>> = _recipeList

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

    private val _recipeDetail = MutableLiveData<RecipeData?>()
    val recipeDetail: LiveData<RecipeData?> = _recipeDetail



    // 상세 데이터
    /*private val _recipeDetail = MutableStateFlow<Result<RecipeData?>>(Result.success(null))
    val recipeDetail: StateFlow<Result<RecipeData?>> = _recipeDetail*/

    // 카테고리 필터 상태
    private val categoryType = MutableStateFlow<String?>(null)
    private val categorySlowAging = MutableStateFlow<String?>(null)

    // 카테고리 필터 변경 함수
    fun setCategoryType(type: String?) {
        categoryType.value = type
    }

    fun setCategorySlowAging(slowAging: String?) {
        categorySlowAging.value = slowAging
    }

    // 검색 키워드
    private val keyword = MutableStateFlow("")

    // 카테고리 타입 검색에 따라 페이징 데이터 불러오기(flatMapLatest로 입력한 마지막 데이터만)
    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val recipes: Flow<PagingData<RecipeListItemData>> =
        combine(categoryType, categorySlowAging) { type, slowAging ->
            repository.getRecipes(type, slowAging)
        }.flatMapLatest { it }

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val searchResults: Flow<PagingData<RecipeListItemData>> =
        combine(keyword, categoryType, categorySlowAging) { keyword, type, slowAging ->
            repository.searchRecipes(keyword, type, slowAging)
        }.flatMapLatest { it }

    fun getRecipeById(recipeId: Int) {
        viewModelScope.launch {
            try {
                Log.d("RecipeViewModel", "코루틴 시작: recipeId=$recipeId")
                // 데이터 요청 코드 (예시)
                val response = repository.getRecipeById(recipeId)
                _recipeDetail.value = response
                Log.d("RecipeViewModel", "recipeDetail 값: $recipeDetail")
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
