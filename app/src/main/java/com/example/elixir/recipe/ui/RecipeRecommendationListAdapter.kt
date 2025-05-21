package com.example.elixir.recipe.ui

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.elixir.R
import com.example.elixir.databinding.ItemRecipeRecommendationListBinding
import com.example.elixir.recipe.data.RecipeData

class RecipeRecommendationListAdapter(
    private val recipeList: List<RecipeData>
) : RecyclerView.Adapter<RecipeRecommendationListAdapter.RecipeViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecipeViewHolder {
        val binding = ItemRecipeRecommendationListBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return RecipeViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RecipeViewHolder, position: Int) {
        val recipe = recipeList[position]

        // 레시피 제목 설정
        holder.binding.recipeNameText.text = recipe.title

        // 이미지 설정 (더미 이미지 사용 가능)
        holder.binding.recipeImage.setImageResource(R.drawable.png_recipe_sample)

        // 재료 태그 RecyclerView 설정 (LayoutManager를 apply로 한 번만 설정)
        holder.binding.ingredientList.apply {
            if (layoutManager == null) {
                layoutManager = LinearLayoutManager(holder.itemView.context, LinearLayoutManager.HORIZONTAL, false)
            }
            adapter = RecipeIngredientAdapter(recipe.ingredients)
        }

        // 북마크 상태에 따라 버튼 이미지 설정
        if (recipe.isBookmarked) {
            holder.binding.bookmarkButton.setImageResource(R.drawable.ic_recipe_bookmark_selected)
        } else {
            holder.binding.bookmarkButton.setImageResource(R.drawable.ic_recipe_bookmark_normal)
        }

        holder.binding.categorySlowAging.text = recipe.categorySlowAging
        holder.binding.categoryType.text = recipe.categoryType

        // 북마크 버튼 클릭 이벤트 처리
        holder.binding.bookmarkButton.setOnClickListener {
            recipe.isBookmarked = !recipe.isBookmarked
            notifyItemChanged(position) // 변경된 항목 갱신
            Log.d("RecipeRecommendationListAdapter", "북마크 상태 변경: ${recipe.title} -> ${recipe.isBookmarked}")
        }

        holder.itemView.setOnClickListener {
            Log.d("RecipeAdapter", "아이템 클릭됨: ${recipe.title}")
            // 필요시 클릭 시 동작 추가 가능
        }
    }

    override fun getItemCount(): Int = recipeList.size

    class RecipeViewHolder(val binding: ItemRecipeRecommendationListBinding) : RecyclerView.ViewHolder(binding.root)
}
