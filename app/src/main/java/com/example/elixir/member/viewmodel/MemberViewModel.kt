package com.example.elixir.member.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.elixir.RetrofitClient
import com.example.elixir.member.data.ProfileEntity
import kotlinx.coroutines.launch
import com.example.elixir.signup.SignupRequest
import java.io.File

class MemberViewModel( private val service: MemberService ) : ViewModel() {
    private val _profile = MutableLiveData<ProfileEntity>()
    val profile: LiveData<ProfileEntity> = _profile

    private val _achievements = MutableLiveData<List<String>>()
    val achievements: LiveData<List<String>> = _achievements

    private val _myRecipes = MutableLiveData<List<String>>()
    val myRecipes: LiveData<List<String>> = _myRecipes

    private val _scrapRecipes = MutableLiveData<List<String>>()
    val scrapRecipes: LiveData<List<String>> = _scrapRecipes

    private val _signupResult = MutableLiveData<Any?>()
    val signupResult: LiveData<Any?> = _signupResult

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> = _error

    fun signup(signupRequest: SignupRequest, profileImageFile: File?) {
        viewModelScope.launch {
            try {
                val result = service.signup(signupRequest, profileImageFile)
                _signupResult.value = result
            } catch (e: Exception) {
                _signupResult.value = null
                _error.value = "회원가입 실패: ${e.message}"
            }
        }
    }

    fun loadMemberProfile() {
        viewModelScope.launch {
            try {
                val api = RetrofitClient.instanceMemberApi
                val response = api.getProfile()
                if (response.status == 200) {
                    response.data?.let { member ->
                        _profile.value = member
                    }
                } else {
                    _error.value = "회원 정보를 불러올 수 없습니다."
                }
            } catch (e: Exception) {
                _error.value = "회원 정보를 불러올 수 없습니다: ${e.message}"
            }
        }
    }

    fun loadTop3Achievements() {
        viewModelScope.launch {
            try {
                val api = RetrofitClient.instanceMemberApi
                val response = api.getTop3Achievements()
                if (response.status == 200) {
                    _achievements.value = response.data.map { it.achievementImageUrl ?: "" }
                }
            } catch (e: Exception) {
                _error.value = "뱃지 목록을 불러올 수 없습니다: ${e.message}"
            }
        }
    }

    fun loadTop3Recipes() {
        viewModelScope.launch {
            try {
                val api = RetrofitClient.instanceMemberApi
                val response = api.getMyRecipes()
                if (response.status == 200) {
                    _myRecipes.value = response.data.take(3).map { it.imageUrl ?: "" }
                }
            } catch (e: Exception) {
                _error.value = "레시피 목록을 불러올 수 없습니다: ${e.message}"
            }
        }
    }

    fun loadTop3Scraps() {
        viewModelScope.launch {
            try {
                val api = RetrofitClient.instanceMemberApi
                val response = api.getScrapRecipes()
                if (response.status == 200) {
                    _scrapRecipes.value = response.data.take(3).map { it.imageUrl ?: "" }
                }
            } catch (e: Exception) {
                _error.value = "스크랩 목록을 불러올 수 없습니다: ${e.message}"
            }
        }
    }
}
