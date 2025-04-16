package com.example.elixir.signup

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
        // 바인딩 정의
        signupBinding = FragmentSignupBinding.inflate(inflater, container, false)
        return signupBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 상단 인디케이터 고정
        setSurveyFragment(SurveyIndicatorFragment())
        signupBinding.btnNext.isEnabled = false
        // 처음: 프로필 설정 로딩
        setSurveyStepFragment(userModel.currentStep)

        // 이전 버튼 클릭 시
        signupBinding.btnPrev.setOnClickListener {
            // 프로필 설정 페이지면 계정 생성 페이지로
            if (userModel.currentStep == 0) {
                parentFragmentManager.beginTransaction()
                    .replace(R.id.fragment_registration, CreateAccountFragment())
                    .commit()
            }
            // 아니면 설문 이전 단계로
            else {
                userModel.currentStep -= 1
                setSurveyStepFragment(userModel.currentStep)
            }
        }

        // 만약 정보 입력을 마친 상태면 다음 버튼 활성화
        signupBinding.btnNext.setBackgroundTintList(
            ContextCompat.getColorStateList(
                requireContext(),
                if (userModel.isStepCompleted())
                    R.color.elixir_orange
                else
                    R.color.elixir_gray
            )
        )

        // 다음 버튼 클릭 시
        signupBinding.btnNext.setOnClickListener {
            // 최대 단계 누르면 로그인 액티비티로
            if (userModel.currentStep == userModel.maxStep) {
                // 마지막 단계면 로그인 액티비티로 이동
                activity?.finish()
            }
            // 아니면 설문 다음 단계로
            else {
                userModel.currentStep += 1
                setSurveyStepFragment(userModel.currentStep)
            }
        }
    }

    private fun setSurveyFragment(fragment: Fragment) {
        childFragmentManager.beginTransaction()
            .replace(R.id.survey_step, fragment)
            .commit()
    }

    private fun setSurveyStepFragment(step: Int) {
        val fragment = when (step) {
            0 -> SettingProfileFragment().apply {
                listener = object : OnProfileCompletedListener {
                    override fun onProfileCompleted(img: String, nick: String, sex: String, birthYear: Int) {
                        userModel.setProfile(img, nick, sex, birthYear)
                        userModel.updateCompletedStep(step, true)
                        signupBinding.btnNext.isEnabled = true
                    }
                    override fun onProfileInvalid() {
                        userModel.updateCompletedStep(step, false)
                        signupBinding.btnNext.isEnabled = false
                    }
                }
                signupBinding.surveyStep.visibility = View.GONE
            }
            1 -> Survey1Fragment().apply {
                listener = object : OnChipCompletedListener {
                    override fun onChipSelected(isValid: Boolean) {
                        signupBinding.btnNext.isEnabled = true
                    }

                    override fun onChipSelectedNot(isValid: Boolean) {
                        signupBinding.btnNext.isEnabled = false
                    }
                }
            }
            2 -> Survey2Fragment()
            3 -> Survey3Fragment()
            4 -> Survey4Fragment()
            else -> return
        }

        childFragmentManager.beginTransaction()
            .replace(R.id.signup_content, fragment)
            .commit()
    }
}