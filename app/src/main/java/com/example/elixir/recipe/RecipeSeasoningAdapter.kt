package com.example.elixir.recipe

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.elixir.databinding.ItemRecipeListIndeterminateBinding

class RecipeSeasoningAdapter(private val ingredients: List<String>) :
    RecyclerView.Adapter<RecipeSeasoningAdapter.IngredientViewHolder>() {

    inner class IngredientViewHolder(val binding: ItemRecipeListIndeterminateBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): IngredientViewHolder {
        val binding = ItemRecipeListIndeterminateBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return IngredientViewHolder(binding)
    }

    override fun onBindViewHolder(holder: IngredientViewHolder, position: Int) {
        holder.binding.indeterminateName.text = ingredients[position]
    }

    override fun getItemCount(): Int = ingredients.size
}

