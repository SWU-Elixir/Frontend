package com.example.elixir.calendar.ui

import android.content.Context
import android.content.Intent
import android.net.Uri
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

    // MealDetailFragment 띄우기
    private lateinit var mealDetailLauncher: ActivityResultLauncher<Intent>

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

        Glide.with(context)
            .load(item.dietImg) // file://, content://, http:// 모두 지원
            .placeholder(R.drawable.img_blank) // 로딩 중 표시할 이미지
            .into(binding.dietPicture)
        
        binding.dietNameText.text = item.dietTitle

        // 식단 점수에 따른 아이콘 설정 (1~5점)
        val iconRes = when (item.score) {
            1 -> R.drawable.ic_meal_number1
            2 -> R.drawable.ic_meal_number2
            3 -> R.drawable.ic_meal_number3
            4 -> R.drawable.ic_meal_number4
            5 -> R.drawable.ic_meal_number5
            else -> R.drawable.ic_meal_number1 // 기본 아이콘
        }
        binding.dietScore.setImageResource(iconRes)

        // 재료 목록을 FlexboxLayoutManager를 사용하여 표시
        binding.dietIngredientList.layoutManager = FlexboxLayoutManager(context)
        binding.dietIngredientList.adapter = MealListIngredientAdapter(item.ingredientTags, ingredientMap)

        // Flexbox 레이아웃 매니저 설정
        val layoutManager = FlexboxLayoutManager(context)
        layoutManager.flexDirection = FlexDirection.COLUMN
        layoutManager.justifyContent = JustifyContent.FLEX_END

        view.setOnClickListener {
            listener.onMealClick(item)
        }

        // 식사 시간 표시
        binding.dietTimesText.text = item.dietCategory

        return view
    }

    //어댑터의 데이터를 새로운 데이터로 업데이트
    fun updateData(newData: List<DietLogData>) {
        data.clear()
        data.addAll(newData)
        notifyDataSetChanged()
    }

    // 식재료 세팅
    fun setIngredientMap(map: Map<Int, IngredientData>) {
        this.ingredientMap = map
        notifyDataSetChanged()
    }
}