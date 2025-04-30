package com.example.elixir.calendar

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.RecyclerView
import com.example.elixir.R
import com.example.elixir.recipe.RecipeDetailFragment
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.flexbox.JustifyContent

class MealListAdapter(
    private val context: Context,
    private var data: MutableList<MealPlanData>,
    private val fragmentManager: FragmentManager
) : BaseAdapter() {

    override fun getCount(): Int = data.size
    override fun getItem(position: Int): MealPlanData = data[position]
    override fun getItemId(position: Int): Long = position.toLong()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view: View
        val holder: ViewHolder

        if (convertView == null) {
            view = LayoutInflater.from(context).inflate(R.layout.item_meal_list, parent, false)
            holder = ViewHolder(view)
            view.tag = holder
        } else {
            view = convertView
            holder = view.tag as ViewHolder
        }

        val item = getItem(position)

        holder.mealTimesText.text = item.mealtimes
/*
        // 이미지 설정: imageUrl이 있으면 사용하고 없으면 식사 시간에 맞는 아이콘 사용
        val pictureRes = item.imageUrl ?: when (item.mealtimes) {
            "아침" -> R.drawable.ic_meal_morning
            "점심" -> R.drawable.ic_meal_lunch
            "저녁" -> R.drawable.ic_meal_dinner
            "간식" -> R.drawable.ic_meal_snack
            else -> R.color.elixir_gray // 기본 아이콘
        }

        holder.mealPicture.setImageResource(pictureRes)*/
        // 이미지 설정
        val imageUriString = item.imageUrl
        val imageUri = Uri.parse(imageUriString)
        holder.mealPicture.setImageURI(imageUri)

        holder.mealNameText.text = item.name

        // 점수(Score)에 따라 아이콘 변경
        val iconRes = when (item.score) {
            1 -> R.drawable.ic_meal_number1
            2 -> R.drawable.ic_meal_number2
            3 -> R.drawable.ic_meal_number3
            4 -> R.drawable.ic_meal_number4
            5 -> R.drawable.ic_meal_number5
            else -> R.drawable.ic_meal_number1 // 기본 아이콘
        }
        holder.mealScoreIcon.setImageResource(iconRes)

        // 재료 목록 RecyclerView 설정
        holder.mealIngredientList.layoutManager = FlexboxLayoutManager(context)
        holder.mealIngredientList.adapter = MealListIngredientAdapter(item.mealPlanIngredients)

        val layoutManager: FlexboxLayoutManager = FlexboxLayoutManager(context)
        layoutManager.setFlexDirection(FlexDirection.COLUMN)
        layoutManager.setJustifyContent(JustifyContent.FLEX_END)

        // 클릭 이벤트 설정
        view.setOnClickListener {
            Log.d("RecipeAdapter", "아이템 클릭됨: ${item.name}")

            // 레시피 상세 프래그먼트 생성 및 데이터 전달
            val detailFragment = MealDetailFragment().apply {
                arguments = Bundle().apply {
                    putString("meal", item.name)
                    putString("createdAt", item.createdAt)
                    putString("mealtimes", item.mealtimes)
                    putStringArrayList("mealPlanIngredients", ArrayList(item.mealPlanIngredients))
                    putString("imageUrl", item.imageUrl)
                }
            }

            fragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, detailFragment)
                .addToBackStack(null)
                .commit()
        }

        return view
    }

    fun updateData(newData: List<MealPlanData>) {
        data.clear()
        data.addAll(newData)
        notifyDataSetChanged()
    }

    private class ViewHolder(view: View) {
        val mealTimesText: TextView = view.findViewById(R.id.dietTimesText)
        val mealNameText: TextView = view.findViewById(R.id.dietNameText)
        val mealIngredientList: RecyclerView = view.findViewById(R.id.dietIngredientList)
        val mealScoreIcon: ImageView = view.findViewById(R.id.dietScore)
        val mealPicture: ImageView = view.findViewById(R.id.dietPicture)
    }
}
