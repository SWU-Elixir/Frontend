package com.example.elixir

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.fragment.app.activityViewModels
import com.example.elixir.databinding.FragmentSignupInfoBinding

class SignupInfoFragment : Fragment() {
    private lateinit var infoBinding: FragmentSignupInfoBinding
    private val viewModel: UserInfoViewModel by activityViewModels()  // 현재 프래그먼트가 속한 액티비티의 뷰 모델
    
    private lateinit var sex: String
    private var birthYear: Int = 0

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
        showYearPicker()

        infoBinding.btnNext.setOnClickListener {
            // 뷰 모델에 저장
            viewModel.setBasicInfo(
                infoBinding.registId.text.toString().trim(),
                infoBinding.registPw.text.toString().trim(),
                infoBinding.registNick.text.toString().trim(),
                infoBinding.registProfile.toString().trim(),
                "기타",
                birthYear
            )
        }
    }

    // 출생년도 스피너 드롭다운 메뉴 보여주기
    private fun showYearPicker() {
        val spinner: Spinner = infoBinding.birthYear

        // 생년 리스트 생성(1900 ~ 2025)
        val birthYears = (1900..2025).map { it.toString() }

        // 어댑터 설정
        val adapter = ArrayAdapter(requireContext(), R.layout.item_spinner_year, birthYears)
        spinner.adapter = adapter

        // 선택 리스너 (선택 시 처리하고 싶을 때)
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                val selectedYear = parent.getItemAtPosition(position).toString()
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }
}