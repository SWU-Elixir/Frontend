package com.example.elixir.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.elixir.R
import com.example.elixir.databinding.ItemMealListIndeterminateBinding

class RecipeKeywordAdapter(
    private val keywordList: List<String>,
    private val onItemClick: ((String) -> Unit)? = null
) : RecyclerView.Adapter<RecipeKeywordAdapter.KeywordViewHolder>() {

    inner class KeywordViewHolder(private val binding: ItemMealListIndeterminateBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(keyword: String) {
            binding.indeterminateName.text = keyword
            binding.root.setOnClickListener {
                onItemClick?.invoke(keyword)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): KeywordViewHolder {
        val binding = ItemMealListIndeterminateBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return KeywordViewHolder(binding)
    }

    override fun onBindViewHolder(holder: KeywordViewHolder, position: Int) {
        holder.bind(keywordList[position])
    }

    override fun getItemCount(): Int = keywordList.size
}