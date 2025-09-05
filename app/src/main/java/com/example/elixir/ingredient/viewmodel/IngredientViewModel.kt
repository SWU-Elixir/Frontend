package com.example.elixir.ingredient.viewmodel

import android.util.Log
import androidx.lifecycle.*
import com.example.elixir.ingredient.data.IngredientEntity
import com.example.elixir.ingredient.network.IngredientRepository
import kotlinx.coroutines.launch
import kotlin.coroutines.cancellation.CancellationException

class IngredientViewModel(private val repository: IngredientRepository) : ViewModel() {
    private val _ingredients = MutableLiveData<List<IngredientEntity>>()
    val ingredients: LiveData<List<IngredientEntity>> = _ingredients

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error
    // ViewModel 생성 시 해시 출력
    init {
        Log.d("IngredientViewModel", "ViewModel created: ${this.hashCode()}")
    }

    fun loadIngredients() {
        Log.d("IngredientViewModel", "loadIngredients() called in ${this.hashCode()}")
        val job = viewModelScope.launch {
            try {
                _error.value = null
                // API에서 데이터 가져오기 시도
                _ingredients.value = repository.fetchAndSaveIngredients()
                val result = repository.fetchAndSaveIngredients()
                Log.d("IngredientViewModel", "Received Ingredients: ${result.size}")

            } catch (e: Exception) {
                Log.e("IngredientViewModel", "API 호출 실패, DB에서 데이터 로드 시도", e)
                // API 호출 실패시 DB에서 데이터 로드
                try {
                    val dbData = repository.getIngredientsFromDb()
                    Log.d("IngredientViewModel", "Load Ingredients from DB: ${dbData.size}")
                    if (dbData.isEmpty()) {
                        _error.value = "데이터를 불러올 수 없습니다. 인터넷 연결을 확인해주세요."
                    } else {
                        _ingredients.value = dbData
                    }
                } catch (e: Exception) {
                    _error.value = "데이터 로드 실패: ${e.message}"
                }
            }
        }
        job.invokeOnCompletion { throwable ->
            if (throwable is CancellationException) {
                Log.d("IngredientViewModel", "Coroutine has been canceled normally")
            } else if (throwable != null) {
                Log.d("IngredientViewModel", "Cancelled with Exception: $throwable")
            } else {
                Log.d("IngredientViewModel", "Normal completion")
            }
        }
    }

    // id-string 맵 변환
    fun getIngredientNameMap(): Map<Int, String?> {
        return _ingredients.value?.associate { it.id to it.name } ?: emptyMap()
    }
}