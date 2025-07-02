package com.example.elixir.recipe.ui

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.elixir.databinding.ItemRecipeRecommendationListIndeterminateBinding // 실제 칩 레이아웃 바인딩으로 변경 고려
import com.example.elixir.ingredient.data.IngredientData

class IngredientTagChipMapAdapter(
    private val tagList: List<Int>, // Chip으로 표시할 재료 ID 리스트
    private val ingredientMap: Map<Int, IngredientData> // ID로 재료를 찾기 위한 Map
) : RecyclerView.Adapter<IngredientTagChipMapAdapter.TagViewHolder>() {

    // 어댑터가 실제로 표시할 재료 이름 목록을 미리 계산하여 저장합니다.
    private val displayIngredientNames: List<String> = tagList.mapNotNull { id ->
        // ingredientMap에서 ID에 해당하는 IngredientData를 찾아 이름을 가져오고,
        // 만약 해당 ID의 재료가 없으면 null을 반환하여 리스트에 추가하지 않습니다.
        ingredientMap[id]?.name
    }

    inner class TagViewHolder(val binding: ItemRecipeRecommendationListIndeterminateBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TagViewHolder {
        val binding = ItemRecipeRecommendationListIndeterminateBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return TagViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TagViewHolder, position: Int) {
        // 미리 계산된 재료 이름 목록에서 직접 가져와 설정합니다.
        // 이로써 onBindViewHolder 호출 시마다 Map 조회를 피할 수 있습니다.
        holder.binding.tvIndeterminateName.text = displayIngredientNames[position]

        // 디버깅 로그는 필요한 경우에만 활성화하세요.
        Log.d("IngredientChip", "Displaying ingredient: ${displayIngredientNames[position]}")
    }

    // getItemCount는 미리 계산된 displayIngredientNames 리스트의 크기를 반환합니다.
    override fun getItemCount(): Int = displayIngredientNames.size
}