package com.example.elixir.recipe.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.elixir.databinding.ItemRecipeRecommendationListIndeterminateBinding
import com.example.elixir.ingredient.data.IngredientItem
import com.example.elixir.ingredient.viewmodel.IngredientViewModel

class IngredientTagChipAdapter(
    private val tagList: List<Int>,
    private val ingredientItems: List<IngredientItem>
) : RecyclerView.Adapter<IngredientTagChipAdapter.TagViewHolder>() {

    inner class TagViewHolder(val binding: ItemRecipeRecommendationListIndeterminateBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TagViewHolder {
        val binding = ItemRecipeRecommendationListIndeterminateBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TagViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TagViewHolder, position: Int) {
        val tagId = tagList[position]
        holder.binding.indeterminateName.text = ingredientItems[tagId].name
    }

    override fun getItemCount(): Int = tagList.size
}