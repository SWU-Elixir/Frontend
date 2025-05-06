package com.example.elixir.recipe

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.PopupMenu
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.elixir.R
import com.example.elixir.databinding.ItemRecipeListCommentBinding

interface CommentActionListener {
    fun onEditComment(commentId: String, commentText: String)
    fun onDeleteComment(commentId: String)
}

class RecipeCommentAdapter(
    private val context: Context,
    private val comments: List<CommentData>,
    private val commentActionListener: CommentActionListener
) : RecyclerView.Adapter<RecipeCommentAdapter.CommentViewHolder>() {

    inner class CommentViewHolder(private val binding: ItemRecipeListCommentBinding) : 
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: CommentData) {
            // 댓글 작성자 정보 설정
            binding.profileImage.setImageResource(item.profileImageResId)
            binding.memberTitle.text = item.memberTitle
            binding.memberNickname.text = item.memberNickname
            
            // 댓글 내용과 작성일 설정
            binding.commentText.text = item.commentText
            binding.dateText.text = item.date

            // 메뉴 버튼 클릭 시 팝업 메뉴 표시
            binding.menuButton.setOnClickListener {
                // 팝업 메뉴 생성 및 메뉴 리소스 설정
                val popupMenu = PopupMenu(context, binding.menuButton)
                popupMenu.menuInflater.inflate(R.menu.item_menu_drop, popupMenu.menu)

                // 메뉴 아이템 클릭 이벤트 처리
                popupMenu.setOnMenuItemClickListener { menuItem ->
                    when (menuItem.itemId) {
                        R.id.menu_edit -> {
                            commentActionListener.onEditComment(item.commentId, item.commentText)
                            true
                        }
                        R.id.menu_delete -> {
                            commentActionListener.onDeleteComment(item.commentId)
                            true
                        }
                        else -> false
                    }
                }
                popupMenu.show()
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentViewHolder {
        val binding = ItemRecipeListCommentBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return CommentViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CommentViewHolder, position: Int) {
        holder.bind(comments[position])
    }

    override fun getItemCount(): Int = comments.size
}
