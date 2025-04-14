package com.example.elixir

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.core.content.ContextCompat

class RecipeListSpinnerAdapter(
    context: Context,
    private val items: List<String>
) : ArrayAdapter<String>(context, R.layout.item_recipe_category_spinner, items) {

    private val inflater = LayoutInflater.from(context)
    private var selectedPosition: Int = -1 // 선택된 포지션 추적

    override fun isEnabled(position: Int): Boolean {
        return true
    }

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = inflater.inflate(R.layout.item_recipe_category_spinner_dropdown, parent, false)
        val textView = view.findViewById<TextView>(R.id.spinnerItemText)
        textView.text = items[position]

        // 선택된 항목이면 주황색, 아니면 검정
        val colorRes = if (position == selectedPosition && position != 0) R.color.elixir_orange else R.color.black
        textView.setTextColor(ContextCompat.getColor(context, colorRes))

        return view
    }

    @SuppressLint("MissingInflatedId")
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = inflater.inflate(R.layout.item_recipe_category_spinner, parent, false)
        val textView = view.findViewById<TextView>(R.id.spinnerText)
        textView.text = items[position]

        // 선택된 항목은 오렌지, 기본은 검정
        val colorRes = if (position == 0) R.color.black else R.color.elixir_orange
        textView.setTextColor(ContextCompat.getColor(context, colorRes))

        return view
    }
}
