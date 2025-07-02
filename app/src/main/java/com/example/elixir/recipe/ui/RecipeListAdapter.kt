package com.example.elixir.recipe.ui

import android.annotation.SuppressLint
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.FragmentManager
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.elixir.R
import com.example.elixir.databinding.ItemRecipeListBinding
import com.example.elixir.ingredient.data.IngredientData
import com.example.elixir.recipe.data.RecipeData
import com.example.elixir.recipe.data.RecipeListItemData
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
    private var ingredientMap: Map<Int, IngredientData>,
    private val onBookmarkClick: (RecipeListItemData) -> Unit,
    private val onHeartClick: (RecipeListItemData) -> Unit,
    private val fragmentManager: FragmentManager
) : PagingDataAdapter<RecipeListItemData, RecipeListAdapter.RecipeViewHolder>(RecipeDiffCallback()) {

    // 바인딩 정의
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecipeViewHolder {
        val binding = ItemRecipeListBinding.inflate(LayoutInflater.from(parent.context), parent,false)
        return RecipeViewHolder(binding)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: RecipeViewHolder, position: Int) {
        val item = getItem(position) ?: return

        // 레시피 썸네일
        Glide.with(holder.itemView.context)
            .load(item.imageUrl)
            .placeholder(R.drawable.img_blank)
            .error(R.drawable.img_blank)
            .into(holder.binding.recipePicture)

        // 카테고리 타입 명칭
        val categoryType = when (item.categoryType) {
            "양념_소스_잼" -> "양념/소스/잼"
            "음료_차" -> "음료/차"
            else -> item.categoryType
        }
        // 차례대로 레시피명, 저속노화, 종류, 난이도, 좋아요 갯수 정의
        holder.binding.recipeNameText.text = item.title
        holder.binding.categorySlowAging.text = item.categorySlowAging
        holder.binding.categoryType.text = categoryType
        holder.binding.recipeLevel.text = item.difficulty
        holder.binding.heartCount.text = formatCount(item.likes)

        // 시간 정의 (시간, 분)
        val timeHours = item.totalTimeMinutes / 60                               // 시간 정의
        if(timeHours > 0)
            holder.binding.recipeTimeHour.text = "${timeHours}시간"

        holder.binding.recipeTimeMin.text = "${item.totalTimeMinutes % 60}분"    // 분 정의

        // 식재료 태그 정의
        holder.binding.ingredientList.apply {
            layoutManager = FlexboxLayoutManager(context).apply {
                flexDirection = FlexDirection.ROW
                justifyContent = JustifyContent.FLEX_START
            }

            // 만약 식재료 태그가 없다면 공란으로 두고 화면 상에서도 지워버리기
            adapter = IngredientTagChipMapAdapter(
                item.ingredientTagIds ?: emptyList(),
                ingredientMap
            )
            visibility = if (item.ingredientTagIds.isNullOrEmpty()) View.GONE else View.VISIBLE
        }
        // 스크랩 버튼: 클릭 시 색상만 바뀌게
        holder.binding.bookmarkButton.setBackgroundResource(
            if (item.scrappedByCurrentUser) R.drawable.ic_recipe_bookmark_selected
            else R.drawable.ic_recipe_bookmark_normal
        )

        // 좋아요 버튼: 클릭 시 색상만 바뀌게
        holder.binding.heartButton.setBackgroundResource(
            if (item.likedByCurrentUser) R.drawable.ic_recipe_heart_selected
            else R.drawable.ic_recipe_heart_normal
        )

        // 스크랩 버튼: 콜백 함수 정의 및 아이템 바뀐거 알리기
        holder.binding.bookmarkButton.setOnClickListener {
            onBookmarkClick(item)
            notifyItemChanged(position)
        }

        // 좋아요 버튼: 콜백 함수 정의 및 아이템 바뀐거 알리기
        holder.binding.heartButton.setOnClickListener {
            onHeartClick(item)
            holder.binding.heartCount.text = formatCount(item.likes)
            notifyItemChanged(position)
        }
        // 아이템 클릭: 상세 페이지로 넘어가게
        holder.binding.root.setOnClickListener {
            // 레시피 아이디 넘겨주기
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
    // 좋아요 숫자 형식화
    @SuppressLint("DefaultLocale")
    private fun formatCount(count: Int): String {
        return when {
            count >= 1_000_000 -> String.format("%.1fM", count / 1_000_000.0)
            count >= 1_000     -> String.format("%.1fk", count / 1_000.0)
            else               -> count.toString()
        }.removeSuffix(".0")
    }

    fun updateIngredientMap(newIngredientMap: Map<Int, IngredientData>) {
        ingredientMap = newIngredientMap
        // 모든 아이템에 영향을 줄 수 있으니, 전체 갱신
        notifyDataSetChanged() // 단순화를 위해, 실제로는 DiffUtil이 알아서 처리하므로 필요 없을 수 있음
    }

    class RecipeViewHolder(val binding: ItemRecipeListBinding) : RecyclerView.ViewHolder(binding.root)
}

class RecipeDiffCallback : DiffUtil.ItemCallback<RecipeListItemData>() {
    override fun areItemsTheSame(oldItem: RecipeListItemData, newItem: RecipeListItemData): Boolean =
        oldItem.id == newItem.id

    override fun areContentsTheSame(oldItem: RecipeListItemData, newItem: RecipeListItemData): Boolean =
        oldItem == newItem
}

