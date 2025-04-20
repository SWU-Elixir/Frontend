package com.example.elixir.signup

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.elixir.R
import com.example.elixir.databinding.FragmentSurvey2Binding

class Survey2Fragment : Fragment() {
    private lateinit var survey2Binding: FragmentSurvey2Binding
    private lateinit var preferredDiet: String
    var listener: OnDietCompletedListener? = null

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
        preferredDiet = ""

        // 식단 선택
        survey2Binding.selectDiet.setOnCheckedChangeListener { _, checkedId ->
            when(checkedId) {
                R.id.diet_meat -> preferredDiet = R.string.diet_meat.toString()
                R.id.diet_vegetable -> preferredDiet = R.string.diet_vegetable.toString()
                R.id.diet_meat_vegetable -> preferredDiet = R.string.diet_meat_vegetable.toString()
                R.id.diet_n_a -> preferredDiet = R.string.diet_noting.toString()
            }
            checkDietValid()
        }
    }

    private fun checkDietValid() {
        if (preferredDiet.isNotBlank()) {
            listener?.onDietSelected(preferredDiet)
        } else {
            listener?.onDietSelectedNot()
        }
    }
}