package com.example.elixir.calendar

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.elixir.databinding.ItemMealListIndeterminateBinding

class MealListIngredientAdapter(private val ingredients: List<String>) :
    RecyclerView.Adapter<MealListIngredientAdapter.IngredientViewHolder>() {

    inner class IngredientViewHolder(private val binding: ItemMealListIndeterminateBinding) : 
        RecyclerView.ViewHolder(binding.root) {
        fun bind(ingredient: String) {
            binding.indeterminateName.text = ingredient
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): IngredientViewHolder {
        val binding = ItemMealListIndeterminateBinding.inflate(
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

