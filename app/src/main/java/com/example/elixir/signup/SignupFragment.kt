package com.example.elixir.signup

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.elixir.R
import com.example.elixir.databinding.FragmentSignupBinding

class SignupFragment : Fragment() {
    private lateinit var signupBinding: FragmentSignupBinding
    private val userModel: UserInfoViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // 뷰 바인딩 초기화
        signupBinding = FragmentSignupBinding.inflate(inflater, container, false)
        return signupBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(signupBinding) {
            // 상단 단계 인디케이터 고정, 현재 단계 프래그먼트
            setStepFragment(SurveyIndicatorFragment())
            setSurveyStepFragment(userModel.currentStep)

            // 처음엔 다음 버튼 비활성화
            btnNext.isEnabled = false

            // 이전 버튼 클릭 이벤트 처리
            btnPrev.setOnClickListener {
                if (userModel.currentStep == 0) {
                    // 첫 단계일 경우 계정 생성 화면으로 이동
                    parentFragmentManager.beginTransaction()
                        .replace(R.id.fragment_registration, CreateAccountFragment())
                        .commit()
                } else {
                    // 그 외는 이전 단계로 이동
                    userModel.currentStep -= 1
                    setSurveyStepFragment(userModel.currentStep)
                }
            }

            // 다음 버튼 클릭 이벤트 처리
            btnNext.setOnClickListener {
                if (userModel.currentStep == userModel.maxStep) {
                    // 마지막 단계면 액티비티 종료 (회원가입 완료)
                    Toast.makeText(requireContext(), "회원가입 성공!", Toast.LENGTH_SHORT).show()
                    activity?.finish()
                } else {
                    // 다음 단계로 이동
                    userModel.currentStep += 1
                    setSurveyStepFragment(userModel.currentStep)
                }
            }
        }
    }

    // 상단 인디케이터 프래그먼트 삽입
    private fun setStepFragment(fragment: Fragment) {
        childFragmentManager.beginTransaction()
            .replace(R.id.survey_step, fragment)
            .commit()
    }

    // 인디케이터 프래그먼트의 상태 업데이트
    private fun updateIndicatorFragment() {
        if (userModel.currentStep >= 1) {
            val indicatorFragment = childFragmentManager.findFragmentById(signupBinding.surveyStep.id) as? SurveyIndicatorFragment
            val completedSteps = userModel.completedStep.value
                ?.filterValues { it }           // true인 항목만 필터링
                ?.keys
                ?.map { it - 1 }                // 인디케이터는 0부터 시작하므로 1 감소
                ?.toSet() ?: emptySet()

            indicatorFragment?.updateIndicators(userModel.currentStep - 1, completedSteps)
        }
    }

    // 다음 버튼 상태 설정 함수 (공통)
    private fun setButtonState(enabled: Boolean) {
        signupBinding.btnNext.isEnabled = enabled
        val color = if (enabled) R.color.elixir_orange else R.color.elixir_gray
        signupBinding.btnNext.setBackgroundTintList(ContextCompat.getColorStateList(requireContext(), color))
    }

    // 칩 선택 이벤트 처리 함수 (공통 로직 처리용)
    private fun handleChipSelection(step: Int, selected: Boolean, dataSetter: () -> Unit) {
        dataSetter() // 데이터를 ViewModel에 저장
        userModel.updateCompletedStep(step, selected) // 완료 여부 저장
        setButtonState(selected) // 버튼 상태 설정
        updateIndicatorFragment() // 인디케이터 업데이트
    }

    // 현재 단계에 맞는 설문 프래그먼트 설정
    private fun setSurveyStepFragment(step: Int) {
        // 버튼 텍스트 조정
        signupBinding.btnNext.text =
            if (step == 4) getString(R.string.start) else getString(R.string.next)


        // 프래그먼트 설정 전에 둘 다 잠깐 GONE 시키기 (깜빡임 방지)
        signupBinding.surveyStep.visibility = View.GONE
        signupBinding.signupContent.visibility = View.GONE
        signupBinding.btnNext.visibility = View.GONE
        signupBinding.btnPrev.visibility = View.GONE

        // 0.2초 후에 동시에 표시 (동기화된 것처럼 보이도록)
        Handler(Looper.getMainLooper()).postDelayed({
            signupBinding.surveyStep.visibility = if (step == 0) View.GONE else View.VISIBLE
            signupBinding.signupContent.visibility = View.VISIBLE
            signupBinding.btnNext.visibility = View.VISIBLE
            signupBinding.btnPrev.visibility = View.VISIBLE
        }, 200)

        // 인디케이터 업데이트 (step >= 1일 때만)
        if (step >= 1) updateIndicatorFragment()

        // 단계별 프래그먼트 설정
        val fragment = when (step) {
            0 -> SettingProfileFragment().apply {
                listener = object : OnProfileCompletedListener {
                    override fun onProfileCompleted(profileImage: String, nickname: String, gender: String, birthYear: Int) {
                        userModel.setProfile(profileImage, nickname, gender, birthYear)
                        handleChipSelection(0, true) {}
                    }

                    override fun onProfileInvalid() {
                        handleChipSelection(0, false) {}
                    }
                }
            }

            1 -> Survey1Fragment().apply {
                listener = object : OnChipCompletedListener {
                    override fun onChipSelected(chips: List<String>) {
                        handleChipSelection(1, true) { userModel.setAllergies(chips) }
                    }

                    override fun onChipSelectedNot() {
                        handleChipSelection(1, false) { userModel.setAllergies(emptyList()) }
                    }
                }
            }

            2 -> Survey2Fragment().apply {
                listener = object : OnChipCompletedListener {
                    override fun onChipSelected(chips: List<String>) {
                        handleChipSelection(2, true) { userModel.setPreferredDiets(chips) }
                    }

                    override fun onChipSelectedNot() {
                        handleChipSelection(2, false) { userModel.setPreferredDiets(emptyList()) }
                    }
                }
            }

            3 -> Survey3Fragment().apply {
                listener = object : OnChipCompletedListener {
                    override fun onChipSelected(chips: List<String>) {
                        handleChipSelection(3, true) { userModel.setPreferredRecipes(chips) }
                    }

                    override fun onChipSelectedNot() {
                        handleChipSelection(3, false) { userModel.setPreferredRecipes(emptyList()) }
                    }
                }
            }

            4 -> Survey4Fragment().apply {
                listener = object : OnChipCompletedListener {
                    override fun onChipSelected(chips: List<String>) {
                        handleChipSelection(4, true) { userModel.setSignupReason(chips) }
                    }

                    override fun onChipSelectedNot() {
                        handleChipSelection(4, false) { userModel.setSignupReason(emptyList()) }
                    }
                }
            }

            else -> return
        }

        // 프래그먼트 삽입
        childFragmentManager.beginTransaction()
            .replace(R.id.signup_content, fragment)
            .commit()

        // 버튼 상태 업데이트 (현재 단계의 완료 여부 기준으로)
        setButtonState(userModel.completedStep.value?.get(step) == true)
    }
}
