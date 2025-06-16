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
    private var preferredRecipes = mutableListOf<String>()              // 레시피 리스트
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
            // 일반 칩 리스트
            val chipList = listOf(
                dietKorean, dietChinese, dietJapanese, dietWestern, dietDessert, dietBeverage, dietSauce
            )
            // 칩별 매핑 맵
            val chipToTextMap = mapOf(
                dietKorean to "한식",
                dietChinese to "중식",
                dietJapanese to "일식",
                dietWestern to "양식",
                dietDessert to "디저트",
                dietBeverage to "음료_차",
                dietSauce to "양념_소스_잼"
            )
            // 특별히 없음 칩
            val chipNone = dietNA

            // 최초에 chipNone을 선택된 상태로
            chipNone.isChecked = true

            // 뷰 모델에 저장된 값이 있다면(Null이 아니라면) 불러와 선택 상태 복원
            userModel.getPreferredRecipes()?.let { savedRecipes ->
                preferredRecipes.clear()
                savedRecipes.forEach { item ->
                    if (item != chipNone.text.toString()) { // chipNone이 아닌 것만 추가
                        preferredRecipes.add(item)
                    }
                }
                chipList.forEach { chip ->
                    val chipText = chipToTextMap[chip]!!
                    chip.isChecked = preferredRecipes.contains(chipText)
                }
                chipNone.isChecked = savedRecipes.contains(chipNone.text.toString())
                updateSelection()
            }

            // 일반 칩 선택 시
            chipList.forEach { chip ->
                chip.setOnCheckedChangeListener { _, isChecked ->
                    val chipText = chipToTextMap[chip]!!
                    if (isChecked) {
                        chipNone.isChecked = false
                        if (!preferredRecipes.contains(chipText))
                            preferredRecipes.add(chipText)
                    } else {
                        preferredRecipes.remove(chipText)
                        // 모든 칩이 해제되면 chipNone을 선택
                        if (preferredRecipes.isEmpty()) {
                            chipNone.isChecked = true
                            userModel.setPreferredRecipes(null)
                        }
                    }
                    userModel.setPreferredRecipes(preferredRecipes)
                    updateSelection()
                }
            }

            // chipNone 선택 시
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
        // userModel.getPreferredRecipes()가 null이면 chipNone이 선택된 상태
        if (userModel.getPreferredRecipes() == null) {
            listener?.onChipSelected(null)
        } else {
            listener?.onChipSelected(userModel.getPreferredRecipes())
        }
    }

}