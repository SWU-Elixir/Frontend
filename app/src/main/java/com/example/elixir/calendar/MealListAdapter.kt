package com.example.elixir.calendar

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import com.example.elixir.R
import com.example.elixir.ToolbarActivity
import com.example.elixir.databinding.ItemMealListBinding
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.flexbox.JustifyContent

class MealListAdapter(
    private val context: Context,
    private var data: MutableList<MealPlanData>
) : BaseAdapter() {
    override fun getCount(): Int = data.size
    override fun getItem(position: Int): MealPlanData = data[position]
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

        binding.dietPicture.setImageResource(pictureRes)
        binding.dietNameText.text = item.name

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
        binding.dietIngredientList.adapter = MealListIngredientAdapter(item.mealPlanIngredients)

        // Flexbox 레이아웃 매니저 설정
        val layoutManager: FlexboxLayoutManager = FlexboxLayoutManager(context)
        layoutManager.setFlexDirection(FlexDirection.COLUMN)
        layoutManager.setJustifyContent(JustifyContent.FLEX_END)

        // 식단 아이템 클릭 시 상세 화면으로 이동
        view.setOnClickListener {
            val intent = Intent(context, ToolbarActivity::class.java).apply {
                putExtra("mode", 4)  // 식단 상세 모드
                putExtra("mealName", item.name)  // 식단 이름
            }
            context.startActivity(intent)
        }

        return view
    }

    /**
     * 어댑터의 데이터를 새로운 데이터로 업데이트
     */
    fun updateData(newData: List<MealPlanData>) {
        data.clear()
        data.addAll(newData)
        notifyDataSetChanged()
    }
}
