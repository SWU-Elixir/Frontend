package com.example.elixir.recipe.ui

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.elixir.databinding.ItemRecipeRecommendationListIndeterminateBinding
import com.example.elixir.ingredient.data.IngredientData

class IngredientTagChipAdapter(
    private val tagList: List<Int>,
    private val ingredientItems: List<IngredientData>
) : RecyclerView.Adapter<IngredientTagChipAdapter.TagViewHolder>() {

    inner class TagViewHolder(val binding: ItemRecipeRecommendationListIndeterminateBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TagViewHolder {
        val binding = ItemRecipeRecommendationListIndeterminateBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TagViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TagViewHolder, position: Int) {
        val tagId = tagList[position]
        Log.d("IngredientChip", "id: ${tagId}, list: ${tagList}")

        val ingredient = ingredientItems.find {
            Log.d("IngredientChip", "id: ${tagId}, id-: ${it.id}")
            it.id == tagId }
        holder.binding.indeterminateName.text = ingredient?.name ?: "Unknown"
    }

    override fun getItemCount(): Int = tagList.size
}