package com.example.elixir.recipe.ui

import android.R
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.elixir.databinding.ItemRecipeHeaderBinding

class RecipeListHeaderAdapter (
    private val onSpinnerSelected: (String) -> Unit
) : RecyclerView.Adapter<RecipeListHeaderAdapter.HeaderViewHolder>() {
    private val categories = listOf("한식", "중식", "일식", "양식", "디저트", "음료_차", "양념_소스_잼")

    inner class HeaderViewHolder(val binding: ItemRecipeHeaderBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HeaderViewHolder {
        val binding = ItemRecipeHeaderBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return HeaderViewHolder(binding)
    }

    override fun onBindViewHolder(holder: HeaderViewHolder, position: Int) {
        val spinner = holder.binding.spinnerCategory
        val adapter = ArrayAdapter(holder.itemView.context, R.layout.simple_spinner_item, categories)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter

        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, pos: Int, id: Long) {
                onSpinnerSelected(categories[pos])
            }
            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }

    override fun getItemCount(): Int = 1
}