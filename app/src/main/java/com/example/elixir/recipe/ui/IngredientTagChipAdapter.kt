package com.example.elixir.recipe.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.elixir.databinding.ItemRecipeRecommendationListIndeterminateBinding

class IngredientTagChipAdapter(
    private val tagList: List<Int>
) : RecyclerView.Adapter<IngredientTagChipAdapter.TagViewHolder>() {

    // ID-이름 매핑
    private val ingredientTagNameMap = mapOf(
        614 to "절임배추",
        388 to "딸기",
        768 to "시금치",
        802 to "아몬드"
    )

    inner class TagViewHolder(val binding: ItemRecipeRecommendationListIndeterminateBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TagViewHolder {
        val binding = ItemRecipeRecommendationListIndeterminateBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TagViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TagViewHolder, position: Int) {
        val tagId = tagList[position]
        holder.binding.indeterminateName.text = ingredientTagNameMap[tagId] ?: tagId.toString()
    }

    override fun getItemCount(): Int = tagList.size
}