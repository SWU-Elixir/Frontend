package com.example.elixir.recipe

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class RecipeViewModel(private val repository: RecipeRepository) : ViewModel() {
    private var recipeTitle = ""
    private var thumbnail = ""
    private var recipeDescription = ""
    var categorySlowAging = ""
    var categoryType = ""
    private var difficulty = ""
    var ingredients = MutableLiveData<List<FlavoringData>>()
    var seasoning = MutableLiveData<List<FlavoringData>>()
    private var steps = MutableLiveData<List<RecipeStepData>>()
    private var tips = ""

    private val recipeList = MutableLiveData<MutableList<RecipeData>>(mutableListOf())

    val allRecipes: LiveData<List<RecipeEntity>> = repository.getAllRecipes()

    fun addRecipe(recipe: RecipeData) {
        val currentList = recipeList.value ?: mutableListOf()
        currentList.add(recipe)
        recipeList.value = currentList
    }

    // 데이터 저장 메서드
    fun saveRecipeData(
        title: String,
        thumbnail: String,
        description: String,
        slowAging: String,
        type: String,
        difficulty: String,
        ingredientsList: List<FlavoringData>,
        seasoningList: List<FlavoringData>,
        stepsList: List<RecipeStepData>,
        tipsText: String
    ) {
        recipeTitle = title
        this.thumbnail = thumbnail
        recipeDescription = description
        categorySlowAging = slowAging
        categoryType = type
        this.difficulty = difficulty
        ingredients.value = ingredientsList
        seasoning.value = seasoningList
        steps.value = stepsList
        tips = tipsText
    }

    fun saveRecipeToDB(recipeEntity: RecipeEntity) {
        viewModelScope.launch {
            repository.insertRecipe(recipeEntity)
        }
    }
}
