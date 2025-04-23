package com.example.elixir.signup

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.elixir.R
import com.example.elixir.databinding.FragmentSurveyIndicatorBinding

class SurveyIndicatorFragment : Fragment() {
    private lateinit var indicatorBinding: FragmentSurveyIndicatorBinding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // 바인딩
        indicatorBinding = FragmentSurveyIndicatorBinding.inflate(inflater, container, false)
        return indicatorBinding.root
    }

    fun updateIndicators(currentStep: Int, completedSteps: Set<Int>) {
        val stepIcons = listOf(
            indicatorBinding.step1,
            indicatorBinding.step2,
            indicatorBinding.step3,
            indicatorBinding.step4
        )

        val selectedDrawables = listOf(
            R.drawable.indicator_1_selected,
            R.drawable.indicator_2_selected,
            R.drawable.indicator_3_selected,
            R.drawable.indicator_4_selected
        )

        val unselectedDrawables = listOf(
            R.drawable.indicator_2_unselected,
            R.drawable.indicator_3_unselected,
            R.drawable.indicator_4_unselected
        )

        for (i in stepIcons.indices) {
            val imageView = stepIcons[i]

            when {
                i == currentStep -> {
                    imageView.setImageResource(selectedDrawables[i])
                }
                completedSteps.contains(i) -> {
                    imageView.setImageResource(R.drawable.indicator_checked)
                }
                else -> {
                    imageView.setImageResource(unselectedDrawables[i-1])
                }
            }
        }
    }
}