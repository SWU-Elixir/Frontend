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
    private lateinit var survey4Binding: FragmentSurvey4Binding
    private val userModel: UserInfoViewModel by activityViewModels()
    private var selectedReasons = mutableListOf<String>()
    private lateinit var reasonMap: Map<String, View>
    private val chipNoneLabel = "특별한 이유 없음" // 또는 getString(R.string.title_no_recommend)
    var listener: OnChipCompletedListener? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        survey4Binding = FragmentSurvey4Binding.inflate(inflater, container, false)
        return survey4Binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // reasonMap 초기화
        reasonMap = mapOf(
            "항산화강화" to survey4Binding.reason1,
            "혈당조절" to survey4Binding.reason2,
            "염증감소" to survey4Binding.reason3,
            chipNoneLabel to survey4Binding.reason4 // "특별한 이유 없음"
        )

        // 최초에 "특별한 이유 없음"을 선택된 상태로
        selectedReasons.add(chipNoneLabel)
        userModel.setSignupReason(null)
        updateAllButtonStates()

        // 뷰 모델 값 불러와서 선택 상태 복원
        userModel.getSignupReason()?.let { savedReasons ->
            selectedReasons.clear()
            savedReasons.forEach { item ->
                if (item != chipNoneLabel) { // "특별한 이유 없음"이 아니면 추가
                    selectedReasons.add(item)
                }
            }
            updateAllButtonStates()
        }

        // 클릭 리스너 등록
        reasonMap.forEach { (label, view) ->
            view.setOnClickListener {
                if (label == chipNoneLabel) {
                    // "특별한 이유 없음"을 누르면 리스트를 비우고, userModel에 null로 저장
                    selectedReasons.clear()
                    selectedReasons.add(chipNoneLabel)
                    userModel.setSignupReason(null)
                } else {
                    // "특별한 이유 없음"이 선택되어 있다면 해제
                    selectedReasons.remove(chipNoneLabel)
                    // 토글 동작: 이미 선택되어 있으면 제거, 아니면 추가
                    if (selectedReasons.contains(label)) {
                        selectedReasons.remove(label)
                    } else {
                        selectedReasons.add(label)
                    }
                    // 모든 항목이 해제되면 "특별한 이유 없음"을 자동 선택
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

    // 모든 버튼의 선택 상태와 스타일을 업데이트
    private fun updateAllButtonStates() {
        reasonMap.forEach { (label, view) ->
            val isSelected = selectedReasons.contains(label)
            view.isSelected = isSelected
            updateButtonStyle(view, isSelected)
        }
    }

    private fun updateButtonStyle(view: View, isSelected: Boolean) {
        if (isSelected) {
            view.setBackgroundResource(R.drawable.bg_rect_filled_orange)
        } else {
            view.setBackgroundResource(R.drawable.bg_rect_outline_gray)
        }
    }

    private fun updateSelection() {
        if (userModel.getSignupReason() == null) {
            listener?.onChipSelected(null)
        } else {
            listener?.onChipSelected(userModel.getSignupReason())
        }
    }
}
