package com.example.elixir.recipe.ui

import android.R
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.elixir.databinding.ItemFlavoringBinding
import com.example.elixir.recipe.data.FlavoringItem

class FlavoringLogAdapter(
    private val itemList: MutableList<FlavoringItem>,
    private val onDeleteClick: (Int) -> Unit,
    private val onUpdateButtonState: () -> Unit // 버튼 상태 업데이트 함수 전달
) : RecyclerView.Adapter<FlavoringLogAdapter.ItemViewHolder>() {

    inner class ItemViewHolder(val binding: ItemFlavoringBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: FlavoringItem, position: Int) {
            with(binding) {
                val units = listOf(
                    "cm(센티미터)", "L(리터)", "ml(밀리리터)", "g(그램)",
                    "큰술(Tbsp/큰 술/tablespoon)", "작은술(tsp/작은 술/teaspoon)",
                    "꼬집(a pinch)", "컵(cup)", "개(개수 단위)", "줌(한 줌)", "직접 입력"
                )
                // 단위의 짧은 이름만 추출하는 함수
                fun getShortUnit(full: String): String {
                    return when (full) {
                        "직접 입력" -> ""
                        else -> full.substringBefore("(").trim()
                    }
                }
                val adapter = ArrayAdapter(root.context, android.R.layout.simple_spinner_item, units)
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                spinnerUnit.adapter = adapter

                // 기존 값이 있으면 Spinner 위치 맞추기 (짧은 단위로 비교)
                val unitIndex = units.indexOfFirst { getShortUnit(it) == item.unit }
                spinnerUnit.setSelection(if (unitIndex != -1) unitIndex else units.size - 1)

                spinnerUnit.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(parent: AdapterView<*>, view: View?, pos: Int, id: Long) {
                        val selected = units[pos]
                        if (selected == "직접 입력") {
                            enterItemUnit.isEnabled = true
                            enterItemUnit.setText(item.unit)
                        } else {
                            val shortUnit = getShortUnit(selected)
                            enterItemUnit.isEnabled = false
                            enterItemUnit.setText(shortUnit)
                            item.unit = shortUnit
                        }
                        onUpdateButtonState()
                    }
                    override fun onNothingSelected(parent: AdapterView<*>) {}
                }

                // 재료명 입력
                enterItemData.setText(item.name)
                enterItemData.addTextChangedListener(object : TextWatcher {
                    override fun afterTextChanged(s: Editable?) {
                        item.name = s.toString()
                        onUpdateButtonState()
                    }
                    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                })

                // 수량 입력
                enterItemAmount.setText(item.value)
                enterItemAmount.addTextChangedListener(object : TextWatcher {
                    override fun afterTextChanged(s: Editable?) {
                        item.value = s.toString()
                        onUpdateButtonState()
                    }
                    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                })

                // 단위 직접 입력 시
                enterItemUnit.addTextChangedListener(object : TextWatcher {
                    override fun afterTextChanged(s: Editable?) {
                        if (spinnerUnit.selectedItem == "직접 입력") {
                            item.unit = s.toString()
                        }
                        onUpdateButtonState()
                    }
                    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                })

                // 삭제 버튼
                btnDel.setOnClickListener { onDeleteClick(position) }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val binding = ItemFlavoringBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ItemViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        holder.bind(itemList[position], position)
    }

    override fun getItemCount(): Int {
        return itemList.size
    }
}