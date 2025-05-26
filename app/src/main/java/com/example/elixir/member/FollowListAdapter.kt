package com.example.elixir.member

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.elixir.databinding.ItemMypageFollowListBinding
import com.example.elixir.R


class FollowListAdapter (private val items: List<FollowItem>) : RecyclerView.Adapter<FollowListAdapter.FollowViewHolder>() {
    inner class FollowViewHolder(val binding: ItemMypageFollowListBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FollowViewHolder {
        val binding = ItemMypageFollowListBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return FollowViewHolder(binding)
    }

    override fun onBindViewHolder(holder: FollowViewHolder, position: Int) {
        val item = items[position]
        val context = holder.itemView.context
        
        holder.binding.profileImage.setImageResource(item.profileImageRes)
        holder.binding.memberTitle.text = item.memberTitle
        holder.binding.memberNickname.text = item.memberNickname

        holder.binding.followButton.setOnClickListener {
            val isFollowing = holder.binding.followButton.text == context.getString(R.string.following)
            // 팔로우 상태 토글 및 UI 업데이트
            holder.binding.followButton.text = if (isFollowing) context.getString(R.string.follow) else context.getString(
                R.string.following
            )
            holder.binding.followButton.setBackgroundResource(
                if (isFollowing) R.drawable.bg_rect_filled_orange
                else R.drawable.bg_rect_outline_gray
            )
            holder.binding.followButton.setTextColor(
                context.getColor(
                    if (isFollowing) R.color.white
                    else R.color.black
                )
            )
        }
    }

    override fun getItemCount() = items.size
}