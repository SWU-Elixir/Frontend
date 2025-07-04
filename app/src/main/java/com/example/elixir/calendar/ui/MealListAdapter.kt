package com.example.elixir.calendar.ui

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import androidx.activity.result.ActivityResultLauncher
import com.bumptech.glide.Glide
import com.example.elixir.R
import com.example.elixir.calendar.data.DietLogData
import com.example.elixir.databinding.ItemMealListBinding
import com.example.elixir.ingredient.data.IngredientData
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.flexbox.JustifyContent

class MealListAdapter(
    private val context: Context,
    private var data: MutableList<DietLogData>,
    private val listener: OnMealClickListener,
    private var ingredientMap: Map<Int, IngredientData> = emptyMap()
) : BaseAdapter() {
    override fun getCount(): Int = data.size
    override fun getItem(position: Int): DietLogData = data[position]
    override fun getItemId(position: Int): Long = position.toLong()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val binding: ItemMealListBinding
        val view: View

        // 뷰 재사용 로직
        if (convertView == null) {
            binding = ItemMealListBinding.inflate(LayoutInflater.from(context), parent, false)
            view = binding.root
            view.tag = binding
        } else {
            binding = convertView.tag as ItemMealListBinding
            view = convertView
        }

        val item = getItem(position)


        // --- 이미지 처리
        Glide.with(context)
            .load(item.dietImg)
            .placeholder(R.drawable.img_blank)
            .error(R.drawable.img_blank)
            .into(binding.imgDiet)

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


        if (item.ingredientTags.isNullOrEmpty()) {
            // 재료 태그가 없으면 RecyclerView를 숨김
            binding.rvDietIngredient.visibility = View.GONE

        } else {
            // 재료 태그가 있으면 RecyclerView를 보여주고 어댑터를 설정
            binding.rvDietIngredient.visibility = View.VISIBLE

            if (binding.rvDietIngredient.layoutManager == null) {
                val flexboxLayoutManager = FlexboxLayoutManager(context)
                flexboxLayoutManager.flexDirection = FlexDirection.ROW // 가로 배치
                flexboxLayoutManager.justifyContent = JustifyContent.FLEX_START // 시작 지점에서 정렬
                binding.rvDietIngredient.layoutManager = flexboxLayoutManager
            }
            binding.rvDietIngredient.adapter = MealListIngredientAdapter(item.ingredientTags, ingredientMap)
        }


        view.setOnClickListener {
            listener.onMealClick(item)
        }

        // 식사 시간 표시
        binding.tvDietTimes.text = item.dietCategory

        return view
    }

    fun updateData(newData: List<DietLogData>) {
        val sortedData = newData.sortedWith(compareBy {
            when (it.dietCategory) {
                context.getString(R.string.breakfast) -> 1 // string resource 사용 권장
                context.getString(R.string.lunch) -> 2
                context.getString(R.string.dinner) -> 3
                context.getString(R.string.snack) -> 4
                else -> 5 // 그 외의 카테고리는 마지막에 정렬
            }
        }).toMutableList()

        data.clear()
        data.addAll(sortedData)
        notifyDataSetChanged()
    }

    fun setIngredientMap(map: Map<Int, IngredientData>) {
        this.ingredientMap = map
        notifyDataSetChanged()
    }
}