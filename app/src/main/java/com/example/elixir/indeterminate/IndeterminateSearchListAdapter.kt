package com.example.elixir.indeterminate

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.elixir.R
import com.example.elixir.databinding.ItemIndeterminateSearchListBinding
import com.example.elixir.recipe.RecipeData

class IndeterminateSearchListAdapter(private var indeterminateList: List<IndeterminateItem>) :
    RecyclerView.Adapter<IndeterminateSearchListAdapter.IngredientViewHolder>() {

    inner class IngredientViewHolder(val binding: ItemIndeterminateSearchListBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): IngredientViewHolder {
        val binding = ItemIndeterminateSearchListBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return IngredientViewHolder(binding)
    }

    override fun onBindViewHolder(holder: IngredientViewHolder, position: Int) {
        val item = indeterminateList[position]

        holder.binding.search.text = item.name
        if(item.type == "초가공식품")
        {
            holder.binding.tag.visibility = View.VISIBLE
            holder.binding.tag.text = "⚠ 초가공식품"
        }
        if(item.type == "챌린지")
        {
            holder.binding.tag.visibility = View.VISIBLE
            holder.binding.tag.setBackgroundResource(R.drawable.bg_rect_outline_orange_5)
            holder.binding.tag.setTextColor(holder.binding.root.context.getColor(R.color.elixir_orange))
            holder.binding.tag.text = "🎉 챌린지"
        }
    }

    override fun getItemCount(): Int = indeterminateList.size

    fun updateData(newList: List<IndeterminateItem>) {
        indeterminateList = newList
        notifyDataSetChanged()
    }
}

