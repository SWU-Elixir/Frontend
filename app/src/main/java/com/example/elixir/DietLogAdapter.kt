package com.example.elixir

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.flexbox.JustifyContent

class DietLogAdapter(
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
            view = LayoutInflater.from(context).inflate(R.layout.item_dietlog, parent, false)
            holder = ViewHolder(view)
            view.tag = holder
        } else {
            view = convertView
            holder = view.tag as ViewHolder
        }

        val item = getItem(position)

        holder.mealTimesText.text = item.mealTimes
        // 점수(Score)에 따라 아이콘 변경
        val PictureRes = when (item.mealTimes) {
            "아침" -> R.drawable.ic_dietlog_morning
            "점심" -> R.drawable.ic_dietlog_lunch
            "저녁" -> R.drawable.ic_dietlog_dinner
            "간식" -> R.drawable.ic_dietlog_snack
            else -> R.drawable.ic_dietlog_morning // 기본 아이콘
        }
        holder.mealPicture.setImageResource(PictureRes)
        holder.mealNameText.text = item.mealName
        // 점수(Score)에 따라 아이콘 변경
        val iconRes = when (item.score) {
            1 -> R.drawable.ic_dietlog_number1
            2 -> R.drawable.ic_dietlog_number2
            3 -> R.drawable.ic_dietlog_number3
            4 -> R.drawable.ic_dietlog_number4
            5 -> R.drawable.ic_dietlog_number5
            else -> R.drawable.ic_dietlog_number1 // 기본 아이콘
        }
        holder.mealScoreIcon.setImageResource(iconRes)

        // 재료 목록 RecyclerView 설정
        holder.mealIngredientList.layoutManager = FlexboxLayoutManager(context)
        holder.mealIngredientList.adapter = IngredientAdapter(item.ingredients)

        val layoutManager: FlexboxLayoutManager = FlexboxLayoutManager(context)
        layoutManager.setFlexDirection(FlexDirection.COLUMN)
        layoutManager.setJustifyContent(JustifyContent.FLEX_END)



        // 클릭 이벤트 설정
        //view.setOnClickListener { onItemClick(item) }

        return view
    }

    fun updateData(newData: List<DietLogItem>) {
        data.clear()
        data.addAll(newData)
        notifyDataSetChanged()
    }

    private class ViewHolder(view: View) {
        val mealTimesText: TextView = view.findViewById(R.id.mealTimesText)
        val mealNameText: TextView = view.findViewById(R.id.mealNameText)
        val mealIngredientList: RecyclerView = view.findViewById(R.id.mealIngredientList)
        val mealScoreIcon: ImageView = view.findViewById(R.id.mealScore)
        val mealPicture: ImageView = view.findViewById(R.id.mealPicture)
    }
}
