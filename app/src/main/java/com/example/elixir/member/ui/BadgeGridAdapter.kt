package com.example.elixir.member.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.elixir.databinding.ItemMypageBadgeGridBinding
import com.example.elixir.member.data.BadgeItem

class BadgeGridAdapter(private val items: List<BadgeItem>) : RecyclerView.Adapter<BadgeGridAdapter.BadgeViewHolder>() {

    inner class BadgeViewHolder(val binding: ItemMypageBadgeGridBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BadgeViewHolder {
        val binding = ItemMypageBadgeGridBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return BadgeViewHolder(binding)
    }

    override fun onBindViewHolder(holder: BadgeViewHolder, position: Int) {
        val item = items[position]
        holder.binding.imgBadge.setImageResource(item.imageRes)
        holder.binding.tvBadge.text = item.title
        holder.binding.tvBadgeSubTitle.text = "${item.year}년 ${item.month}월 챌린지 성공"
    }

    override fun getItemCount() = items.size
}