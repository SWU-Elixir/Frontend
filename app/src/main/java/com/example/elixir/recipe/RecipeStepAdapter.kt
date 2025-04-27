package com.example.elixir.recipe

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.elixir.R

class RecipeStepAdapter(private val recipeOrder: List<String>) :
    RecyclerView.Adapter<RecipeStepAdapter.StepViewHolder>() {

    inner class StepViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val recipeExplain: TextView = itemView.findViewById(R.id.recipeExplain)
        val stepNumber: TextView = itemView.findViewById(R.id.stepNumber)
        val recipePicture: ImageView = itemView.findViewById(R.id.recipePicture)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StepViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_recipe_list_step, parent, false)
        return StepViewHolder(view)
    }

    override fun onBindViewHolder(holder: StepViewHolder, position: Int) {
        holder.recipeExplain.text = recipeOrder[position]
        holder.stepNumber.text = "STEP ${position + 1}"
    }

    override fun getItemCount(): Int = recipeOrder.size
}

