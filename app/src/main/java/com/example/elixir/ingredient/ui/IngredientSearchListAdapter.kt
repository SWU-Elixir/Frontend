package com.example.elixir.ingredient.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.elixir.ingredient.data.IngredientItem
import com.example.elixir.R
import com.example.elixir.databinding.ItemIndeterminateSearchListBinding

class IngredientSearchListAdapter(
    private var indeterminateList: List<IngredientItem>,
    private val onItemClick: (IngredientItem) -> Unit
) : RecyclerView.Adapter<IngredientSearchListAdapter.IngredientViewHolder>() {

    inner class IngredientViewHolder(val binding: ItemIndeterminateSearchListBinding) : 
        RecyclerView.ViewHolder(binding.root) {
        
        fun bind(item: IngredientItem) {
            binding.search.text = item.name


            // 태그 설정
            when(item.type) {
                "초가공식품" -> {
                    binding.tag.visibility = View.VISIBLE
                    binding.tag.text = "⚠ 초가공식품"
                }
                "챌린지" -> {
                    binding.tag.visibility = View.VISIBLE
                    binding.tag.setBackgroundResource(R.drawable.bg_rect_outline_orange_5)
                    binding.tag.setTextColor(binding.root.context.getColor(R.color.elixir_orange))
                    binding.tag.text = "🎉 챌린지"
                }
                else -> {
                    binding.tag.visibility = View.GONE
                }
            }

            // 클릭 이벤트 설정
            binding.root.setOnClickListener {
                onItemClick(item)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): IngredientViewHolder {
        val binding = ItemIndeterminateSearchListBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return IngredientViewHolder(binding)
    }

    override fun onBindViewHolder(holder: IngredientViewHolder, position: Int) {
        holder.bind(indeterminateList[position])
    }

    override fun getItemCount(): Int = indeterminateList.size

    fun updateData(newList: List<IngredientItem>) {
        // 챌린지 항목을 상단으로 정렬
        indeterminateList = newList.sortedWith(
            compareByDescending<IngredientItem> { it.type == "챌린지" }
                .thenBy { it.name }
        )
        notifyDataSetChanged()
    }
}

