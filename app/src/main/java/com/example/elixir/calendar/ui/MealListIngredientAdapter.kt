package com.example.elixir.calendar.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.elixir.databinding.ItemMealListIndeterminateBinding
import com.example.elixir.ingredient.data.IngredientData

// 전체 식재료 맵을 같이 전달
class MealListIngredientAdapter(
    private val ingredients: List<Int>,
    private val ingredientMap: Map<Int, IngredientData>
) : RecyclerView.Adapter<MealListIngredientAdapter.IngredientViewHolder>() {

    inner class IngredientViewHolder(private val binding: ItemMealListIndeterminateBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(ingredientId: Int) {
            // ingredientMap에서 이름 찾기
            val ingredientName = ingredientMap[ingredientId]?.name ?: "알 수 없음"
            binding.indeterminateName.text = ingredientName
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


