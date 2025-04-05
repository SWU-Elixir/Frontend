package com.example.elixir

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.elixir.databinding.FragmentSurvey1Binding

class Survey1Fragment : Fragment() {
    private lateinit var survey1Binding: FragmentSurvey1Binding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // 바인딩
        survey1Binding = FragmentSurvey1Binding.inflate(inflater, container, false)
        return survey1Binding.root
    }
}