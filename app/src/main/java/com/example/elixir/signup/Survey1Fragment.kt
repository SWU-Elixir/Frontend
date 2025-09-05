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
    private var allergies: MutableList<String>? = null                  // 알러지 리스트
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
        allergies = mutableListOf()
        with(survey1Binding) {
            // 일반 칩 리스트
            val chipList = listOf(
                allergyEgg, allergyMilk, allergyBuckwheat, allergyPeanut,
                allergySoybean, allergyWheat, allergyMackerel, allergyCrab, allergyShrimp,
                allergyPig, allergyPeach, allergyTomato, allergyDioxide, allergyWalnut,
                allergyChicken, allergyCow, allergySquid, allergySeashell, allergyOyster,
                allergyPinenut
            )
            val chipNone = nA                                           // 해당없음 칩

            val savedAllergies = userModel.getAllergies()               // 뷰 모델에 저장된 알러지 정보 가져오기

            // 뷰 모델에 저장된 알러지 정보가 있다면 ui에 반영, 없다면 해당없음
            if (!savedAllergies.isNullOrEmpty()) {
                allergies?.clear()
                allergies?.addAll(savedAllergies)
                chipList.forEach { chip ->
                    chip.isChecked = allergies?.contains(chip.text.toString()) == true
                }
                chipNone.isChecked = false

                listener?.onChipSelected(savedAllergies)
            } else {
                allergies?.clear()
                chipList.forEach { chip ->
                    chip.isChecked = false
                }
                chipNone.isChecked = true

                listener?.onChipSelected(savedAllergies)
            }

            chipList.forEach { chip ->
                chip.setOnCheckedChangeListener { _, isChecked ->
                    if (isChecked) {
                        // 일반 칩이 선택되면, 알러지 정보를 저장하고 해당없음 칩 선택 해제
                        chipNone.isChecked = false

                        // allergyOyster 선택 시 '굴'
                        val allergyName = when (chip) {
                            allergyOyster -> "굴"
                            else -> chip.text.toString()
                        }

                        if (allergies?.contains(allergyName) == false)
                            allergies?.add(allergyName)

                    } else {
                        // allergyOyster 칩이면 '굴'
                        val allergyName = when (chip) {
                            allergyOyster -> "굴"
                            else -> chip.text.toString()
                        }
                        allergies?.remove(allergyName)

                        val anyChecked = chipList.any { it.isChecked }
                        if (!anyChecked) {
                            chipNone.isChecked = true
                            userModel.setAllergies(null)
                        }
                    }
                    // 일반 칩 선택 시 정보 저장
                    if (!chipNone.isChecked) {
                        userModel.setAllergies(allergies)
                    }
                    updateSelection()
                }
            }

            // 해당없음 선택 시 null로 저장
            chipNone.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    chipList.forEach { it.isChecked = false }
                    allergies?.clear()
                    userModel.setAllergies(null)
                }
                updateSelection()
            }
        }
    }

    private fun updateSelection() {
        if (userModel.getAllergies() == null) {
            listener?.onChipSelected(null)
        } else {
            listener?.onChipSelected(userModel.getAllergies())
        }
    }
}