package com.example.elixir.recipe

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.elixir.databinding.ItemRecipeRecommendationListIndeterminateBinding

class RecipeIngredientAdapter(private val ingredients: List<String>) :
    RecyclerView.Adapter<RecipeIngredientAdapter.IngredientViewHolder>() {

    inner class IngredientViewHolder(private val binding: ItemRecipeRecommendationListIndeterminateBinding) : 
        RecyclerView.ViewHolder(binding.root) {
        fun bind(ingredient: String) {
            binding.indeterminateName.text = ingredient
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): IngredientViewHolder {
        val binding = ItemRecipeRecommendationListIndeterminateBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return IngredientViewHolder(binding)
    }

    override fun onBindViewHolder(holder: IngredientViewHolder, position: Int) {
        holder.bind(ingredients[position])
    }

    override fun getItemCount(): Int = ingredients.size
}

