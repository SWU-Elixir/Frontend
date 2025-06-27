package com.example.elixir.recipe.ui

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.elixir.R
import com.example.elixir.databinding.ItemRecipeListStepBinding
import com.example.elixir.recipe.data.RecipeStepData
import java.io.File

class RecipeStepAdapter(private val stepList: List<RecipeStepData>) :
    RecyclerView.Adapter<RecipeStepAdapter.StepViewHolder>() {

    inner class StepViewHolder(val binding: ItemRecipeListStepBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StepViewHolder {
        val binding = ItemRecipeListStepBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return StepViewHolder(binding)
    }

    override fun onBindViewHolder(holder: StepViewHolder, position: Int) {
        val step = stepList[position]
        Log.d("RecipeStepAdapter", "position=$position, desc=${step.stepDescription}, img=${step.stepImg}")

        holder.binding.recipeExplain.text = step.stepDescription
        holder.binding.stepNumber.text = holder.itemView.context.getString(
            com.example.elixir.R.string.recipe_step_format, position + 1
        )
        if (step.stepImg.isNotBlank()) {
            if (step.stepImg.startsWith("/")) {
                // 내부 저장소 파일 경로일 때
                Glide.with(holder.itemView)
                    .load(File(step.stepImg))
                    .placeholder(R.drawable.img_blank)
                    .into(holder.binding.recipePicture)
            } else {
                // content:// 등 URI일 때
                Glide.with(holder.itemView)
                    .load(step.stepImg)
                    .placeholder(R.drawable.img_blank)
                    .into(holder.binding.recipePicture)
            }
        } else {
            holder.binding.recipePicture.setImageResource(R.drawable.img_blank)
        }
    }

    override fun getItemCount(): Int = stepList.size
}