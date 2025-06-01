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
import kotlinx.coroutines.launch
import org.threeten.bp.LocalDateTime

class RecipeViewModel(private val repository: RecipeRepository) : ViewModel() {
    private lateinit var recipeEntity: RecipeEntity // Room DB에 저장할 객체

    // 레시피를 DB에 저장하는 메서드
    fun saveRecipeToDB(data: RecipeData) {
        recipeEntity = RecipeEntity(
            email = data.email,
            title = data.title,
            description = data.description,
            categorySlowAging = data.categorySlowAging,
            categoryType = data.categoryType,
            difficulty = data.difficulty,
            timeHours = data.timeHours,
            timeMinutes = data.timeMinutes,
            ingredientTagIds = data.ingredientTagIds,
            ingredients = data.ingredients,
            seasoning = data.seasoning,
            stepDescriptions = data.stepDescriptions,
            stepImageUrls = data.stepImageUrls,
            tips = data.tips,
            allergies = data.allergies,
            imageUrl = data.imageUrl,
            authorFollowByCurrentUser = false,
            likedByCurrentUser = false,
            scrappedByCurrentUser = false,
            authorNickname = data.authorNickname,
            authorTitle = data.authorTitle,
            likes = 0,
            scraps = 0,
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )
    }
}
