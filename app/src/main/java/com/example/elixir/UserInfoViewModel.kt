package com.example.elixir

import androidx.lifecycle.ViewModel

// 사용자 정보 뷰 모델 (회원가입, 정보 수정)
class UserInfoViewModel : ViewModel() {
    private val signupData = SignupData()

    // 정보 입력
    fun setBasicInfo(id: String, password: String, nickname: String, profileImage: String, sex: String, birthYear: Int) {
        signupData.id = id
        signupData.pw = password
        signupData.nickname = nickname
        signupData.profileImage = profileImage
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