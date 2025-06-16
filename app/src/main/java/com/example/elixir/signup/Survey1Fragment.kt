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
    private var allergies: MutableList<String>? = null                                        // 알러지 리스트
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
        allergies = mutableListOf() // 여기서 초기화
        with(survey1Binding) {
            val chipList = listOf(
                allergyEgg, allergyMilk, allergyBuckwheat, allergyPeanut,
                allergySoybean, allergyWheat, allergyMackerel, allergyCrab, allergyShrimp,
                allergyPig, allergyPeach, allergyTomato, allergyDioxide, allergyWalnut,
                allergyChicken, allergyCow, allergySquid, allergySeashell, allergyOyster,
                allergyPinenut)
            val chipNone = nA

            chipNone.isChecked = true

            userModel.getAllergies()?.let { savedAllergies ->
                allergies?.clear()
                allergies?.addAll(savedAllergies)
                chipList.forEach { chip ->
                    chip.isChecked = allergies?.contains(chip.text.toString()) == true
                }
                chipNone.isChecked = allergies?.contains(chipNone.text.toString()) == true
                updateSelection()
            }

            chipList.forEach { chip ->
                chip.setOnCheckedChangeListener { _, isChecked ->
                    if (isChecked) {
                        chipNone.isChecked = false
                        allergies?.add(chip.text.toString())
                    } else {
                        allergies?.remove(chip.text.toString())
                        if(allergies.isNullOrEmpty()) {
                            userModel.setAllergies(null)
                            chipNone.isChecked = true
                        }
                    }
                    userModel.setAllergies(allergies) // 리스트가 비어있어도 저장
                    updateSelection()
                }
            }

            // 해당 없음 선택
            chipNone.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    chipList.forEach { it.isChecked = false }
                    allergies?.clear()
                    userModel.setAllergies(null) // chipNone 선택 시 null로 저장
                }
                updateSelection()
            }
        }
    }

    private fun updateSelection() {
        // userModel.getAllergies()가 null이면 chipNone이 선택, 아님 일반 칩이 선택된 상태
        if (userModel.getAllergies() == null) {
            listener?.onChipSelected(null)
        } else {
            listener?.onChipSelected(userModel.getAllergies())
        }
    }
}