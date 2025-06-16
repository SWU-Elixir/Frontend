package com.example.elixir.signup

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.elixir.databinding.FragmentSurvey2Binding

class Survey2Fragment : Fragment() {
    private lateinit var survey2Binding: FragmentSurvey2Binding         // 바인딩 객체
    private val userModel: UserInfoViewModel by activityViewModels()    // 뷰 모델 연결
    private var preferredDiets = mutableListOf<String>()                // 선호 식단 리스트
    var listener: OnChipCompletedListener? = null                       // 칩 선택 여부를 알려줄 인터페이스

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // 바인딩
        survey2Binding = FragmentSurvey2Binding.inflate(inflater, container, false)
        return survey2Binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(survey2Binding) {
            // 칩 묶기
            val chipToTextMap = mapOf(
                dietMeat to "고기위주",
                dietVegetable to "채소위주",
                dietMeatVegetable to "혼합식"
            )

            // 일반 칩 리스트
            val chipList = listOf(dietMeat, dietVegetable, dietMeatVegetable)

            // 특별히 없음 칩
            val chipNone = dietNothing

            // 뷰 모델에 저장된 값이 있다면 불러와 선택 상태 복원
            userModel.getPreferredDiets()?.let { savedDiets ->
                preferredDiets.clear()
                savedDiets.forEach { item ->
                    if (item != chipNone.text.toString()) { // chipNone이 아닌 것만 추가
                        preferredDiets.add(item)
                    }
                }
                chipList.forEach { chip ->
                    val chipText = chipToTextMap[chip]!!
                    chip.isChecked = preferredDiets.contains(chipText)
                }
                chipNone.isChecked = savedDiets.contains(chipNone.text.toString())
                updateSelection()
            }

            // 일반 칩 선택 시
            chipList.forEach { chip ->
                chip.setOnCheckedChangeListener { _, isChecked ->
                    val chipText = chipToTextMap[chip]!!
                    if (isChecked) {
                        chipNone.isChecked = false
                        if (!preferredDiets.contains(chipText))
                            preferredDiets.add(chipText)
                    } else {
                        preferredDiets.remove(chipText)
                        // 모든 칩이 해제되면 chipNone을 선택
                        if (preferredDiets.isEmpty()) {
                            chipNone.isChecked = true
                            userModel.setPreferredDiets(null)
                        }
                    }
                    userModel.setPreferredDiets(preferredDiets)
                    updateSelection()
                }
            }

            // chipNone 선택 시
            chipNone.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    chipList.forEach { it.isChecked = false }
                    preferredDiets.clear()
                    userModel.setPreferredDiets(null)
                }
                updateSelection()
            }
        }
    }

    private fun updateSelection() {
        // userModel.getPreferredDiets()가 null이면 chipNone이 선택된 상태
        if (userModel.getPreferredDiets() == null) {
            listener?.onChipSelected(null)
        } else {
            listener?.onChipSelected(userModel.getPreferredDiets())
        }
    }
}