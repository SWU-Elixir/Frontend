package com.example.elixir.member

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.elixir.databinding.ItemMypageBadgeGridBinding

class BadgeGridAdapter(private val items: List<BadgeItem>) : RecyclerView.Adapter<BadgeGridAdapter.BadgeViewHolder>() {

    inner class BadgeViewHolder(val binding: ItemMypageBadgeGridBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BadgeViewHolder {
        val binding = ItemMypageBadgeGridBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return BadgeViewHolder(binding)
    }

    override fun onBindViewHolder(holder: BadgeViewHolder, position: Int) {
        val item = items[position]
        holder.binding.badgeImage.setImageResource(item.imageRes)
        holder.binding.badgeTitle.text = item.title
        holder.binding.badgeSubtitle.text = "${item.year}년 ${item.month}월 챌린지 성공"
    }

    override fun getItemCount() = items.size
}