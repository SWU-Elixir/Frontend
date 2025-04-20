package com.example.elixir.signup

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.elixir.databinding.FragmentSurvey1Binding

class Survey1Fragment : Fragment() {
    private lateinit var survey1Binding: FragmentSurvey1Binding
    private var allergies = mutableListOf<String>()
    var listener: OnChipCompletedListener? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        survey1Binding = FragmentSurvey1Binding.inflate(inflater, container, false)
        return survey1Binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(survey1Binding) {
            // 일반 칩
            val chipList = listOf(
                allergyEgg, allergyMilk, allergyBuckwheat, allergyPeanut,
                allergySoybean, allergyWheat, allergyMackerel, allergyCrab, allergyShrimp,
                allergyPig, allergyPeach, allergyTomato, allergyDioxide, allergyWalnut,
                allergyChicken, allergyCow, allergySquid, allergySeashell, allergyOyster,
                allergyPinenut
            )
            // 해당 없음
            val chipNone = nA

            // 일반 칩 선택 시
            chipList.forEach { chip ->
                chip.setOnCheckedChangeListener { _, isChecked ->
                    if (isChecked) {
                        // 해당 없음 해제 및 배열에서 지우기
                        chipNone.isChecked = false
                        allergies.clear()
                        // 만약에 리스트에 같은 재료가 없다면 추가
                        if (!allergies.contains(chip.text.toString())) {
                            allergies.add(chip.text.toString())
                        }
                    }
                    // 만약에 리스트에 같은 재료가 있다면 삭제
                    else {
                        allergies.remove(chip.text.toString())
                    }
                    // 업데이트
                    updateSelection()
                }
            }

            // "해당 없음" 선택 시
            chipNone.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    // 일반 칩 전부 해제
                    chipList.forEach { it.isChecked = false }
                    allergies.clear()
                    allergies.add(chipNone.text.toString())
                } else {
                    allergies.remove(chipNone.text.toString())
                }
                updateSelection()
            }
        }
    }

    private fun updateSelection() {
        // 공백이 아니라면 칩이 선택된 상태 -> 다음 버튼 활성화
        if (allergies.isNotEmpty()) {
            listener?.onChipSelected(allergies)
        } else {
            listener?.onChipSelectedNot()
        }
    }
}