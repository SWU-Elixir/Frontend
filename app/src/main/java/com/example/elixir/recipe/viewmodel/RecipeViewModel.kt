package com.example.elixir.recipe.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asFlow
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.insertHeaderItem
import com.example.elixir.recipe.data.RecipeData
import com.example.elixir.recipe.data.RecipeListItemData
import com.example.elixir.recipe.data.SearchItemData
import com.example.elixir.recipe.repository.RecipeRepository
import com.example.elixir.recipe.data.entity.RecipeEntity
import com.example.elixir.recipe.data.toData
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.io.File
import kotlin.coroutines.cancellation.CancellationException

class RecipeViewModel(private val repository: RecipeRepository) : ViewModel() {

    private val _recipes = MediatorLiveData<PagingData<RecipeListItemData>>()
    val recipes: LiveData<PagingData<RecipeListItemData>> get() = _recipes

    private val _searchResults = MediatorLiveData<PagingData<SearchItemData>>()
    val searchResults: LiveData<PagingData<SearchItemData>> get() = _searchResults

    private var currentSource: LiveData<PagingData<RecipeListItemData>>? = null
    private var currentSearchSource: LiveData<PagingData<SearchItemData>>? = null

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

    // 카테고리 필터 상태
    private var categoryType: String? = null
    private var categorySlowAging: String? = null

    // 검색 키워드
    private var keyword: String? = null

    // 카테고리 필터 변경 함수
    fun setCategoryType(type: String?) {
        categoryType = type
        Log.d("RecipeViewModel", "categoryType: '$categoryType'")
        loadRecipes()
    }

    fun setCategorySlowAging(slowAging: String?) {
        categorySlowAging = slowAging
        Log.d("RecipeViewModel", "categorySlowAging: $categorySlowAging")
        loadRecipes()
    }

    private fun loadRecipes() {
        Log.d("RecipeViewModel", "loadRecipes() called")

        currentSource?.let {
            _recipes.removeSource(it)
            Log.d("RecipeViewModel", "Previous source removed")
        }

        // 헤더는 Adapter에서 처리하므로 여기서는 추가하지 않음
        val newSource = repository.getRecipes(categoryType, categorySlowAging)
        currentSource = newSource

        _recipes.addSource(newSource) {
            Log.d("RecipeViewModel", "New source emitting data")
            _recipes.value = it
        }
    }

    // 검색 필터 세팅 함수들
    fun setSearchKeyword(keyword: String?) {
        this.keyword = keyword
        loadSearchResults()
    }

    fun setSearchCategoryType(type: String?) {
        categoryType = type
        loadSearchResults()
    }

    fun setSearchCategorySlowAging(slowAging: String?) {
        categorySlowAging = slowAging
        loadSearchResults()
    }

    private fun loadSearchResults() {
        currentSearchSource?.let {
            _searchResults.removeSource(it)
        }

        val newSearchSource = repository.searchRecipes(keyword, categoryType, categorySlowAging)
            .asFlow()
            .map { pagingData ->
                pagingData
                    .insertHeaderItem(item = SearchItemData.SearchSpinnerHeader)
                    .insertHeaderItem(item = SearchItemData.SearchTextHeader)
            }
            .asLiveData()

        currentSearchSource = newSearchSource

        _searchResults.addSource(newSearchSource) {
            _searchResults.value = it
        }
    }


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
