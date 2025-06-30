package com.example.elixir.recipe.ui

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.elixir.R
import com.example.elixir.databinding.ItemRecipeListBinding
import com.example.elixir.ingredient.data.IngredientData
import com.example.elixir.recipe.data.RecipeData
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.flexbox.JustifyContent

/**
 * 레시피 리스트 화면에서 사용되는 RecyclerView 어댑터
 * recipeList: 레시피 목록 데이터
 * onBookmarkClick: 북마크 버튼 클릭 시 동작
 * onHeartClick: 좋아요(하트) 버튼 클릭 시 동작
 */
class RecipeListAdapter(
    private var recipeList: List<RecipeData>,
    private val ingredientMap: Map<Int, IngredientData>,
    private val onBookmarkClick: (RecipeData) -> Unit,
    private val onHeartClick: (RecipeData) -> Unit,
    private val fragmentManager: FragmentManager
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val VIEW_TYPE_ITEM = 0
        private const val VIEW_TYPE_FOOTER = 1
    }

    private var onMoreClick: (() -> Unit)? = null

    fun setOnMoreClickListener(listener: () -> Unit) {
        Log.d("RecipeAdapter", "setOnMoreClickListener called")
        onMoreClick = listener
    }

    override fun getItemCount(): Int = recipeList.size + 1

    override fun getItemViewType(position: Int): Int {
        return if (position == recipeList.size) VIEW_TYPE_FOOTER else VIEW_TYPE_ITEM
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == VIEW_TYPE_FOOTER) {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_recipe_footer, parent, false)
            FooterViewHolder(view)
        } else {
            val binding = ItemRecipeListBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
            RecipeViewHolder(binding)
        }
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is FooterViewHolder) {
            holder.bind { onMoreClick?.invoke() }
        } else if (holder is RecipeViewHolder) {
            val item = recipeList[position]

            Glide.with(holder.itemView.context)
                .load(item.imageUrl)
                .placeholder(R.drawable.ic_recipe_white)
                .error(R.drawable.ic_recipe_white)
                .into(holder.binding.recipePicture)

            if (item.categoryType == "양념_소스_잼") item.categoryType = "양념/소스/잼"
            else if (item.categoryType == "음료_차") item.categoryType = "음료/차"

            holder.binding.recipeNameText.text = item.title
            holder.binding.categorySlowAging.text = item.categorySlowAging
            holder.binding.categoryType.text = item.categoryType
            holder.binding.recipeLevel.text = item.difficulty
            holder.binding.heartCount.text = formatCount(item.likes)

            holder.binding.recipeTimeHour.text = if (item.timeHours == 0) "" else "${item.timeHours}시간"
            holder.binding.recipeTimeMin.text = "${item.timeMinutes}분"

            holder.binding.ingredientList.apply {
                layoutManager = FlexboxLayoutManager(context).apply {
                    flexDirection = FlexDirection.ROW
                    justifyContent = JustifyContent.FLEX_START
                }
                adapter = IngredientTagChipMapAdapter(item.ingredientTagIds, ingredientMap)
                visibility = View.VISIBLE
            }

            holder.binding.bookmarkButton.setBackgroundResource(
                if (item.scrappedByCurrentUser) R.drawable.ic_recipe_bookmark_selected
                else R.drawable.ic_recipe_bookmark_normal
            )

            holder.binding.heartButton.setBackgroundResource(
                if (item.likedByCurrentUser) R.drawable.ic_recipe_heart_selected
                else R.drawable.ic_recipe_heart_normal
            )

            holder.binding.bookmarkButton.setOnClickListener {
                holder.binding.bookmarkButton.setBackgroundResource(
                    if (item.scrappedByCurrentUser) R.drawable.ic_recipe_bookmark_selected
                    else R.drawable.ic_recipe_bookmark_normal
                )
                onBookmarkClick(item)
                notifyItemChanged(position)
            }

            holder.binding.heartButton.setOnClickListener {
                holder.binding.heartButton.setBackgroundResource(
                    if (item.likedByCurrentUser) R.drawable.ic_recipe_heart_selected
                    else R.drawable.ic_recipe_heart_normal
                )
                holder.binding.heartCount.text = formatCount(item.likes)
                onHeartClick(item)
                notifyItemChanged(position)
            }

            holder.binding.root.setOnClickListener {
                val detailFragment = RecipeDetailFragment().apply {
                    arguments = Bundle().apply {
                        putInt("recipeId", item.id)
                    }
                }
                fragmentManager.beginTransaction()
                    .replace(R.id.flContainer, detailFragment)
                    .addToBackStack(null)
                    .commit()
            }
        }
    }

    fun updateData(newList: List<RecipeData>) {
        recipeList = newList
        notifyDataSetChanged()
    }

    private fun formatCount(count: Int): String {
        return when {
            count >= 1_000_000 -> String.format("%.1fM", count / 1_000_000.0)
            count >= 1_000     -> String.format("%.1fk", count / 1_000.0)
            else               -> count.toString()
        }.removeSuffix(".0")
    }

    class RecipeViewHolder(val binding: ItemRecipeListBinding) : RecyclerView.ViewHolder(binding.root)
    class FooterViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val button: Button = itemView.findViewById(R.id.recipe_footer_btn)
        fun bind(onMoreClick: () -> Unit) {
            Log.d("RecipeAdapter", "Footer button clicked!")
            button.setOnClickListener { onMoreClick() }
        }
    }
}

