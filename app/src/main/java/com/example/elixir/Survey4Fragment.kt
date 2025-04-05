package com.example.elixir

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.elixir.databinding.FragmentSurvey4Binding

class Survey4Fragment : Fragment() {
    private lateinit var survey4Binding: FragmentSurvey4Binding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // 바인딩
        survey4Binding = FragmentSurvey4Binding.inflate(inflater, container, false)
        return survey4Binding.root
    }
}