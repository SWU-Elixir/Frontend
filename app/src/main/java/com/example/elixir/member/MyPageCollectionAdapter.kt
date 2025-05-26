package com.example.elixir.member

import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.elixir.databinding.ItemMypageCollectionBadgeBinding
import com.example.elixir.databinding.ItemMypageCollectionBinding
class MyPageCollectionAdapter(
    private val items: List<Uri>,
    private val isBadge: Boolean                        // 뱃지 여부를 판단하는 플래그 추가
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {   // 제네릭 타입을 RecyclerView.ViewHolder로 변경

    // 뱃지 여부에 따라 다른 ViewHolder를 사용하기 위해 RecyclerView.ViewHolder를 상속받는 두 개의 ViewHolder 클래스 생성
    inner class CollectionViewHolder(val binding: ItemMypageCollectionBinding) :
        RecyclerView.ViewHolder(binding.root)

    inner class BadgeViewHolder(val badgeBinding: ItemMypageCollectionBadgeBinding) :
        RecyclerView.ViewHolder(badgeBinding.root)

    // ViewHolder의 종류에 따라 다른 레이아웃을 사용
    override fun getItemViewType(position: Int): Int {
        // 뱃지일 경우 1, 아닐 경우 0 반환
        return if (isBadge) 1 else 0
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder { // 반환 타입 수정
        // 뱃지 아이템 적용
        return if (viewType == 1) {
            val badgeBinding = ItemMypageCollectionBadgeBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            BadgeViewHolder(badgeBinding)
        }
        // 기본 아이템 적용
        else {
            val binding = ItemMypageCollectionBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            CollectionViewHolder(binding)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        // 아이템에 해당하는 Uri를 가져옴
        val uri = items[position]
        // Glide를 사용하여 이미지 로드
        // 뱃지일 경우 BadgeViewHolder에 이미지 로드
        if (holder is BadgeViewHolder) {
            Glide.with(holder.itemView)
                .load(uri)
                .into(holder.badgeBinding.mypageCollectionBadgeImg)
        }
        // 아닐 경우 CollectionViewHolder에 이미지 로드
        else if (holder is CollectionViewHolder) {
            Glide.with(holder.itemView)
                .load(uri)
                .into(holder.binding.mypageCollectionImg)
        }
    }

    override fun getItemCount(): Int = items.size
}