package com.example.elixir.recipe.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.elixir.R
import com.example.elixir.databinding.ItemRecipeListStepBinding

class RecipeStepAdapter(private val recipeOrder: List<String>) :
    RecyclerView.Adapter<RecipeStepAdapter.StepViewHolder>() {

    inner class StepViewHolder(val binding: ItemRecipeListStepBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StepViewHolder {
        val binding = ItemRecipeListStepBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return StepViewHolder(binding)
    }

    override fun onBindViewHolder(holder: StepViewHolder, position: Int) {
        holder.binding.recipeExplain.text = recipeOrder[position]
        holder.binding.stepNumber.text = holder.itemView.context.getString(R.string.recipe_step_format, position + 1)
    }

    override fun getItemCount(): Int = recipeOrder.size
}

