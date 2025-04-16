package com.example.elixir.signup

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.elixir.databinding.FragmentSurvey2Binding

class Survey2Fragment : Fragment() {
    private lateinit var survey2Binding: FragmentSurvey2Binding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // 바인딩
        survey2Binding = FragmentSurvey2Binding.inflate(inflater, container, false)
        return survey2Binding.root
    }
}