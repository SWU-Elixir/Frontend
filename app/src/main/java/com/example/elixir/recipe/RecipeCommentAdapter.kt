package com.example.elixir.recipe

import android.content.Context
import android.view.*
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import com.example.elixir.R

class RecipeCommentAdapter(
    private val context: Context,
    private val comments: List<CommentData>
) : RecyclerView.Adapter<RecipeCommentAdapter.CommentViewHolder>() {

    inner class CommentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val profileImage: ImageView = itemView.findViewById(R.id.profileImage)
        val memberTitle: TextView = itemView.findViewById(R.id.memberTitle)
        val memberNickname: TextView = itemView.findViewById(R.id.memberNickname)
        val commentText: TextView = itemView.findViewById(R.id.commentText)
        val dateText: TextView = itemView.findViewById(R.id.dateText)
        val menuButton: ImageButton = itemView.findViewById(R.id.menuButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_recipe_list_comment, parent, false)
        return CommentViewHolder(view)
    }

    override fun onBindViewHolder(holder: CommentViewHolder, position: Int) {
        val item = comments[position]

        holder.profileImage.setImageResource(item.profileImageResId)
        holder.memberTitle.text = item.memberTitle
        holder.memberNickname.text = item.memberNickname
        holder.commentText.text = item.commentText
        holder.dateText.text = item.date

        // 메뉴 버튼 클릭 시 팝업 메뉴 표시
        holder.menuButton.setOnClickListener {
            val popupMenu = PopupMenu(context, holder.menuButton)
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

    override fun getItemCount(): Int = comments.size
}
