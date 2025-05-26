package com.example.elixir.signup

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.elixir.databinding.FragmentSurvey1Binding

class Survey1Fragment : Fragment() {
    private lateinit var survey1Binding: FragmentSurvey1Binding         // 바인딩 객체
    private val userModel: UserInfoViewModel by activityViewModels()    // 뷰 모델 연결
    private var allergies = mutableListOf<String>()                     // 알러지 리스트
    var listener: OnChipCompletedListener? = null                       // 칩 선택 여부를 알려줄 인터페이스

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // 바인딩 정의
        survey1Binding = FragmentSurvey1Binding.inflate(inflater, container, false)
        return survey1Binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(survey1Binding) {
            // 일반 칩 리스트
            val chipList = listOf(
                allergyEgg, allergyMilk, allergyBuckwheat, allergyPeanut,
                allergySoybean, allergyWheat, allergyMackerel, allergyCrab, allergyShrimp,
                allergyPig, allergyPeach, allergyTomato, allergyDioxide, allergyWalnut,
                allergyChicken, allergyCow, allergySquid, allergySeashell, allergyOyster,
                allergyPinenut)

            // 해당 없음 칩
            val chipNone = nA

            // 뷰 모델에 저장된 값이 잆다면(Null이 아니라면) 불러와 선택 상태 복원
            userModel.getAllergies()?.let { savedAllergies ->
                // 쓰레기 값 우려로 리스트를 아예 비워주고 뷰 모델에서 불러오기
                allergies.clear()
                allergies.addAll(savedAllergies)

                // 일반 칩 체크 상태 복원
                chipList.forEach { chip ->
                    chip.isChecked = allergies.contains(chip.text.toString())
                }
                // 해당 없음 칩 체크 상태 복원
                chipNone.isChecked = allergies.contains(chipNone.text.toString())
            }

            // 일반 칩 선택 시
            chipList.forEach { chip ->
                chip.setOnCheckedChangeListener { _, isChecked ->
                    // 일반 칩을 선택했다면 해당 없음 칩을 리스트에서 제거
                    if (isChecked) {
                        chipNone.isChecked = false
                        allergies.remove(chipNone.text.toString())

                        // 중복 저장 방지
                        if (!allergies.contains(chip.text.toString()))
                            allergies.add(chip.text.toString())
                    }
                    // 두번 클릭 시 리스트에서 제거
                    else allergies.remove(chip.text.toString())

                    // 상태 갱신
                    updateSelection()
                }
            }

            // 특별히 없음 선택 시
            chipNone.setOnCheckedChangeListener { _, isChecked ->
                // 일반 칩 모두 선택 해제, 리스트에서 제거하고 특별히 없음을 저장
                if (isChecked) {
                    chipList.forEach { it.isChecked = false }
                    allergies.clear()
                    allergies.add(chipNone.text.toString())
                }
                // 두 번 클릭 시 리스트에서 제거
                else allergies.remove(chipNone.text.toString())

                // 상태 갱신
                updateSelection()
            }
        }
    }

    // 작업 상태 갱신 & 값 저장 함수
    private fun updateSelection() {
        // 리스트에 정보가 저장되어 있는 상태라면 선택한 알러지들을 뷰 모델에 저장
        // 공백이 아니라면 칩이 선택된 상태 -> 다음 버튼 활성화
        if (allergies.isNotEmpty()) {
            userModel.setAllergies(allergies)
            listener?.onChipSelected(allergies)
        }
        // 저장되어 있지 않다면 칩이 선택되지 않은 상태 -> 버튼 비활성화
        else listener?.onChipSelectedNot()
    }
}