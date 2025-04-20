package com.example.elixir.signup

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.elixir.databinding.FragmentSurvey3Binding

class Survey3Fragment : Fragment() {
    private lateinit var survey3Binding: FragmentSurvey3Binding
    private var preferredRecipes = mutableListOf<String>()
    var listener: OnChipCompletedListener? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // 바인딩
        survey3Binding = FragmentSurvey3Binding.inflate(inflater, container, false)
        return survey3Binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(survey3Binding) {
            // 일반 칩
            val chipList = listOf(dietKorean, dietChinese, dietJapanese, dietWestern, dessert,
                beverage, sauce)
            // 해당 없음
            val chipNone = dietNA

            // 일반 칩 클릭 시
            chipList.forEach { chip ->
                chip.setOnCheckedChangeListener { _, isChecked ->
                    // 해당 없음 해제
                    if (isChecked) {
                        chipNone.isChecked = false
                        preferredRecipes.clear()
                        // 만약에 리스트에 같은 재료가 없다면 추가
                        if (!preferredRecipes.contains(chip.text.toString())) {
                            preferredRecipes.add(chip.text.toString())
                        }
                    }// 만약에 리스트에 같은 재료가 있다면 삭제
                    else {
                        preferredRecipes.remove(chip.text.toString())
                    }
                    // 업데이트
                    updateSelection()
                }
            }
            // "해당 없음" 클릭 시 나머지 해제
            chipNone.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    chipList.forEach { it.isChecked = false }
                    updateSelection()
                }
            }
        }
    }

    private fun updateSelection() {
        // 공백이 아니라면 칩이 선택된 상태 -> 다음 버튼 활성화
        if (preferredRecipes.isNotEmpty()) {
            listener?.onChipSelected(preferredRecipes)
        } else {
            listener?.onChipSelectedNot()
        }
    }
}