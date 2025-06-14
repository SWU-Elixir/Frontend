package com.example.elixir.member

import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.elixir.HomeActivity
import com.example.elixir.databinding.ItemMypageFollowListBinding
import com.example.elixir.R
import com.example.elixir.ToolbarActivity
import kotlinx.coroutines.launch

class FollowListAdapter(
    val items: List<FollowItem>,
    private val myId: Int,
    private val onFollowChanged: (() -> Unit)? = null ,
    private val onItemClick: ((FollowItem) -> Unit)? = null,
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

        // 내 아이디면 팔로우 버튼 숨김
        if (item.targetMemberId == myId) {
            holder.binding.followButton.visibility = View.GONE
        } else {
            holder.binding.followButton.visibility = View.VISIBLE
        }

        // 전체 아이템 클릭 이벤트 통합
        holder.binding.root.setOnClickListener {
            if (item.targetMemberId == myId) {
                val context = holder.itemView.context

                // 현재 Activity가 ToolbarActivity이면 닫고, HomeActivity로 이동
                if (context is ToolbarActivity) {
                    // HomeActivity에서 MyPageFragment가 보이도록 모드 전달
                    val intent = Intent(context, HomeActivity::class.java).apply {
                        putExtra("navigateTo", "mypage") // HomeActivity가 이걸 보고 MyPageFragment로 전환하게
                        flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                    }

                    context.startActivity(intent)
                    context.finish() // 현재 ToolbarActivity 종료
                }
            } else {
                onItemClick?.invoke(item) // 상대방 프로필로 이동
            }
        }
    }

    override fun getItemCount() = items.size
}