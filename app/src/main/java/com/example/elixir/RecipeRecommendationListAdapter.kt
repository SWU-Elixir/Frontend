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
    private val recipeList: List<RecipeItem>
) : RecyclerView.Adapter<RecipeRecommendationListAdapter.RecipeViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecipeViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_recipe_recommendation_list, parent, false)
        return RecipeViewHolder(view)
    }

    override fun onBindViewHolder(holder: RecipeViewHolder, position: Int) {
        val recipe = recipeList[position]

        // 레시피 제목 설정
        holder.recipeTitle.text = recipe.recipeName

        // 이미지 설정 (더미 이미지 사용 가능)
        holder.recipeImage.setImageResource(R.drawable.png_recipe_sample)

        // 재료 태그 RecyclerView 설정 (LayoutManager를 apply로 한 번만 설정)
        holder.ingredientRecyclerView.apply {
            if (layoutManager == null) {
                layoutManager = LinearLayoutManager(holder.itemView.context, LinearLayoutManager.HORIZONTAL, false)
            }
            adapter = RecipeIngredientAdapter(recipe.recipeIngredients)
        }
        holder.categoryRecyclerView.apply {
            if (layoutManager == null) {
                layoutManager = LinearLayoutManager(holder.itemView.context, LinearLayoutManager.HORIZONTAL, false)
            }
            adapter = CategoryAdapter(recipe.recipeCategory)
        }

        // 검색 버튼 클릭 이벤트 예제
        holder.bookmarkButton.setOnClickListener {
            // 검색 버튼 클릭 시 동작 추가 (예제)
            Log.e("RecipeRecommendationListAdapter", "북마크 버튼 클릭")
        }
    }

    override fun getItemCount(): Int = recipeList.size

    class RecipeViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val recipeImage: ImageView = view.findViewById(R.id.recipeImage)
        val recipeTitle: TextView = view.findViewById(R.id.recipeNameText)
        val categoryRecyclerView: RecyclerView = view.findViewById(R.id.categoryList)
        val ingredientRecyclerView: RecyclerView = view.findViewById(R.id.ingredientList)
        val bookmarkButton: ImageButton = view.findViewById(R.id.bookmarkButton)
    }
}
