package com.example.elixir.signup

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.elixir.signup.SignupRequest

// 사용자 정보 뷰 모델 (회원가입, 정보 수정)
class UserInfoViewModel : ViewModel() {
    private var accountData: AccountData = AccountData("", "")
    private var profileData: ProfileData = ProfileData("", "", "", 0)
    private var allergies: List<String> = mutableListOf<String>()
    private var preferredDiets: List<String> = mutableListOf<String>()
    private var preferredRecipes: List<String> = mutableListOf<String>()
    private var signupReasons: List<String> = mutableListOf<String>()

    fun toSignupRequest(): SignupRequest? {
        val account = getAccount() ?: return null
        val profile = getProfile() ?: return null
        val allergies = getAllergies() ?: emptyList()
        val mealStyles = getPreferredDiets() ?: emptyList()
        val recipeStyles = getPreferredRecipes() ?: emptyList()
        val reasons = getSignupReason() ?: emptyList()

        return SignupRequest(
            email = account.id,
            password = account.password,
            nickname = profile.nickname,
            gender = profile.gender,
            birthYear = profile.birthYear,
            allergies = allergies,
            mealStyles = mealStyles,
            recipeStyles = recipeStyles,
            reasons = reasons
        )
    }

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

    // 계정 생성
    fun setAccount(email: String, pw: String) {
        accountData = AccountData(email, pw)
    }

    fun getAccount(): AccountData? {
        return if (accountData.id.isNotBlank() && accountData.password.isNotBlank()) accountData
        else null
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

    // 설문조사 - 알러지 정보 설정
    fun setAllergies(algs: List<String>) {
        allergies = algs
    }

    // 설문조사 - 알러지 정보 가져오기
    fun getAllergies(): List<String>? {
        return allergies.ifEmpty { null }
    }

    // 설문조사 - 선호 식단 설정
    fun setPreferredDiets(diets: List<String>) {
        preferredDiets = diets
    }

    // 설문조사 - 선호 식단 가져오기
    fun getPreferredDiets(): List<String>? {
        return preferredDiets.ifEmpty { null }
    }

    // 설문조사 - 선호 레시피 설정
    fun setPreferredRecipes(recipes: List<String>) {
        preferredRecipes = recipes
    }

    // 설문조사 - 선호 레시피 가져오기
    fun getPreferredRecipes(): List<String>? {
        return preferredRecipes.ifEmpty { null }
    }

    // 설문조사 - 저속노화 이유 설정
    fun setSignupReason(reason: List<String>) {
        signupReasons = reason
    }

    // 설문조사 - 저속노화 이유 가져오기
    fun getSignupReason(): List<String>? {
        return signupReasons.ifEmpty { null }
    }

}