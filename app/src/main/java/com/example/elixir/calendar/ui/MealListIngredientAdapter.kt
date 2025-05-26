package com.example.elixir.calendar.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.elixir.R
import com.example.elixir.databinding.ItemMealListIndeterminateBinding

class MealListIngredientAdapter(private val ingredients: List<Int>) :
    RecyclerView.Adapter<MealListIngredientAdapter.IngredientViewHolder>() {

    inner class IngredientViewHolder(private val binding: ItemMealListIndeterminateBinding) : 
        RecyclerView.ViewHolder(binding.root) {
        fun bind(ingredient: Int) {
            when (ingredient) {
                614 -> {
                    binding.indeterminateName.setText(R.string.seasoned_cabbage)
                }
                388 -> {
                    binding.indeterminateName.setText(R.string.strawberry)
                }
                768 -> {
                    binding.indeterminateName.setText(R.string.spinach)
                }
                802 -> {
                    binding.indeterminateName.setText(R.string.almond)
                }
            }
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

