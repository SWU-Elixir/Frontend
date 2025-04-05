package com.example.elixir

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import com.example.elixir.databinding.FragmentSignupInfoBinding
import java.util.Calendar

class SignupInfoFragment : Fragment() {
    private lateinit var infoBinding: FragmentSignupInfoBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // 바인딩
        infoBinding = FragmentSignupInfoBinding.inflate(inflater, container, false)
        return infoBinding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupYearDropdown()
    }

    private fun setupYearDropdown() {
        val currentYear = Calendar.getInstance().get(Calendar.YEAR)
        val yearList = (1900..currentYear).toList().reversed().map { it.toString() }

        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_dropdown_item_1line,
            yearList
        )

        infoBinding.autoCompleteYear.setAdapter(adapter)

        infoBinding.autoCompleteYear.setOnItemClickListener { _, _, position, _ ->
            val selectedYear = adapter.getItem(position)
            Toast.makeText(requireContext(), "선택된 출생년도: $selectedYear", Toast.LENGTH_SHORT).show()
        }
    }
}