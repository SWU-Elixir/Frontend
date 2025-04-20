package com.example.elixir.signup

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

// 사용자 정보 뷰 모델 (회원가입, 정보 수정)
class UserInfoViewModel : ViewModel() {
    var profileData: ProfileData = ProfileData("", "", "", 0)
    private var id: String = ""
    private var password: String = ""
    private var allergies: List<String> = mutableListOf<String>()
    private var preferredDiet: String = ""
    private var preferredRecipes: List<String> = mutableListOf<String>()
    private var signupReason: String = ""

    // 회원가입 단계 설정 (계정 생성 제외)
    var currentStep = 0
    val maxStep = 4  // 마지막 단계 번호

    // 단계별 정보 입력 완료 여부
    val completedStep = MutableLiveData(
        mutableMapOf(
            0 to false,
            1 to false,
            2 to false,
            3 to false,
            4 to false
        )
    )

    // 완료 단계 추가
    fun updateCompletedStep(step: Int, status: Boolean) {
        val map = completedStep.value ?: mutableMapOf()
        map[step] = status
        completedStep.value = map
    }

    // 현재 단계가 완료됐는지
    fun isStepCompleted(): Boolean {
        return completedStep.value?.get(currentStep) == true
    }

    // 계정 생성
    fun setAccount(email: String, pw: String) {
        id = email
        password = pw
    }

    // 프로필 생성
    fun setProfile(profileImage: String, nickname: String, gender: String, birthYear: Int) {
        profileData = ProfileData(profileImage, nickname, gender, birthYear)
    }

    // 프로필 조회
    fun getProfile(): ProfileData? {
        return if (profileData.profileImage.isNotBlank() && profileData.nickname.isNotBlank()
            && profileData.gender.isNotBlank() && profileData.birthYear != 0)
            profileData
        else null
    }

    // 설문조사 - 알러지
    fun setAllergies(algs: List<String>) {
        allergies = algs
    }

    // 설문조사 - 선호 식단
    fun setPreferredDiets(diet: String) {
        preferredDiet = diet
    }

    // 설문조사 - 선호 레시피
    fun setPreferredRecipes(recipes: List<String>) {
        preferredRecipes = recipes
    }

    // 설문조사 - 저속노화 이유
    fun setSignupReason(reason: String) {
        signupReason = reason
    }
}