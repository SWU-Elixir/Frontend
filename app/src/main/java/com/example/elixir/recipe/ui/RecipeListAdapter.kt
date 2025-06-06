package com.example.elixir.recipe.ui

import android.annotation.SuppressLint
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
    private var ingredientItems: List<IngredientData>,
    private val onBookmarkClick: (RecipeData) -> Unit,
    private val onHeartClick: (RecipeData) -> Unit,
    private val fragmentManager: FragmentManager
) : RecyclerView.Adapter<RecipeListAdapter.RecipeViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecipeViewHolder {
        val binding = ItemRecipeListBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return RecipeViewHolder(binding)
    }

    override fun getItemCount(): Int = recipeList.size

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: RecipeViewHolder, position: Int) {
        val item = recipeList[position]

        // 이미지 설정
        Glide.with(holder.itemView.context)
            .load(item.imageUrl)
            .placeholder(R.drawable.img_blank)
            .error(R.drawable.img_blank)
            .into(holder.binding.recipePicture)

        // 텍스트 정보 설정
        holder.binding.recipeNameText.text = item.title
        holder.binding.categorySlowAging.text = item.categorySlowAging
        holder.binding.categoryType.text = item.categoryType
        holder.binding.recipeLevel.text = item.difficulty
        holder.binding.heartCount.text = formatCount(item.likes)

        // 조리 시간 표시
        holder.binding.recipeTimeHour.text = if (item.timeHours == 0) "" else "${item.timeHours}시간"
        holder.binding.recipeTimeMin.text = "${item.timeMinutes}분"

        // 재료 리스트 Flexbox 설정 (ingredientMap 전달)
        holder.binding.ingredientList.apply {
            layoutManager = FlexboxLayoutManager(context).apply {
                flexDirection = FlexDirection.ROW
                justifyContent = JustifyContent.FLEX_START
            }
            // ingredientMap이 필요하다면 FlavoringAdapter에 전달
            adapter = IngredientTagChipAdapter(item.ingredientTagIds, ingredientItems)
            visibility = View.VISIBLE
        }

        // 북마크/좋아요 버튼 상태
        holder.binding.bookmarkButton.setBackgroundResource(
            if (item.scrappedByCurrentUser) R.drawable.ic_recipe_bookmark_selected
            else R.drawable.ic_recipe_bookmark_normal
        )
        holder.binding.heartButton.setBackgroundResource(
            if (item.likedByCurrentUser) R.drawable.ic_recipe_heart_selected
            else R.drawable.ic_recipe_heart_normal
        )

        // 북마크 버튼 클릭
        holder.binding.bookmarkButton.setOnClickListener {
            onBookmarkClick(item)
        }

        // 좋아요 버튼 클릭
        holder.binding.heartButton.setOnClickListener {
            item.likedByCurrentUser = !item.likedByCurrentUser
            holder.binding.heartButton.setBackgroundResource(
                if (item.likedByCurrentUser) R.drawable.ic_recipe_heart_selected
                else R.drawable.ic_recipe_heart_normal
            )
            if (item.likedByCurrentUser) item.likes++ else item.likes--
            holder.binding.heartCount.text = formatCount(item.likes)
            onHeartClick(item)
        }

        // 전체 아이템 클릭
        holder.binding.root.setOnClickListener {
            Log.d("RecipeAdapter", "아이템 클릭됨: ${item.title}")
            val detailFragment = RecipeDetailFragment().apply {
                arguments = Bundle().apply {
                    putInt("recipeId", item.id)
                }
            }
            fragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, detailFragment)
                .addToBackStack(null)
                .commit()
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
}

