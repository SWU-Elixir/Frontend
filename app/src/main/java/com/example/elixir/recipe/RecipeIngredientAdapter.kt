package com.example.elixir.recipe

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.elixir.R

class RecipeIngredientAdapter(private val ingredients: List<String>) :
    RecyclerView.Adapter<RecipeIngredientAdapter.IngredientViewHolder>() {

    inner class IngredientViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val ingredientTextView: TextView = itemView.findViewById(R.id.indeterminateName)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): IngredientViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_recipe_recommendation_list_indeterminate, parent, false)
        return IngredientViewHolder(view)
    }

    override fun onBindViewHolder(holder: IngredientViewHolder, position: Int) {
        holder.ingredientTextView.text = ingredients[position]
    }

    override fun getItemCount(): Int = ingredients.size
}

