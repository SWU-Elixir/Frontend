package com.example.elixir.signup

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.elixir.databinding.FragmentSurvey3Binding

class Survey3Fragment : Fragment() {
    private lateinit var survey3Binding: FragmentSurvey3Binding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // 바인딩
        survey3Binding = FragmentSurvey3Binding.inflate(inflater, container, false)
        return survey3Binding.root
    }
}