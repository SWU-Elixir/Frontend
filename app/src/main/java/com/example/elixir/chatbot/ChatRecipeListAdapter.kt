package com.example.elixir.chatbot

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.elixir.databinding.ItemChatRecipeBinding

class ChatRecipeListAdapter(
    private val items: List<ChatRecipe>,
    private val onClick: (ChatRecipe) -> Unit
) : RecyclerView.Adapter<ChatRecipeListAdapter.ViewHolder>() {

    private var selectedIndex = -1

    inner class ViewHolder(val binding: ItemChatRecipeBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: ChatRecipe, position: Int) {
            binding.icon.setImageResource(item.iconResId)
            binding.title.text = item.title
            binding.subtitle.text = item.subtitle

            binding.root.isSelected = (position == selectedIndex)
            binding.root.setOnClickListener {
                val prev = selectedIndex
                selectedIndex = position
                notifyItemChanged(prev)
                notifyItemChanged(selectedIndex)
                onClick(item)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemChatRecipeBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun getItemCount() = items.size
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position], position)
    }
} 