package com.example.elixir.chatbot

import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.example.elixir.R
import com.example.elixir.databinding.ItemChatMealBinding
import com.example.elixir.ingredient.data.IngredientData

class ChatMealListAdapter(
    private val items: List<ChatMeal>,
    private val ingredientMap: Map<Int, IngredientData>,
    private val onClick: (ChatMeal) -> Unit
) : RecyclerView.Adapter<ChatMealListAdapter.ViewHolder>() {

    private var selectedIndex = -1

    inner class ViewHolder(val binding: ItemChatMealBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: ChatMeal, position: Int) {
            // Glide 옵션 설정
            val requestOptions = RequestOptions()
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .placeholder(R.drawable.ic_recipe_white)
                .error(R.drawable.ic_recipe_white)
                .centerCrop()

            // 이미지 URL이 null이 아니고 비어있지 않은 경우 Glide 로드
            val imageUrl = item.imageUrl // 여기서 item.imageUrl 가져오도록 수정
            if (!imageUrl.isNullOrEmpty()) {
                Glide.with(binding.root.context)
                    .load(imageUrl)
                    .apply(requestOptions)
                    .into(binding.icon)
            } else {
                binding.icon.setImageResource(R.drawable.ic_recipe_white)
            }
            binding.date.text = item.date
            binding.title.text = item.title
            
            val ingredientNames = item.ingredientTags.mapNotNull { ingredientMap[it]?.name }
            val subtitle = ingredientNames.joinToString("/")
            binding.subtitle.text = subtitle
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