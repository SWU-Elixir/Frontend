package com.example.elixir.signup

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.elixir.databinding.FragmentSurvey3Binding

class Survey3Fragment : Fragment() {
    private lateinit var survey3Binding: FragmentSurvey3Binding         // 바인딩 객체
    private val userModel: UserInfoViewModel by activityViewModels()    // 뷰 모델 연결
    private var preferredRecipes = mutableListOf<String>()              // 선호 레시피 리스트
    var listener: OnChipCompletedListener? = null                       // 칩 선택 여부를 알려줄 인터페이스

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // 바인딩 정의
        survey3Binding = FragmentSurvey3Binding.inflate(inflater, container, false)
        return survey3Binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(survey3Binding) {
            // 칩과 서버 전송 텍스트(값) 묶기
            val chipToTextMap = mapOf(
                dietKorean to "한식",
                dietChinese to "중식",
                dietJapanese to "일식",
                dietWestern to "양식",
                dietDessert to "디저트",
                dietBeverage to "음료_차",
                dietSauce to "양념_소스_잼"
            )

            val chipList = chipToTextMap.keys.toList()              // 일반 칩 리스트
            val chipNone = dietNA                                   // 특별히 없음 칩

            val savedRecipes = userModel.getPreferredRecipes()      // 뷰 모델에 저장된 선호 레시피 정보 가져오기

            // 뷰 모델에 저장된 선호 레시피 정보가 있다면 ui에 반영, 없다면 특별히 없음 선택(빈 리스트 or null)
            if (!savedRecipes.isNullOrEmpty()) {
                preferredRecipes.clear()
                preferredRecipes.addAll(savedRecipes.filterNot { it == chipNone.text.toString() })

                chipList.forEach { chip ->
                    chip.isChecked = preferredRecipes.contains(chipToTextMap[chip])
                }
                chipNone.isChecked = savedRecipes.contains(chipNone.text.toString())
                listener?.onChipSelected(savedRecipes)
            } else {
                chipList.forEach { it.isChecked = false }
                chipNone.isChecked = true
                listener?.onChipSelected(null)
            }

            // 일반 칩 이벤트
            chipList.forEach { chip ->
                chip.setOnCheckedChangeListener { _, isChecked ->
                    val chipText = chipToTextMap[chip]!!
                    // 일반 칩이 선택되면, 선호 레시피 정보를 저장하고 특별히 없음 칩 선택 해제
                    if (isChecked) {
                        chipNone.isChecked = false
                        if (!preferredRecipes.contains(chipText)) {
                            preferredRecipes.add(chipText)
                        }
                    } else {
                        // 모든 칩 선택 해제 시 특별히 없음 선택
                        preferredRecipes.remove(chipText)

                        val anyChecked = chipList.any { it.isChecked }
                        if (!anyChecked) {
                            chipNone.isChecked = true
                            userModel.setPreferredRecipes(null)
                        }
                    }
                    // 일반 칩 선택 시 정보 저장
                    if (!chipNone.isChecked) {
                        userModel.setPreferredRecipes(preferredRecipes)
                    }
                    updateSelection()
                }
            }

            // 특별히 없음 칩 선택 시 null로 저장
            chipNone.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    chipList.forEach { it.isChecked = false }
                    preferredRecipes.clear()
                    userModel.setPreferredRecipes(null)
                }
                updateSelection()
            }
        }
    }

    private fun updateSelection() {
        val recipes = userModel.getPreferredRecipes()
        listener?.onChipSelected(recipes)
    }
}
