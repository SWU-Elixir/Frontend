package com.example.elixir

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import com.example.elixir.databinding.FragmentSurvey1Binding
import com.google.android.material.chip.Chip

class Survey1Fragment : Fragment() {
    private lateinit var survey1Binding: FragmentSurvey1Binding
    var listener: OnChipCompletedListener? = null
    private val userModel: UserInfoViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // 바인딩
        survey1Binding = FragmentSurvey1Binding.inflate(inflater, container, false)
        return survey1Binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Fragment나 Activity 내부
        with(survey1Binding) {
            val chipList = listOf(allergyEgg, allergyMilk, allergyBuckwheat, allergyPeanut,
                allergySoybean, allergyWheat, allergyMackerel, allergyCrab, allergyShrimp,
                allergyPig, allergyPeach, allergyTomato, allergyDioxide, allergyWalnut,
                allergyChicken, allergyCow, allergySquid,
                allergySeashell, allergyOyster, allergyPinenut)
            val chipNone = nA // "해당 없음" Chip

            // 일반 Chips 클릭 시 "해당 없음" 해제
            chipList.forEach { chip ->
                chip.setOnCheckedChangeListener { _, isChecked ->
                    if (isChecked) {
                        chipNone.isChecked = false

                    }
                }
            }

            // "해당 없음" 클릭 시 나머지 해제
            chipNone.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    chipList.forEach { it.isChecked = false }
                }
            }
        }
    }
    private fun updateSelection(chipList: List<Chip>, chipNone: Chip) {
        val selectedTexts = mutableListOf<String>()

        chipList.forEach { chip ->
            if (chip.isChecked)
                selectedTexts.add(chip.text.toString())
        }
        if (chipNone.isChecked)
            selectedTexts.add(chipNone.text.toString())

        userModel.setAllergies(selectedTexts)

        // 콜백으로 버튼 활성화 요청
        listener?.onChipSelected(selectedTexts.isNotEmpty())
    }
}