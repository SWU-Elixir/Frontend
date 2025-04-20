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
    private var profileData: ProfileData? = null            // 프로필 데이터
    private var selectedAllergies: List<String>? = null     // 알러지 배열
    private var preferredDiet: String? = null               // 선호 식단
    private var preferredRecipe: List<String>? = null       // 선호 레시피

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
        with(signupBinding) {

            // 상단 인디케이터 고정
            setStepFragment(SurveyIndicatorFragment())
            btnNext.isEnabled = false
            // 처음: 프로필 설정 로딩
            setSurveyStepFragment(userModel.currentStep)

            // 이전 버튼 클릭 시
            btnPrev.setOnClickListener {
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

            // 다음 버튼 클릭 시
            btnNext.setOnClickListener {
                // 최대 단계 누르면 로그인 액티비티로
                if (userModel.currentStep == userModel.maxStep) {
                    // 마지막 단계면 로그인 액티비티로 이동
                    activity?.finish()
                }
                else {
                    // userModel의 프로필에 저장, 완료 스테이지 추가
                    when(userModel.currentStep) {
                        0 -> profileData?.let {
                            userModel.setProfile(it.profileImage, it.nickname, it.gender, it.birthYear)
                            userModel.updateCompletedStep(0, true)
                        }
                        1 -> selectedAllergies?.let {
                            userModel.setAllergies(it)
                            userModel.updateCompletedStep(1, true)
                        }
                        2 -> preferredDiet?.let {
                            userModel.setPreferredDiets(it)
                        }
                    }
                    // 설문 다음 단계로
                    userModel.currentStep += 1
                    setSurveyStepFragment(userModel.currentStep)
                }
            }
        }
    }
    // 설문 프래그먼트 설정
    private fun setStepFragment(fragment: Fragment) {
        childFragmentManager.beginTransaction()
            .replace(R.id.survey_step, fragment)
            .commit()
    }
    // 설문 프래그먼트 설정
    private fun setSurveyStepFragment(step: Int) {
        val fragment = when (step) {
            // 프로필 입력 프래그먼트, 칩 선택 시 다음 버튼 활성화
            0 -> SettingProfileFragment().apply {
                listener = object : OnProfileCompletedListener {
                    // 유효한 값이 입력되었다면 활성화
                    override fun onProfileCompleted(profileImage: String, nickname: String, gender: String, birthYear: Int) {
                        userModel.setProfile(profileImage, nickname, gender, birthYear)
                        // 버튼 활성화
                        with(signupBinding) {
                            btnNext.isEnabled = true
                            btnNext.setBackgroundTintList(
                                ContextCompat.getColorStateList(requireContext(), R.color.elixir_orange))
                        }
                    }
                    // 유효한 값이 입력되지 않았다면 활성화 x
                    override fun onProfileInvalid() {
                        // 버튼 비활성화
                        with(signupBinding) {
                            btnNext.isEnabled = false
                            btnNext.setBackgroundTintList(
                                ContextCompat.getColorStateList(requireContext(), R.color.elixir_gray))
                        }
                    }
                }
                // 인디케이터 숨기기
                signupBinding.surveyStep.visibility = View.GONE
            }
            // 알레르기 입력 프래그먼트, 칩 선택 시 다음 버튼 활성화
            1 -> Survey1Fragment().apply {
                listener = object : OnChipCompletedListener {
                    // 칩이 선택되었다면, 다음 버튼 활성화 및 데이터 저장
                    override fun onChipSelected(chips: List<String>) {
                        selectedAllergies = chips
                        // 버튼 활성화
                        with(signupBinding) {
                            btnNext.isEnabled = true
                            btnNext.setBackgroundTintList(
                                ContextCompat.getColorStateList(requireContext(), R.color.elixir_orange))
                        }
                    }
                    // 칩이 선택되지 않았다면, 다음 버튼 비활성화 및 데이터 NULL
                    override fun onChipSelectedNot() {
                        selectedAllergies = null
                        // 버튼 비활성화
                        with(signupBinding) {
                            btnNext.isEnabled = false
                            btnNext.setBackgroundTintList(
                                ContextCompat.getColorStateList(requireContext(), R.color.elixir_gray))
                        }
                    }
                }
            }
            // 식단 입력 프래그먼트, 식단 선택 시 다음 버튼 활성화
            2 -> Survey2Fragment().apply {
                listener = object  : OnDietCompletedListener {
                    // 식단이 입력되었다면, 다음 버튼 활성화 및 데이터 저장
                    override fun onDietSelected(diet: String) {
                        preferredDiet = diet
                        // 버튼 활성화
                        with(signupBinding) {
                            btnNext.isEnabled = true
                            btnNext.setBackgroundTintList(
                                ContextCompat.getColorStateList(requireContext(), R.color.elixir_orange))
                        }
                    }
                    // 식단이 선택되지 않았다면, 다음 버튼 비활성화 및 데이터 NULL
                    override fun onDietSelectedNot() {
                        preferredDiet = null
                        // 버튼 비활성화
                        with(signupBinding) {
                            btnNext.isEnabled = false
                            btnNext.setBackgroundTintList(
                                ContextCompat.getColorStateList(requireContext(), R.color.elixir_gray))
                        }
                    }
                }
            }
            3 -> Survey3Fragment().apply {
                listener = object : OnChipCompletedListener {
                    // 칩이 선택되었다면, 다음 버튼 활성화 및 데이터 저장
                    override fun onChipSelected(chips: List<String>) {
                        preferredRecipe = chips
                        // 버튼 활성화
                        with(signupBinding) {
                            btnNext.isEnabled = true
                            btnNext.setBackgroundTintList(
                                ContextCompat.getColorStateList(requireContext(), R.color.elixir_orange))
                        }
                    }
                    // 칩이 선택되지 않았다면, 다음 버튼 비활성화 및 데이터 NULL
                    override fun onChipSelectedNot() {
                        preferredRecipe = null
                        // 버튼 비활성화
                        with(signupBinding) {
                            btnNext.isEnabled = false
                            btnNext.setBackgroundTintList(
                                ContextCompat.getColorStateList(requireContext(), R.color.elixir_gray))
                        }
                    }
                }
            }
            4 -> Survey4Fragment()
            else -> return
        }

        childFragmentManager.beginTransaction()
            .replace(R.id.signup_content, fragment)
            .commit()
    }
}