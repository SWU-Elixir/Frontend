package com.example.elixir.recipe.ui

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.elixir.R
import com.example.elixir.databinding.ItemRecipeListCommentBinding
import com.example.elixir.recipe.data.CommentItem
import org.threeten.bp.LocalDateTime
import org.threeten.bp.format.DateTimeFormatter

interface CommentActionListener {
    fun onEditComment(commentId: Int, commentText: String)
    fun onDeleteComment(recipeId:Int, commentId: Int)
}

class RecipeCommentAdapter(
    private val context: Context,
    private val comments: MutableList<CommentItem>,
    private val commentActionListener: CommentActionListener
) : RecyclerView.Adapter<RecipeCommentAdapter.CommentViewHolder>() {

    inner class CommentViewHolder(private val binding: ItemRecipeListCommentBinding) : 
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: CommentItem) {
            // 댓글 작성자 정보 설정
            Glide.with(context)
                .load(item.authorProfileUrl)
                .placeholder(R.drawable.ic_profile)
                .error(R.drawable.ic_profile)
                .into(binding.imgProfile)

            Log.d("RecipeCommentAdapter", "${item.title}, ${item.nickName}, ${item.content}, ${item.updatedAt}")

            // 회원 칭호와 닉네임: 칭호의 경우, 없으면 일반으로 표시
            binding.tvMember.text = if(item.title.isNullOrBlank()) "일반" else item.title
            binding.tvNickname.text = item.nickName
            
            // 댓글 내용
            binding.commentText.text = item.content

            // 날짜 포맷 변경 후 화면에 띄워주기
            val date = stringToLocalDateTime(item.updatedAt)
            binding.dateText.text = localDateTimeToString(date)

            // 메뉴 버튼 클릭 시 팝업 메뉴 표시
            binding.menuButton.setOnClickListener {
                // 팝업 메뉴 생성 및 메뉴 리소스 설정
                val popupMenu = PopupMenu(context, binding.menuButton)
                popupMenu.menuInflater.inflate(R.menu.item_menu_drop, popupMenu.menu)

                // 메뉴 아이템 클릭 이벤트 처리
                popupMenu.setOnMenuItemClickListener { menuItem ->
                    when (menuItem.itemId) {
                        R.id.menu_edit -> {
                            commentActionListener.onEditComment(item.commentId, item.content)
                            true
                        }
                        R.id.menu_delete -> {
                            commentActionListener.onDeleteComment(item.recipeId, item.commentId)
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

    override fun getItemCount(): Int {
        Log.d("RecipeCommentAdapter", "${comments.size}")
        return comments.size
    }

    // 날짜 변환 함수
    private fun stringToLocalDateTime(date: String): LocalDateTime
            = LocalDateTime.parse(date, DateTimeFormatter.ISO_LOCAL_DATE_TIME)

    private fun localDateTimeToString(date: LocalDateTime): String
            = date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
}
