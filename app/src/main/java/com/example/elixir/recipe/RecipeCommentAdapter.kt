package com.example.elixir.recipe

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.PopupMenu
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.elixir.R
import com.example.elixir.databinding.ItemRecipeListCommentBinding

class RecipeCommentAdapter(
    private val context: Context,
    private val comments: List<CommentData>
) : RecyclerView.Adapter<RecipeCommentAdapter.CommentViewHolder>() {

    inner class CommentViewHolder(private val binding: ItemRecipeListCommentBinding) : 
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: CommentData) {
            binding.profileImage.setImageResource(item.profileImageResId)
            binding.memberTitle.text = item.memberTitle
            binding.memberNickname.text = item.memberNickname
            binding.commentText.text = item.commentText
            binding.dateText.text = item.date

            // 메뉴 버튼 클릭 시 팝업 메뉴 표시
            binding.menuButton.setOnClickListener {
                val popupMenu = PopupMenu(context, binding.menuButton)
                popupMenu.menuInflater.inflate(R.menu.item_menu_drop, popupMenu.menu)

                popupMenu.setOnMenuItemClickListener { menuItem ->
                    when (menuItem.itemId) {
                        R.id.menu_edit -> {
                            Toast.makeText(context, "댓글 수정 클릭됨", Toast.LENGTH_SHORT).show()
                            true
                        }
                        R.id.menu_delete -> {
                            Toast.makeText(context, "댓글 삭제 클릭됨", Toast.LENGTH_SHORT).show()
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
