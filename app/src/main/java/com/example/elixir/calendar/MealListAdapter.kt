package com.example.elixir.calendar

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.elixir.R
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.flexbox.JustifyContent

class MealListAdapter(
    private val context: Context,
    private var data: MutableList<DietLogItem>,
    private val onItemClick: (DietLogItem) -> Unit
) : BaseAdapter() {

    override fun getCount(): Int = data.size
    override fun getItem(position: Int): DietLogItem = data[position]
    override fun getItemId(position: Int): Long = position.toLong()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view: View
        val holder: ViewHolder

        if (convertView == null) {
            view = LayoutInflater.from(context).inflate(R.layout.item_dietlog_list, parent, false)
            holder = ViewHolder(view)
            view.tag = holder
        } else {
            view = convertView
            holder = view.tag as ViewHolder
        }

        val item = getItem(position)

        holder.dietTimesText.text = item.dietTimes
        // 점수(Score)에 따라 아이콘 변경
        val pictureRes = item.dietImageRes ?: when (item.dietTimes) {
            "아침" -> R.drawable.ic_dietlog_morning
            "점심" -> R.drawable.ic_dietlog_lunch
            "저녁" -> R.drawable.ic_dietlog_dinner
            "간식" -> R.drawable.ic_dietlog_snack
            else -> R.color.elixir_gray // 기본 아이콘
        }
        holder.dietPicture.setImageResource(pictureRes)
        holder.dietNameText.text = item.dietName
        // 점수(Score)에 따라 아이콘 변경
        val iconRes = when (item.dietScore) {
            1 -> R.drawable.ic_dietlog_number1
            2 -> R.drawable.ic_dietlog_number2
            3 -> R.drawable.ic_dietlog_number3
            4 -> R.drawable.ic_dietlog_number4
            5 -> R.drawable.ic_dietlog_number5
            else -> R.drawable.ic_dietlog_number1 // 기본 아이콘
        }
        holder.dietScoreIcon.setImageResource(iconRes)

        // 재료 목록 RecyclerView 설정
        holder.dietIngredientList.layoutManager = FlexboxLayoutManager(context)
        holder.dietIngredientList.adapter = MealListIngredientAdapter(item.dietIngredients)

        val layoutManager: FlexboxLayoutManager = FlexboxLayoutManager(context)
        layoutManager.setFlexDirection(FlexDirection.COLUMN)
        layoutManager.setJustifyContent(JustifyContent.FLEX_END)

        // 클릭 이벤트 설정
        view.setOnClickListener { onItemClick(item) }

        return view
    }

    fun updateData(newData: List<DietLogItem>) {
        data.clear()
        data.addAll(newData)
        notifyDataSetChanged()
    }

    private class ViewHolder(view: View) {
        val dietTimesText: TextView = view.findViewById(R.id.dietTimesText)
        val dietNameText: TextView = view.findViewById(R.id.dietNameText)
        val dietIngredientList: RecyclerView = view.findViewById(R.id.dietIngredientList)
        val dietScoreIcon: ImageView = view.findViewById(R.id.dietScore)
        val dietPicture: ImageView = view.findViewById(R.id.dietPicture)
    }
}
