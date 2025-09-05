package com.example.elixir.signup

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.elixir.R
import com.example.elixir.databinding.FragmentSurvey4Binding

class Survey4Fragment : Fragment() {
    private lateinit var survey4Binding: FragmentSurvey4Binding         // 바인딩 객체
    private val userModel: UserInfoViewModel by activityViewModels()    // 뷰 모델 연결
    private var selectedReasons = mutableListOf<String>()               // 가입 이유 리스트
    var listener: OnChipCompletedListener? = null                       // 칩 선택 여부를 알려줄 인터페이스
    private val chipNoneLabel = "특별한 이유 없음"
    private lateinit var reasonMap: Map<String, View>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // 바인딩 정의
        survey4Binding = FragmentSurvey4Binding.inflate(inflater, container, false)
        return survey4Binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 칩과 서버 전송 텍스트(값) 묶기
        reasonMap = mapOf(
            "항산화강화" to survey4Binding.reason1,
            "혈당조절" to survey4Binding.reason2,
            "염증감소" to survey4Binding.reason3,
            chipNoneLabel to survey4Binding.reason4
        )

        // 뷰 모델에 저장된 가입 이유 정보 가져오기
        val savedReasons = userModel.getSignupReason()

        // 뷰 모델에 저장된 가입 이유가 없을 경우 "특별한 이유 없음"을 선택함
        selectedReasons = if (savedReasons.isNullOrEmpty()) {
            mutableListOf(chipNoneLabel)
        } else {
            savedReasons.toMutableList()
        }

        // ViewModel 업데이트 (처음 진입 시 chipNone 선택 반영)
        if (savedReasons.isNullOrEmpty()) {
            userModel.setSignupReason(null)
        }

        updateAllButtonStates()
        updateSelection() // ✅ 초기 상태도 버튼 활성화 상태 반영

        reasonMap.forEach { (label, view) ->
            view.setOnClickListener {
                if (label == chipNoneLabel) {
                    // "특별한 이유 없음" 클릭 시 모두 해제 후 자신만 선택
                    selectedReasons.clear()
                    selectedReasons.add(chipNoneLabel)
                    userModel.setSignupReason(null)
                } else {
                    if (selectedReasons.contains(label)) {
                        selectedReasons.remove(label)
                    } else {
                        selectedReasons.add(label)
                    }
                    // chipNone 해제
                    selectedReasons.remove(chipNoneLabel)

                    // 선택 항목이 비었으면 chipNone 자동 선택
                    if (selectedReasons.isEmpty()) {
                        selectedReasons.add(chipNoneLabel)
                        userModel.setSignupReason(null)
                    } else {
                        userModel.setSignupReason(selectedReasons)
                    }
                }

                updateAllButtonStates()
                updateSelection()
            }
        }
    }

    private fun updateAllButtonStates() {
        reasonMap.forEach { (label, view) ->
            val isSelected = selectedReasons.contains(label)
            view.isSelected = isSelected
            updateButtonStyle(view, isSelected)
        }
    }

    private fun updateButtonStyle(view: View, isSelected: Boolean) {
        view.setBackgroundResource(
            if (isSelected) R.drawable.bg_rect_filled_orange
            else R.drawable.bg_rect_outline_gray
        )
    }

    private fun updateSelection() {
        listener?.onChipSelected(userModel.getSignupReason())
    }
}
