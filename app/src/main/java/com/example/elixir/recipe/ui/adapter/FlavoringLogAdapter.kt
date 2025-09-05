package com.example.elixir.recipe.ui.adapter

import android.text.Editable
import android.text.TextWatcher
import android.util.Log
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
    private val onDeleteClick: (Int) -> Unit, // onDeleteClick으로 콜백 이름을 사용합니다.
    private val onUpdateButtonState: () -> Unit
) : RecyclerView.Adapter<FlavoringLogAdapter.ItemViewHolder>() {

    inner class ItemViewHolder(val binding: ItemFlavoringBinding) : RecyclerView.ViewHolder(binding.root) {
        private var nameWatcher: TextWatcher? = null
        private var amountWatcher: TextWatcher? = null
        private var unitWatcher: TextWatcher? = null
        private var spinnerListener: AdapterView.OnItemSelectedListener? = null

        fun bind(item: FlavoringItem, position: Int) {
            with(binding) {
                // 기존 리스너 제거 (중복 등록 방지)
                enterItemData.removeTextChangedListener(nameWatcher)
                enterItemAmount.removeTextChangedListener(amountWatcher)
                enterItemUnit.removeTextChangedListener(unitWatcher)
                spinnerUnit.onItemSelectedListener = null

                // 단위 목록 및 어댑터 설정
                val units = listOf(
                    "cm(센티미터)", "L(리터)", "ml(밀리리터)", "g(그램)",
                    "큰술(Tbsp/큰 술/tablespoon)", "작은술(tsp/작은 술/teaspoon)",
                    "꼬집(a pinch)", "컵(cup)", "개(개수 단위)", "줌(한 줌)", "직접 입력"
                )
                fun getShortUnit(full: String): String {
                    return when (full) {
                        "직접 입력" -> ""
                        else -> full.substringBefore("(").trim()
                    }
                }
                val adapter = ArrayAdapter(root.context, android.R.layout.simple_spinner_item, units)
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                spinnerUnit.adapter = adapter

                // Spinner 위치 설정 (기존 데이터에 맞는 단위 선택)
                val unitIndex = units.indexOfFirst { getShortUnit(it) == item.unit }
                spinnerUnit.setSelection(if (unitIndex != -1) unitIndex else units.size - 1)

                // "직접 입력" 선택 시 enterItemUnit 활성화 여부
                enterItemUnit.isEnabled = (spinnerUnit.selectedItem == "직접 입력")


                // Spinner 리스너 등록
                spinnerListener = object : AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(parent: AdapterView<*>, view: View?, pos: Int, id: Long) {
                        val selected = units[pos]
                        if (selected == "직접 입력") {
                            enterItemUnit.isEnabled = true
                            enterItemUnit.setText(item.unit) // 기존 직접 입력 값 유지
                        } else {
                            val shortUnit = getShortUnit(selected)
                            enterItemUnit.isEnabled = false
                            enterItemUnit.setText(shortUnit)
                            item.unit = shortUnit // 선택된 단위로 업데이트
                        }
                        onUpdateButtonState() // UI 상태 갱신
                    }
                    override fun onNothingSelected(parent: AdapterView<*>) {}
                }
                spinnerUnit.onItemSelectedListener = spinnerListener

                // 재료명 입력
                enterItemData.setText(item.name)
                nameWatcher = object : TextWatcher {
                    override fun afterTextChanged(s: Editable?) {
                        item.name = s.toString()
                        onUpdateButtonState()
                    }
                    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                }
                enterItemData.addTextChangedListener(nameWatcher)

                // 수량 입력
                enterItemAmount.setText(item.value)
                amountWatcher = object : TextWatcher {
                    override fun afterTextChanged(s: Editable?) {
                        item.value = s.toString()
                        onUpdateButtonState()
                    }
                    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                }
                enterItemAmount.addTextChangedListener(amountWatcher)

                // 단위 직접 입력 시
                enterItemUnit.setText(item.unit)
                unitWatcher = object : TextWatcher {
                    override fun afterTextChanged(s: Editable?) {
                        // "직접 입력"이 선택된 경우에만 item.unit을 업데이트
                        if (spinnerUnit.selectedItem.toString() == "직접 입력") {
                            item.unit = s.toString()
                        }
                        onUpdateButtonState()
                    }
                    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                }
                enterItemUnit.addTextChangedListener(unitWatcher)

                // 삭제 버튼
                btnDel.setOnClickListener {
                    // **중요: adapterPosition을 사용합니다.**
                    val currentPosition = adapterPosition
                    if (currentPosition != RecyclerView.NO_POSITION) { // 유효한 포지션인지 확인
                        onDeleteClick(currentPosition)
                    } else {
                        Log.w("FlavoringLogAdapter", "Delete button clicked but adapterPosition is NO_POSITION.")
                    }
                }

            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val binding = ItemFlavoringBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ItemViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        Log.d("FlavoringLogAdapter", "onBindViewHolder: ${itemList[position].name}, ${itemList[position].unit}")
        holder.bind(itemList[position], position)
    }

    override fun getItemCount(): Int {
        return itemList.size
    }
}