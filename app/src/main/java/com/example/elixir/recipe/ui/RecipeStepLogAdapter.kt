package com.example.elixir.recipe.ui

import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.elixir.R
import com.example.elixir.databinding.ItemRecipeStepBinding
import com.example.elixir.recipe.data.RecipeStepData

class RecipeStepLogAdapter(
    private val stepList: MutableList<RecipeStepData>,
    private val onDeleteClick: (Int) -> Unit,
    private val onImageClick: (Int) -> Unit,
    private val onUpdateButtonState: () -> Unit
) : RecyclerView.Adapter<RecipeStepLogAdapter.StepViewHolder>() {

    inner class StepViewHolder(val binding: ItemRecipeStepBinding) : RecyclerView.ViewHolder(binding.root) {
        private var textWatcher: android.text.TextWatcher? = null

        fun bind(step: RecipeStepData, position: Int) = with(binding) {
            btnDel.setOnClickListener { onDeleteClick(position) }
            stepImg.setOnClickListener { onImageClick(position) }
            Glide.with(stepImg.context)
                .load(step.stepImg)
                .placeholder(R.drawable.img_blank)
                .error(R.drawable.img_blank)
                .into(stepImg)

            textWatcher?.let { stepDescription.removeTextChangedListener(it) }
            stepDescription.setText(step.stepDescription)
            textWatcher = object : android.text.TextWatcher {
                override fun afterTextChanged(s: android.text.Editable?) {
                    val pos = bindingAdapterPosition
                    if (pos != RecyclerView.NO_POSITION) {
                        stepList[pos].stepDescription = s?.toString() ?: ""
                        onUpdateButtonState()
                    }
                }
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            }
            stepDescription.addTextChangedListener(textWatcher)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StepViewHolder {
        val binding = ItemRecipeStepBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return StepViewHolder(binding)
    }

    override fun onBindViewHolder(holder: StepViewHolder, position: Int) {
        holder.bind(stepList[position], position)
    }

    override fun getItemCount(): Int = stepList.size
}