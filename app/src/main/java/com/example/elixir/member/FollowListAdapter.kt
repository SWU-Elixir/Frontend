package com.example.elixir.member

import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.example.elixir.databinding.ItemMypageFollowListBinding
import com.example.elixir.R
import kotlinx.coroutines.launch

class FollowListAdapter(
    private val items: List<FollowItem>,
    private val onFollowChanged: (() -> Unit)? = null
) : RecyclerView.Adapter<FollowListAdapter.FollowViewHolder>() {
    companion object {
        private const val TAG = "FollowListAdapter"
    }

    inner class FollowViewHolder(val binding: ItemMypageFollowListBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FollowViewHolder {
        val binding = ItemMypageFollowListBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return FollowViewHolder(binding)
    }

    override fun onBindViewHolder(holder: FollowViewHolder, position: Int) {
        val item = items[position]
        val context = holder.itemView.context
        
        // 프로필 이미지 설정
        Glide.with(holder.itemView.context)
            .load(item.profileImageRes ?: "")
            .placeholder(R.drawable.ic_profile) // 기본 이미지
            .error(R.drawable.ic_profile)       // 에러 시 이미지
            .circleCrop()                       // 동그란 프로필 이미지
            .into(holder.binding.profileImage)

        // 타이틀 설정 (null이면 숨김)
        if (item.memberTitle.isNullOrEmpty() || item.memberTitle == "칭호 없음") {
            holder.binding.memberTitle.visibility = View.GONE
        } else {
            holder.binding.memberTitle.visibility = View.VISIBLE
            holder.binding.memberTitle.text = item.memberTitle
        }
        
        // 닉네임 설정
        holder.binding.memberNickname.text = item.memberNickname

        // 버튼 상태 초기화
        if (item.isFollowing) {
            holder.binding.followButton.text = context.getString(R.string.following)
            holder.binding.followButton.setBackgroundResource(R.drawable.bg_rect_outline_gray)
            holder.binding.followButton.setTextColor(context.getColor(R.color.black))
        } else {
            holder.binding.followButton.text = context.getString(R.string.follow)
            holder.binding.followButton.setBackgroundResource(R.drawable.bg_rect_filled_orange)
            holder.binding.followButton.setTextColor(context.getColor(R.color.white))
        }

        // 팔로우 버튼 클릭 이벤트
        holder.binding.followButton.setOnClickListener {
            val isFollowing = holder.binding.followButton.text == context.getString(R.string.following)
            val targetMemberId = items[position].targetMemberId

            Log.d("FollowListAdapter", "targetMemberId: $targetMemberId") // 로그 추가

            // 임시로 조건 주석처리
            // if (targetMemberId <= 1) {
            //     Toast.makeText(context, "이 계정은 팔로우/언팔로우 할 수 없습니다.", Toast.LENGTH_SHORT).show()
            //     return@setOnClickListener
            // }

            (holder.itemView.context as? FragmentActivity)?.lifecycleScope?.launch {
                try {
                    val api = com.example.elixir.RetrofitClient.instanceMemberApi
                    val response = if (isFollowing) {
                        api.unfollow(targetMemberId)
                    } else {
                        api.follow(targetMemberId)
                    }
                    if (response.isSuccessful) {
                        onFollowChanged?.invoke()
                    } else {
                        Toast.makeText(context, "요청 실패: ${response.code()}", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    Toast.makeText(context, "네트워크 오류: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun getItemCount() = items.size
}