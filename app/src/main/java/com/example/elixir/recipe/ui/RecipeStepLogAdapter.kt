package com.example.elixir.recipe.ui

import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.RecyclerView
import com.example.elixir.databinding.ItemRecipeStepBinding
import com.example.elixir.recipe.data.RecipeStepData

class RecipeStepLogAdapter(
    private val stepList: MutableList<RecipeStepData>,
    private val onDeleteClick: (Int) -> Unit,
    private val onImageClick: (Int) -> Unit,
    private val onUpdateButtonState: () -> Unit // 버튼 상태 업데이트 함수 전달
) : RecyclerView.Adapter<RecipeStepLogAdapter.StepViewHolder>() {

    inner class StepViewHolder(val binding: ItemRecipeStepBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(step: RecipeStepData, position: Int) {
            with(binding) {
                // 삭제 버튼 클릭 시 해당 아이템 삭제
                btnDel.setOnClickListener {
                    onDeleteClick(position)
                }

                // 이미지 클릭 시 다이얼로그 호출
                stepImg.setOnClickListener {
                    onImageClick(position)
                }

                // 이미지 설정
                stepImg.setImageURI(Uri.parse(step.stepImg))

                // 설명 설정
                stepDescription.setText(step.stepDescription)
                stepDescription.addTextChangedListener {
                    step.stepDescription = it.toString()
                    onUpdateButtonState() // 버튼 상태 업데이트 호출
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StepViewHolder {
        val binding = ItemRecipeStepBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return StepViewHolder(binding)
    }

    override fun onBindViewHolder(holder: StepViewHolder, position: Int) {
        holder.bind(stepList[position], position)
    }

    override fun getItemCount(): Int {
        return stepList.size
    }
}