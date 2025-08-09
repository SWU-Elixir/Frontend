package com.example.elixir.recipe.ui.adapter

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
            // 삭제 버튼 클릭 리스너
            btnDel.setOnClickListener {
                val currentPosition = adapterPosition
                if (currentPosition != RecyclerView.NO_POSITION) {
                    onDeleteClick(currentPosition) // adapterPosition 사용
                }
            }

            // 이미지 클릭 리스너
            stepImg.setOnClickListener {
                val currentPosition = adapterPosition
                if (currentPosition != RecyclerView.NO_POSITION) {
                    onImageClick(currentPosition) // adapterPosition 사용
                }
            }
            Glide.with(stepImg.context)
                .load(step.stepImg)
                .placeholder(R.drawable.img_blank)
                .error(R.drawable.img_blank)
                .into(stepImg)

            // 텍스트 변경 리스너 (기존 로직 유지)
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