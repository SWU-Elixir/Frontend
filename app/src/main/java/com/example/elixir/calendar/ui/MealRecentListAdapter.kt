package com.example.elixir.calendar.ui

import android.content.Context
import android.os.Build
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.elixir.R
import com.example.elixir.calendar.data.DietLogData
import com.example.elixir.databinding.ItemMealListBinding
import com.example.elixir.ingredient.data.IngredientData
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.flexbox.JustifyContent
import org.threeten.bp.format.DateTimeFormatter

class MealRecentListAdapter(
    private val context: Context,
    private var data: MutableList<DietLogData>,
    private val listener: OnMealClickListener,
    private var ingredientMap: Map<Int, IngredientData> = emptyMap()
) : RecyclerView.Adapter<MealRecentListAdapter.MealLogViewHolder>() { // RecyclerView.Adapter 상속

    // ViewHolder 내부 클래스 정의
    inner class MealLogViewHolder(private val binding: ItemMealListBinding) :
        RecyclerView.ViewHolder(binding.root) {

        // bind 메소드를 사용하여 뷰에 데이터를 바인딩합니다.
        @RequiresApi(Build.VERSION_CODES.O)
        fun bind(item: DietLogData) {
            Log.d("MealListAdapter", "Loading image for item: ${item.dietTitle}, URI: ${item.dietImg}")

            // --- 이미지 처리: Glide를 사용하여 간결하게 처리합니다 ---
            Glide.with(context)
                .load(item.dietImg)
                .placeholder(R.drawable.img_blank)
                .error(R.drawable.img_blank)
                .into(binding.imgDiet)
            // --- 이미지 처리 끝 ---

            binding.tvDietName.text = item.dietTitle

            // 식단 점수에 따른 아이콘 설정 (1~5점)
            val iconRes = when (item.score) {
                1 -> R.drawable.ic_meal_number1
                2 -> R.drawable.ic_meal_number2
                3 -> R.drawable.ic_meal_number3
                4 -> R.drawable.ic_meal_number4
                5 -> R.drawable.ic_meal_number5
                else -> R.drawable.ic_meal_number1 // 기본 아이콘
            }
            binding.tvScoreLabel.setImageResource(iconRes)

            // --- 재료 목록을 FlexboxLayoutManager를 사용하여 표시 (방어 코드 추가) ---
            if (item.ingredientTags.isNullOrEmpty()) {
                binding.rvDietIngredient.visibility = android.view.View.GONE
                Log.d("MealListAdapter", "No ingredient tags for item: ${item.dietTitle}, hiding RecyclerView.")
            } else {
                binding.rvDietIngredient.visibility = android.view.View.VISIBLE

                // FlexboxLayoutManager는 ViewHolder가 생성될 때 한 번만 설정하면 됩니다.
                if (binding.rvDietIngredient.layoutManager == null) {
                    val flexboxLayoutManager = FlexboxLayoutManager(context)
                    flexboxLayoutManager.flexDirection = FlexDirection.ROW
                    flexboxLayoutManager.justifyContent = JustifyContent.FLEX_START
                    binding.rvDietIngredient.layoutManager = flexboxLayoutManager
                    Log.d("MealListAdapter", "FlexboxLayoutManager set for dietIngredientList.")
                }
                binding.rvDietIngredient.adapter = MealListIngredientAdapter(item.ingredientTags, ingredientMap)
            }
            // --- 재료 목록 처리 끝 ---

            // 클릭 리스너 설정
            binding.root.setOnClickListener {
                listener.onMealClick(item)
            }

            val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
            // 식사 시간 표시
            binding.tvDietTimes.text = buildString {
                append(item.time.format(dateFormatter)) // 년월일만 출력
                append(" ")
                append(item.dietCategory)
            }
        }
    }

    // 뷰홀더 생성
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MealLogViewHolder {
        val binding = ItemMealListBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MealLogViewHolder(binding)
    }

    // 뷰홀더에 데이터 바인딩
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBindViewHolder(holder: MealLogViewHolder, position: Int) {
        holder.bind(data[position])
    }

    // 아이템 개수 반환
    override fun getItemCount(): Int = data.size

    // 데이터 업데이트 메소드 - 최신순으로 정렬
    fun updateData(newData: List<DietLogData>) {
        // 날짜와 시간 기준으로 최신순 정렬 (내림차순)
        val sortedData = newData.sortedByDescending { it.time }.toMutableList()

        data.clear()
        data.addAll(sortedData)
        notifyDataSetChanged()
        Log.d("MealListAdapter", "Data updated with latest sort. Total items: ${data.size}")
    }

    // 재료 맵 업데이트 메소드 (기존과 동일)
    fun setIngredientMap(map: Map<Int, IngredientData>) {
        this.ingredientMap = map
        notifyDataSetChanged()
        Log.d("MealListAdapter", "Ingredient map updated. Map size: ${ingredientMap.size}")
    }

    fun getIngredientMap(): Map<Int, IngredientData> {
        return ingredientMap
    }
}