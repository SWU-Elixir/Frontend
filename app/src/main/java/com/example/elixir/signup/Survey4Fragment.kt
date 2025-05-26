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
    private var selectedReasons = mutableListOf<String>()                // 이유 선택
    private lateinit var reasonMap: Map<String, View>
    var listener: OnChipCompletedListener? = null               // 칩 선택 여부를 알려줄 인터페이스

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // 바인딩
        survey4Binding = FragmentSurvey4Binding.inflate(inflater, container, false)
        return survey4Binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 문자열 <-> 뷰 매핑 (String 리소스를 문자열로 변환)
        reasonMap = mapOf(
            getString(R.string.title_antiox) to survey4Binding.reason1,
            getString(R.string.title_blood) to survey4Binding.reason2,
            getString(R.string.title_inflam) to survey4Binding.reason3,
            getString(R.string.title_no_recommend) to survey4Binding.reason4,
        )
        // 초기 값 설정
        setReasons()

        // 클릭 리스너 등록
        reasonMap.forEach { (label, view) ->
            view.setOnClickListener {
                if (label == getString(R.string.title_no_recommend)) {
                    // 다른 모든 선택 해제 후 "특별한 제한 없음"만 추가
                    selectedReasons.clear()
                    selectedReasons.add(label)
                } else {
                    // "특별한 제한 없음"이 선택되어 있다면 해제
                    selectedReasons.remove(getString(R.string.title_no_recommend))

                    // 토글 동작: 이미 선택되어 있으면 제거, 아니면 추가
                    if (selectedReasons.contains(label)) {
                        selectedReasons.remove(label)
                    } else {
                        selectedReasons.add(label)
                    }
                }
                reasonMap.forEach { (label, view) ->
                    view.isSelected = selectedReasons.contains(label)
                    updateButtonStyle(view, selectedReasons.contains(label))
                }
                // 상태 갱신
                updateSelection()
            }
        }
        // 상태 갱신
        updateSelection()
    }

    // 선택된 버튼 스타일 업데이트
    private fun updateButtonStyle(view: View, isSelected: Boolean) {
        if (isSelected) {
            // 선택된 상태에 맞는 스타일을 설정 (배경, 색상 등)
            view.setBackgroundResource(R.drawable.bg_rect_filled_orange)
        } else {
            // 선택되지 않은 상태에 맞는 스타일을 설정
            view.setBackgroundResource(R.drawable.bg_rect_outline_gray)
        }
    }

    // 선택 이유 불러오기
    private fun setReasons() {
        userModel.getSignupReason()?.let { savedReasons ->
            selectedReasons.clear()
            selectedReasons.addAll(savedReasons)

            // 버튼 반영
            reasonMap.forEach { (label, view) ->
                // view의 선택 상태를 반영
                view.isSelected = selectedReasons.contains(label)
                // 선택된 상태에 맞는 배경, 글자
                updateButtonStyle(view, selectedReasons.contains(label))
            }
        }
    }

    // 작업 상태 갱신 & 값 저장 함수
    private fun updateSelection() {
        // 리스트에 정보가 저장되어 있는 상태라면 선택한 레시피들을 뷰 모델에 저장
        // 공백이 아니라면 선택된 상태 -> 다음 버튼 활성화
        if (selectedReasons.isNotEmpty()) {
            userModel.setSignupReason(selectedReasons)
            listener?.onChipSelected(selectedReasons)
        }
        // 저장되어 있지 않다면 선택되지 않은 상태 -> 버튼 비활성화
        else listener?.onChipSelectedNot()
    }
}