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
        // 바인딩 정의
        survey2Binding = FragmentSurvey2Binding.inflate(inflater, container, false)
        return survey2Binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(survey2Binding) {
            // 칩과 서버 전송 텍스트(값) 묶기
            val chipToTextMap = mapOf(
                dietMeat to "고기위주",
                dietVegetable to "채소위주",
                dietMeatVegetable to "혼합식"
            )
            val chipList = chipToTextMap.keys.toList()                  // 일반 칩 리스트
            val chipNone = dietNothing                                  // 특별히 없음 칩

            val savedDiets = userModel.getPreferredDiets()              // 뷰 모델에 저장된 선호 식단 정보 가져오기

            // 뷰 모델에 저장된 선호 식단 정보가 있다면 ui에 반영, 없다면 특별히 없음 선택(빈 리스트 or null)
            if (!savedDiets.isNullOrEmpty()) {
                preferredDiets.clear()
                preferredDiets.addAll(savedDiets.filterNot { it == chipNone.text.toString() })

                chipList.forEach { chip ->
                    chip.isChecked = preferredDiets.contains(chipToTextMap[chip])
                }
                chipNone.isChecked = savedDiets.contains(chipNone.text.toString())

                listener?.onChipSelected(savedDiets)
            } else {
                chipList.forEach { it.isChecked = false }
                chipNone.isChecked = true

                listener?.onChipSelected(null)
            }

            chipList.forEach { chip ->
                chip.setOnCheckedChangeListener { _, isChecked ->
                    // 일반 칩이 선택되면, 선호 식단 정보를 저장하고 특별히 없음 칩 선택 해제
                    val chipText = chipToTextMap[chip]!!
                    if (isChecked) {
                        chipNone.isChecked = false
                        if (!preferredDiets.contains(chipText)) {
                            preferredDiets.add(chipText)
                        }
                    } else {
                        // 모든 칩 선택 해제 시 특별히 없음 선택
                        preferredDiets.remove(chipText)

                        val anyChecked = chipList.any { it.isChecked }
                        if (!anyChecked) {
                            chipNone.isChecked = true
                            userModel.setPreferredDiets(null)
                        }
                    }
                    // 일반 칩 선택 시 정보 저장
                    if (!chipNone.isChecked) {
                        userModel.setPreferredDiets(preferredDiets)
                    }
                    updateSelection()
                }
            }

            // 특별히 없음 선택 시 null로 저장
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
        val diets = userModel.getPreferredDiets()
        if (diets == null) {
            listener?.onChipSelected(null)
        } else {
            listener?.onChipSelected(diets)
        }
    }
}
