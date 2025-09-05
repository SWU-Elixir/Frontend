package com.example.elixir.recipe.ui.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.core.content.ContextCompat
import com.example.elixir.R
import com.example.elixir.databinding.ItemRecipeCategorySpinnerBinding
import com.example.elixir.databinding.ItemRecipeCategorySpinnerDropdownBinding

class RecipeListSpinnerAdapter(
    context: Context,
    private val items: List<String>
) : ArrayAdapter<String>(context, R.layout.item_recipe_category_spinner, items) {

    private val inflater = LayoutInflater.from(context)
    private var selectedPosition: Int = 0 // 기본값을 0으로 설정

    // 선택된 포지션을 설정하는 메서드 추가
    fun setSelectedPosition(position: Int) {
        selectedPosition = position
        notifyDataSetChanged() // 뷰 업데이트
    }

    override fun isEnabled(position: Int): Boolean {
        return true
    }

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
        val binding = ItemRecipeCategorySpinnerDropdownBinding.inflate(inflater, parent, false)
        binding.tvSpinnerItem.text = items[position]

        // 선택된 항목이면 주황색, 아니면 검정
        val colorRes = if (position == selectedPosition && position != 0) R.color.elixir_orange else R.color.black
        binding.tvSpinnerItem.setTextColor(ContextCompat.getColor(context, colorRes))

        return binding.root
    }

    @SuppressLint("MissingInflatedId")
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val binding = ItemRecipeCategorySpinnerBinding.inflate(inflater, parent, false)
        binding.tvSpinner.text = items[position]

        // 스피너 배경의 선택 상태 설정
        binding.root.isSelected = (position == selectedPosition && position != 0)

        // 선택된 항목은 오렌지, 기본은 검정
        val colorRes = if (position == 0) R.color.black else R.color.elixir_orange
        binding.tvSpinner.setTextColor(ContextCompat.getColor(context, colorRes))

        return binding.root
    }
}