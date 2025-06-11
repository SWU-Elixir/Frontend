package com.example.elixir.ingredient.viewmodel

import android.util.Log
import androidx.lifecycle.*
import com.example.elixir.ingredient.data.IngredientData
import kotlinx.coroutines.launch

class IngredientViewModel(
    private val service: IngredientService
) : ViewModel() {
    private val _ingredients = MutableLiveData<List<IngredientData>>()
    val ingredients: LiveData<List<IngredientData>> = _ingredients

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    fun loadIngredients() {
        val job = viewModelScope.launch {
            try {
                _error.value = null
                // API에서 데이터 가져오기 시도
                _ingredients.value = service.getIngredients()
            } catch (e: Exception) {
                Log.e("IngredientViewModel", "API 호출 실패, DB에서 데이터 로드 시도", e)
                // API 호출 실패시 DB에서 데이터 로드
                try {
                    val dbData = service.getIngredientsFromDb()
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
            Log.d("IngredientViewModel", "취소 이유 : ${throwable}")
        }
    }

    // id-string 맵 변환
    fun getIngredientNameMap(): Map<Int, String?> {
        return _ingredients.value!!.associate { it.id to it.name } ?: emptyMap()
    }
} 