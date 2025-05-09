package com.example.elixir.chatbot

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.elixir.databinding.ItemChatMealBinding

class ChatMealListAdapter(
    private val items: List<ChatMeal>,
    private val onClick: (ChatMeal) -> Unit
) : RecyclerView.Adapter<ChatMealListAdapter.ViewHolder>() {

    private var selectedIndex = -1

    inner class ViewHolder(val binding: ItemChatMealBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: ChatMeal, position: Int) {
            binding.icon.setImageResource(item.iconResId)
            binding.date.text = item.date
            binding.title.text = item.title
            binding.subtitle.text = item.subtitle
            binding.badge.text = item.badgeNumber.toString()

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
        val binding = ItemChatMealBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun getItemCount() = items.size
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position], position)
    }
} 