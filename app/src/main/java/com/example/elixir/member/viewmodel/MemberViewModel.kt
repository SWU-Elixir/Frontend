package com.example.elixir.member.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.elixir.member.data.AchievementEntity
import com.example.elixir.member.data.MemberEntity
import com.example.elixir.member.data.FollowEntity
import com.example.elixir.member.data.RecipeEntity
import kotlinx.coroutines.launch
import com.example.elixir.signup.SignupRequest
import java.io.File

class MemberViewModel (
    private val service: MemberService
) : ViewModel() {
    private val _member = MutableLiveData<MemberEntity?>()
    val member: MutableLiveData<MemberEntity?> = _member

    private val _achievement = MutableLiveData<List<AchievementEntity>>()
    val achievement: LiveData<List<AchievementEntity>> = _achievement

    private val _recipe = MutableLiveData<List<RecipeEntity>>()
    val recipe: LiveData<List<RecipeEntity>> = _recipe

    private val _scrap = MutableLiveData<List<RecipeEntity>>()
    val scrap: LiveData<List<RecipeEntity>> = _scrap

    private val _following = MutableLiveData<List<FollowEntity>>()
    val following: LiveData<List<FollowEntity>> = _following

    private val _follower = MutableLiveData<List<FollowEntity>>()
    val follower: LiveData<List<FollowEntity>> = _follower

    private val _followResult = MutableLiveData<Boolean>()
    val followResult: LiveData<Boolean> = _followResult

    private val _unfollowResult = MutableLiveData<Boolean>()
    val unfollowResult: LiveData<Boolean> = _unfollowResult

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    private val _signupResult = MutableLiveData<Any?>()
    val signupResult: LiveData<Any?> = _signupResult

    private val _top3Achievements = MutableLiveData<List<AchievementEntity>>()
    val top3Achievements: LiveData<List<AchievementEntity>> = _top3Achievements

    private val _top3Recipes = MutableLiveData<List<String>>()
    val top3Recipes: LiveData<List<String>> = _top3Recipes

    private val _top3Scraps = MutableLiveData<List<String>>()
    val top3Scraps: LiveData<List<String>> = _top3Scraps

    fun loadMember() {
        viewModelScope.launch {
            _error.value = null

            try {
                val apiData = service.getMember()
                if (apiData != null) {
                    _member.value = apiData
                } else {
                    // API 응답은 성공했으나 null인 경우 → DB 시도
                    val dbData = service.getMemberFromDb()
                    if (dbData != null) {
                        _member.value = dbData
                    } else {
                        _error.value = "데이터를 불러올 수 없습니다. 인터넷 연결을 확인해주세요."
                    }
                }
            } catch (e: Exception) {
                _error.value = "팔로워 목록 로드 실패: ${e.message}"
            }
        }
    }

    fun loadAchievement() {
        viewModelScope.launch {
            try {
                _error.value = null
                // API에서 데이터 가져오기 시도
                _achievement.value = service.getAchievement()
            } catch (e: Exception) {
                Log.e("MemberViewModel", "팔로잉 API 호출 실패, DB에서 데이터 로드 시도", e)
                // API 호출 실패시 DB에서 데이터 로드
                try {
                    val dbData = service.getAchievementFromDb()
                    if (dbData.isEmpty()) {
                        _error.value = "팔로잉 목록을 불러올 수 없습니다. 인터넷 연결을 확인해주세요."
                    } else {
                        _achievement.value = dbData
                    }
                } catch (e: Exception) {
                    _error.value = "팔로잉 목록 로드 실패: ${e.message}"
                }
            }
        }
    }

    fun loadRecipe() {
        viewModelScope.launch {
            try {
                _error.value = null
                // API에서 데이터 가져오기 시도
                _recipe.value = service.getMyRecipes()
            } catch (e: Exception) {
                Log.e("MemberViewModel", "팔로잉 API 호출 실패, DB에서 데이터 로드 시도", e)
                // API 호출 실패시 DB에서 데이터 로드
                try {
                    val dbData = service.getMyRecipesFromDb()
                    if (dbData.isEmpty()) {
                        _error.value = "팔로잉 목록을 불러올 수 없습니다. 인터넷 연결을 확인해주세요."
                    } else {
                        _recipe.value = dbData
                    }
                } catch (e: Exception) {
                    _error.value = "팔로잉 목록 로드 실패: ${e.message}"
                }
            }
        }
    }

    fun loadScrap() {
        viewModelScope.launch {
            try {
                _error.value = null
                // API에서 데이터 가져오기 시도
                _scrap.value = service.getScrapRecipes()
            } catch (e: Exception) {
                Log.e("MemberViewModel", "팔로잉 API 호출 실패, DB에서 데이터 로드 시도", e)
                // API 호출 실패시 DB에서 데이터 로드
                try {
                    val dbData = service.getScrapRecipesFromDb()
                    if (dbData.isEmpty()) {
                        _error.value = "팔로잉 목록을 불러올 수 없습니다. 인터넷 연결을 확인해주세요."
                    } else {
                        _scrap.value = dbData
                    }
                } catch (e: Exception) {
                    _error.value = "팔로잉 목록 로드 실패: ${e.message}"
                }
            }
        }
    }

    fun loadFollowing(targetMemberId: Int) {
        viewModelScope.launch {
            try {
                _error.value = null
                // API에서 데이터 가져오기 시도
                _following.value = service.getFollowing(targetMemberId)
            } catch (e: Exception) {
                Log.e("MemberViewModel", "팔로잉 API 호출 실패, DB에서 데이터 로드 시도", e)
                // API 호출 실패시 DB에서 데이터 로드
                try {
                    val dbData = service.getFollowingFromDb(targetMemberId)
                    if (dbData.isEmpty()) {
                        _error.value = "팔로잉 목록을 불러올 수 없습니다. 인터넷 연결을 확인해주세요."
                    } else {
                        _following.value = dbData
                    }
                } catch (e: Exception) {
                    _error.value = "팔로잉 목록 로드 실패: ${e.message}"
                }
            }
        }
    }

    fun loadFollower(targetMemberId: Int) {
        viewModelScope.launch {
            try {
                _error.value = null
                _follower.value = service.getFollower(targetMemberId)
            } catch (e: Exception) {
                Log.e("MemberViewModel", "팔로워 API 호출 실패, DB에서 데이터 로드 시도", e)
                try {
                    val dbData = service.getFollowerFromDb(targetMemberId)
                    if (dbData.isEmpty()) {
                        _error.value = "팔로워 목록을 불러올 수 없습니다. 인터넷 연결을 확인해주세요."
                    } else {
                        _follower.value = dbData
                    }
                } catch (e: Exception) {
                    _error.value = "팔로워 목록 로드 실패: ${e.message}"
                }
            }
        }
    }

    fun follow(targetMemberId: Int) {
        viewModelScope.launch {
            _followResult.value = service.follow(targetMemberId)
        }
    }

    fun unfollow(targetMemberId: Int) {
        viewModelScope.launch {
            _unfollowResult.value = service.unfollow(targetMemberId)
        }
    }

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

    fun loadTop3Achievements() {
        viewModelScope.launch {
            try {
                val data = service.getTop3Achievements()
                _top3Achievements.value = data
            } catch (e: Exception) {
                _top3Achievements.value = emptyList()
            }
        }
    }

    fun loadTop3Recipes() {
        viewModelScope.launch {
            try {
                val data = service.getMyRecipes().take(3).map { it.imageUrl }
                _top3Recipes.value = data
            } catch (e: Exception) {
                _top3Recipes.value = emptyList()
            }
        }
    }

    fun loadTop3Scraps() {
        viewModelScope.launch {
            try {
                val data = service.getScrapRecipes().take(3).map { it.imageUrl }
                _top3Scraps.value = data
            } catch (e: Exception) {
                _top3Scraps.value = emptyList()
            }
        }
    }
}