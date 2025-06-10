package com.example.elixir.recipe.ui

import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.ViewGroup
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
                // 삭제 버튼 클릭 시 해당 아이템 삭제
                btnDel.setOnClickListener {
                    onDeleteClick(position)
                }

                // 재료명 설정
                enterItemData.setText(item.name)
                enterItemData.addTextChangedListener(object : TextWatcher {
                    override fun afterTextChanged(s: Editable?) {
                        item.name = s.toString()
                        onUpdateButtonState() // 버튼 상태 업데이트 호출
                    }
                    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                })

                // 양 설정
                enterItemUnit.setText(item.unit)
                enterItemUnit.addTextChangedListener(object : TextWatcher {
                    override fun afterTextChanged(s: Editable?) {
                        item.unit = s.toString()
                        onUpdateButtonState() // 버튼 상태 업데이트 호출
                    }
                    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                })
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