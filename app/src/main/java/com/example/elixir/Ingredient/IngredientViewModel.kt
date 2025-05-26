package com.example.elixir.Ingredient

import androidx.lifecycle.*
import kotlinx.coroutines.launch

class IngredientViewModel(
    private val service: IngredientService
) : ViewModel() {
    private val _ingredients = MutableLiveData<List<IngredientItem>>()
    val ingredients: LiveData<List<IngredientItem>> = _ingredients

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    fun loadIngredients() {
        viewModelScope.launch {
            try {
                _error.value = null
                _ingredients.value = service.getIngredientsFromDb()
            } catch (e: Exception) {
                _error.value = e.message ?: "알 수 없는 오류가 발생했습니다"
                // DB에서 데이터 로드 시도
                try {
                    _ingredients.value = service.getIngredients()
                } catch (e: Exception) {
                    _error.value = "데이터 로드 실패: ${e.message}"
                }
            }
        }
    }
} 