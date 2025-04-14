package com.example.elixir

import androidx.lifecycle.ViewModel

// 사용자 정보 뷰 모델 (회원가입, 정보 수정)
class UserInfoViewModel : ViewModel() {
    private val signupData = SignupData()

    // 회원가입 단계 설정 (계정 생성 제외)
    var currentStep = 0
    val maxStep = 4  // 마지막 단계 번호

    // 완료 단계 배열
    private val _completedSteps = mutableSetOf<Int>()
    val completedSteps: Set<Int> get() = _completedSteps

    fun completeStep(step: Int) {
        _completedSteps.add(step)
        currentStep = step + 1
    }

    // 계정 생성
    fun setAccount(email: String, pw: String) {
        signupData.id = email
        signupData.pw = pw
    }

    // 프로필 생성
    fun setProfile(img: String, nick: String, sex: String, birthYear: Int) {
        signupData.profileImage = img
        signupData.nickname = nick
        signupData.sex = sex
        signupData.birthYear = birthYear
    }

    // 설문조사 - 알러지
    fun setAllergies(allergies: List<String>) {
        signupData.allergies = allergies
    }

    // 설문조사 - 선호 식단
    fun setPreferredDiets(diets: List<String>) {
        signupData.preferredDiets = diets
    }

    // 설문조사 - 선호 레시피
    fun setPreferredRecipes(recipes: List<String>) {
        signupData.preferredRecipes = recipes
    }

    // 설문조사 - 저속노화 이유
    fun setSignupReason(reason: String) {
        signupData.signupReason = reason
    }

    // 정보 가져 오기
    fun getFinalSignupData(): SignupData {
        return signupData
    }
}