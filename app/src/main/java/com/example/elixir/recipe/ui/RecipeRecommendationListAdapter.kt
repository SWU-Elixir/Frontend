package com.example.elixir.recipe.ui

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.elixir.R
import com.example.elixir.databinding.ItemRecipeRecommendationListBinding
import com.example.elixir.recipe.data.RecipeListItemData
import com.example.elixir.recipe.viewmodel.RecipeViewModel

class RecipeRecommendationListAdapter(
    private var recipeList: List<RecipeListItemData>,
    private val fragmentManager: FragmentManager,
    private val recipeViewModel: RecipeViewModel
) : RecyclerView.Adapter<RecipeRecommendationListAdapter.RecipeViewHolder>() {

    fun updateData(newRecipeList: List<RecipeListItemData>) {
        recipeList = newRecipeList
        notifyDataSetChanged()
    }

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

        // 이미지 설정 (Glide 사용)
        Glide.with(holder.itemView.context)
            .load(recipe.imageUrl)
            .placeholder(R.drawable.ic_recipe_white)
            .error(R.drawable.ic_recipe_white)
            .into(holder.binding.recipeImage)

        // 재료 태그 RecyclerView 설정 (LayoutManager를 apply로 한 번만 설정)
        holder.binding.ingredientList.apply {
            if (layoutManager == null) {
                layoutManager = LinearLayoutManager(holder.itemView.context, LinearLayoutManager.HORIZONTAL, false)
            }
            //adapter = FlavoringAdapter(recipe.ingredientTagIds.map { FlavoringData(it.key, it.value) })
        }

        // 북마크 상태에 따라 버튼 이미지 설정
        if (recipe.scrappedByCurrentUser) {
            holder.binding.bookmarkButton.setImageResource(R.drawable.ic_recipe_bookmark_selected)
        } else {
            holder.binding.bookmarkButton.setImageResource(R.drawable.ic_recipe_bookmark_normal)
        }

        holder.binding.categorySlowAging.text = recipe.categorySlowAging
        holder.binding.categoryType.text = recipe.categoryType

        holder.binding.bookmarkButton.setOnClickListener {
            // 현재 북마크 상태 반전
            recipe.scrappedByCurrentUser = !recipe.scrappedByCurrentUser

            // API 호출
            if (recipe.scrappedByCurrentUser) {
                recipeViewModel.addScrap(recipe.id)
            } else {
                recipeViewModel.deleteScrap(recipe.id)
            }

            notifyItemChanged(position) // 변경된 항목 갱신
            Log.d("RecipeRecommendationListAdapter", "북마크 상태 변경: ${recipe.title} -> ${recipe.scrappedByCurrentUser}")
        }

        holder.itemView.setOnClickListener {
            Log.d("RecipeAdapter", "아이템 클릭됨: ${recipe.title}")
            val detailFragment = RecipeDetailFragment().apply {
                arguments = Bundle().apply {
                    putInt("recipeId", recipe.id)
                }
            }
            fragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, detailFragment)
                .addToBackStack(null)
                .commit()
        }

    }

    override fun getItemCount(): Int = recipeList.size

    class RecipeViewHolder(val binding: ItemRecipeRecommendationListBinding) : RecyclerView.ViewHolder(binding.root)
}
