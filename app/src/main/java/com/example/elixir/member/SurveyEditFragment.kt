package com.example.elixir.member

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.example.elixir.R
import com.example.elixir.RetrofitClient
import com.example.elixir.databinding.FragmentSignupBinding
import com.example.elixir.signup.Survey1Fragment
import com.example.elixir.signup.Survey2Fragment
import com.example.elixir.signup.Survey3Fragment
import com.example.elixir.signup.Survey4Fragment
import com.example.elixir.signup.SurveyIndicatorFragment
import com.example.elixir.signup.UserInfoViewModel
import com.example.elixir.signup.OnChipCompletedListener
import com.google.gson.Gson
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody

class SurveyEditFragment : Fragment() {
    private lateinit var binding: FragmentSignupBinding
    private val userModel: UserInfoViewModel by activityViewModels()
    private val maxStep = 4

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSignupBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setStepFragment(SurveyIndicatorFragment())

        with(binding) {
            btnNext.isEnabled = false

            btnPrev.setOnClickListener {
                if (userModel.currentStep > 1) {
                    userModel.currentStep -= 1
                    setSurveyStepFragment(userModel.currentStep)
                }
            }

            btnNext.setOnClickListener {
                if (userModel.currentStep == maxStep) {
                    saveSurveyData()
                } else if (userModel.currentStep < maxStep) {
                    userModel.currentStep += 1
                    setSurveyStepFragment(userModel.currentStep)
                }
            }
        }

        // 설문 데이터 로드 후 1단계로 시작
        loadSurveyData()
    }

    private fun loadSurveyData() {
        lifecycleScope.launch {
            try {
                val api = RetrofitClient.instanceMemberApi
                val response = api.getSurvey()
                if (response.status == 200) {
                    val survey = response.data
                    userModel.setAllergies(survey?.allergies ?: emptyList())
                    userModel.setPreferredDiets(survey?.mealStyles ?: emptyList())
                    userModel.setPreferredRecipes(survey?.recipeStyles ?: emptyList())
                    userModel.setSignupReason(survey?.reasons ?: emptyList())
                } else {
                    userModel.setAllergies(emptyList())
                    userModel.setPreferredDiets(emptyList())
                    userModel.setPreferredRecipes(emptyList())
                    userModel.setSignupReason(emptyList())
                }
                userModel.currentStep = 1
                setSurveyStepFragment(userModel.currentStep)
            } catch (e: Exception) {
                userModel.setAllergies(emptyList())
                userModel.setPreferredDiets(emptyList())
                userModel.setPreferredRecipes(emptyList())
                userModel.setSignupReason(emptyList())
                userModel.currentStep = 1
                setSurveyStepFragment(userModel.currentStep)
            }
        }
    }

    private fun setStepFragment(fragment: Fragment) {
        if (userModel.currentStep < 1 || userModel.currentStep > maxStep) return
        childFragmentManager.beginTransaction()
            .replace(binding.surveyStep.id, fragment)
            .commit()
    }

    private fun updateIndicatorFragment(step: Int) {
        val indicatorFragment = childFragmentManager.findFragmentById(binding.surveyStep.id) as? SurveyIndicatorFragment
        val safeStep = (step - 1).coerceAtLeast(0)
        val completedSteps = (1 until step).toSet()
        indicatorFragment?.updateIndicators(safeStep, completedSteps)
    }

    private fun setSurveyStepFragment(step: Int) {
        if (step < 1 || step > maxStep) return
        binding.btnNext.text = if (step == maxStep) getString(R.string.alert_save_title) else getString(R.string.next)

        binding.surveyStep.visibility = View.GONE
        binding.signupContent.visibility = View.GONE
        binding.btnNext.visibility = View.GONE
        binding.btnPrev.visibility = View.GONE

        Handler(Looper.getMainLooper()).postDelayed({
            binding.surveyStep.visibility = View.VISIBLE
            binding.signupContent.visibility = View.VISIBLE
            binding.btnNext.visibility = View.VISIBLE
            binding.btnPrev.visibility = View.VISIBLE
        }, 200)

        updateIndicatorFragment(step)

        val fragment = when (step) {
            1 -> Survey1Fragment().apply {
                listener = object : OnChipCompletedListener {
                    override fun onChipSelected(chips: List<String>?) {
                        userModel.setAllergies(chips)
                        setButtonState(true)
                    }
                    override fun onChipSelectedNot() {
                        userModel.setAllergies(emptyList())
                        setButtonState(false)
                    }
                }
            }
            2 -> Survey2Fragment().apply {
                listener = object : OnChipCompletedListener {
                    override fun onChipSelected(chips: List<String>?) {
                        userModel.setPreferredDiets(chips)
                        setButtonState(true)
                    }
                    override fun onChipSelectedNot() {
                        userModel.setPreferredDiets(emptyList())
                        setButtonState(false)
                    }
                }
            }
            3 -> Survey3Fragment().apply {
                listener = object : OnChipCompletedListener {
                    override fun onChipSelected(chips: List<String>?) {
                        userModel.setPreferredRecipes(chips)
                        setButtonState(true)
                    }
                    override fun onChipSelectedNot() {
                        userModel.setPreferredRecipes(emptyList())
                        setButtonState(false)
                    }
                }
            }
            4 -> Survey4Fragment().apply {
                listener = object : OnChipCompletedListener {
                    override fun onChipSelected(chips: List<String>?) {
                        userModel.setSignupReason(chips)
                        setButtonState(true)
                    }
                    override fun onChipSelectedNot() {
                        userModel.setSignupReason(emptyList())
                        setButtonState(false)
                    }
                }
            }
            else -> return
        }

        childFragmentManager.beginTransaction()
            .replace(binding.signupContent.id, fragment)
            .commit()

        val hasData = when (step) {
            1 -> userModel.getAllergies()?.isNotEmpty() == true
            2 -> userModel.getPreferredDiets()?.isNotEmpty() == true
            3 -> userModel.getPreferredRecipes()?.isNotEmpty() == true
            4 -> userModel.getSignupReason()?.isNotEmpty() == true
            else -> false
        }
        setButtonState(hasData)
    }

    private fun setButtonState(enabled: Boolean) {
        binding.btnNext.isEnabled = enabled
        val color = if (enabled) R.color.elixir_orange else R.color.elixir_gray
        binding.btnNext.setBackgroundTintList(ContextCompat.getColorStateList(requireContext(), color))
    }

    private fun saveSurveyData() {
        lifecycleScope.launch {
            try {
                val api = RetrofitClient.instanceMemberApi
                val surveyData = com.example.elixir.member.network.SurveyData(
                    memberId = 0, // 서버에서 무시한다면 0, 필요하면 실제 값
                    allergies = userModel.getAllergies() ?: emptyList(),
                    mealStyles = userModel.getPreferredDiets() ?: emptyList(),
                    recipeStyles = userModel.getPreferredRecipes() ?: emptyList(),
                    reasons = userModel.getSignupReason() ?: emptyList()
                )
                val requestBody = RequestBody.create(
                    "application/json; charset=utf-8".toMediaTypeOrNull(),
                    Gson().toJson(surveyData)
                )

                val response = api.putSurvey(requestBody)
                if (response.status == 200) {
                    Toast.makeText(requireContext(), "설문이 저장되었습니다.", Toast.LENGTH_SHORT).show()
                    requireActivity().finish()
                } else {
                    Toast.makeText(requireContext(), "설문 저장에 실패했습니다.", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "설문 저장에 실패했습니다: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
} 