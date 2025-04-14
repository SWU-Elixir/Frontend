package com.example.elixir

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class RecipeRecommendationListAdapter(
    private val recipeList: List<RecommendationRecipeItem>
) : RecyclerView.Adapter<RecipeRecommendationListAdapter.RecipeViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecipeViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_recipe_recommendation_list, parent, false)
        return RecipeViewHolder(view)
    }

    override fun onBindViewHolder(holder: RecipeViewHolder, position: Int) {
        val recipe = recipeList[position]

        // 레시피 제목 설정
        holder.recipeTitle.text = recipe.recipeTitle

        // 이미지 설정 (더미 이미지 사용 가능)
        holder.recipeImage.setImageResource(R.drawable.png_recipe_sample)

        // 재료 태그 RecyclerView 설정 (LayoutManager를 apply로 한 번만 설정)
        holder.ingredientRecyclerView.apply {
            if (layoutManager == null) {
                layoutManager = LinearLayoutManager(holder.itemView.context, LinearLayoutManager.HORIZONTAL, false)
            }
            adapter = RecipeIngredientAdapter(recipe.recipeIngredients)
        }

        // 북마크 상태에 따라 버튼 이미지 설정
        if (recipe.isBookmarked) {
            holder.bookmarkButton.setImageResource(R.drawable.ic_recipe_bookmark_selected)
        } else {
            holder.bookmarkButton.setImageResource(R.drawable.ic_recipe_bookmark_normal)
        }

        holder.categorySlowAging.text = recipe.categorySlowAging
        holder.categoryType.text = recipe.categoryType

        // 북마크 버튼 클릭 이벤트 처리
        holder.bookmarkButton.setOnClickListener {
            recipe.isBookmarked = !recipe.isBookmarked
            notifyItemChanged(position) // 변경된 항목 갱신
            Log.d("RecipeRecommendationListAdapter", "북마크 상태 변경: ${'$'}{recipe.recipeName} -> ${'$'}{recipe.isBookmarked}")
        }

        holder.itemView.setOnClickListener {
            Log.d("RecipeAdapter", "아이템 클릭됨: ${recipe.recipeTitle}")
            // 필요시 클릭 시 동작 추가 가능
        }
    }

    override fun getItemCount(): Int = recipeList.size

    class RecipeViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val recipeImage: ImageView = view.findViewById(R.id.recipeImage)
        val recipeTitle: TextView = view.findViewById(R.id.recipeNameText)
        val categorySlowAging: TextView = view.findViewById(R.id.category_slow_aging)
        val categoryType: TextView = view.findViewById(R.id.category_type)
        val ingredientRecyclerView: RecyclerView = view.findViewById(R.id.ingredientList)
        val bookmarkButton: ImageButton = view.findViewById(R.id.bookmarkButton)
    }
}
