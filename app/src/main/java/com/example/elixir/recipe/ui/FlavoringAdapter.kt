package com.example.elixir.recipe.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.elixir.databinding.ItemRecipeListTagWhiteBigBinding
import com.example.elixir.databinding.ItemRecipeRecommendationListIndeterminateBinding
import com.example.elixir.recipe.data.FlavoringItem

// FlavoringAdapter.kt
class FlavoringAdapter(private val flavors: List<FlavoringItem>) :
    RecyclerView.Adapter<FlavoringAdapter.FlavorViewHolder>() {

    inner class FlavorViewHolder(private val binding: ItemRecipeListTagWhiteBigBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(flavor: FlavoringItem) {
            binding.tvIndeterminateName.text = "${flavor.name} ${flavor.value}${flavor.unit}"
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FlavorViewHolder {
        val binding = ItemRecipeListTagWhiteBigBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return FlavorViewHolder(binding)
    }

    override fun onBindViewHolder(holder: FlavorViewHolder, position: Int) {
        holder.bind(flavors[position])
    }

    override fun getItemCount(): Int = flavors.size
}

