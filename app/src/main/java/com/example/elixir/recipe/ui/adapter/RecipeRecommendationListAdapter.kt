package com.example.elixir.recipe.ui.adapter

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.elixir.R
import com.example.elixir.databinding.ItemRecipeRecommendationListBinding
import com.example.elixir.recipe.data.RecipeItemData
import com.example.elixir.ingredient.data.IngredientEntity // IngredientData import 추가
import com.example.elixir.recipe.ui.fragment.RecipeDetailFragment
import com.example.elixir.recipe.viewmodel.RecipeViewModel
import com.google.android.flexbox.FlexDirection // FlexboxLayoutManager를 위한 import 추가
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.flexbox.JustifyContent

class RecipeRecommendationListAdapter(
    private var recipeList: List<RecipeItemData>,
    private val fragmentManager: FragmentManager,
    private val recipeViewModel: RecipeViewModel,
    // ingredientDataMap을 var로 선언하여 외부에서 업데이트 가능하게 함
    private var ingredientDataMap: Map<Int, IngredientEntity>?
) : RecyclerView.Adapter<RecipeRecommendationListAdapter.RecipeViewHolder>() {

    fun updateData(newRecipeList: List<RecipeItemData>) {
        recipeList = newRecipeList
        notifyDataSetChanged()
    }

    // ingredientDataMap을 업데이트하는 메서드 추가
    fun updateIngredientMap(newIngredientMap: Map<Int, IngredientEntity>) {
        this.ingredientDataMap = newIngredientMap
        notifyDataSetChanged() // 데이터가 업데이트되었으므로 어댑터 갱신
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
            .into(holder.binding.imgRecipe)

        // 재료 태그 RecyclerView 설정 (FlexboxLayoutManager 사용)
        holder.binding.ingredientList.apply {
            if (layoutManager == null) {
                // FlexboxLayoutManager를 사용하여 태그가 유연하게 배치되도록 설정
                layoutManager = FlexboxLayoutManager(context).apply {
                    flexDirection = FlexDirection.ROW
                    justifyContent = JustifyContent.FLEX_START
                }
            }
            // ingredientDataMap이 null이 아니고, ingredientTagIds가 있을 경우에만 어댑터 설정
            if (ingredientDataMap != null && !recipe.ingredientTagIds.isNullOrEmpty()) {
                // IngredientTagChipMapAdapter에 ingredientDataMap과 ingredientTagIds 전달
                adapter = IngredientTagChipMapAdapter(recipe.ingredientTagIds!!, ingredientDataMap!!)
                visibility = View.VISIBLE // 태그가 있으면 보이게
            } else {
                visibility = View.GONE // 태그가 없으면 숨김
            }
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
                .replace(R.id.flContainer, detailFragment)
                .addToBackStack(null)
                .commit()
        }
    }

    override fun getItemCount(): Int = recipeList.size

    class RecipeViewHolder(val binding: ItemRecipeRecommendationListBinding) : RecyclerView.ViewHolder(binding.root)
}