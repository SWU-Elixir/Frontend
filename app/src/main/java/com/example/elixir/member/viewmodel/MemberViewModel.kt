package com.example.elixir.member.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.elixir.member.data.AchievementEntity
import com.example.elixir.member.data.FollowEntity
import com.example.elixir.member.data.FollowItem
import com.example.elixir.member.data.MemberEntity
import com.example.elixir.member.data.ProfileEntity
import com.example.elixir.member.data.RecipeEntity
import com.example.elixir.member.network.MemberRepository // MemberRepository를 import 합니다.
import com.example.elixir.member.network.SignupResponse
import com.example.elixir.member.network.SurveyData // SurveyData import
import com.example.elixir.signup.SignupRequest
import kotlinx.coroutines.launch
import java.io.File

class MemberViewModel(private val repository: MemberRepository) : ViewModel() { // MemberRepository를 주입받습니다.

    private val _member = MutableLiveData<MemberEntity?>()
    val member: LiveData<MemberEntity?> = _member

    private val _profile = MutableLiveData<ProfileEntity?>()
    val profile: LiveData<ProfileEntity?> = _profile

    private val _achievements = MutableLiveData<List<AchievementEntity>>()
    val achievements: LiveData<List<AchievementEntity>> = _achievements

    private val _top3Achievements = MutableLiveData<List<AchievementEntity>>()
    val top3Achievements: LiveData<List<AchievementEntity>> = _top3Achievements

    private val _myRecipes = MutableLiveData<List<RecipeEntity>>()
    val myRecipes: LiveData<List<RecipeEntity>> = _myRecipes

    private val _scrapRecipes = MutableLiveData<List<RecipeEntity>>()
    val scrapRecipes: LiveData<List<RecipeEntity>> = _scrapRecipes

    private val _myFollowing = MutableLiveData<List<FollowEntity>>()
    val myFollowing: LiveData<List<FollowEntity>> = _myFollowing

    private val _myFollower = MutableLiveData<List<FollowEntity>>()
    val myFollower: LiveData<List<FollowEntity>> = _myFollower

    private val _idFollowing = MutableLiveData<List<FollowEntity>>()
    val idFollowing: LiveData<List<FollowEntity>> = _idFollowing

    private val _idFollower = MutableLiveData<List<FollowEntity>>()
    val idFollower: LiveData<List<FollowEntity>> = _idFollower

    private val _signupResult = MutableLiveData<Any?>()
    val signupResult: LiveData<Any?> = _signupResult

    private val _emailVerificationResult = MutableLiveData<SignupResponse?>()
    val emailVerificationResult: LiveData<SignupResponse?> = _emailVerificationResult

    private val _emailCodeVerifyResult = MutableLiveData<SignupResponse?>()
    val emailCodeVerifyResult: LiveData<SignupResponse?> = _emailCodeVerifyResult

    private val _followResult = MutableLiveData<Boolean>()
    val followResult: LiveData<Boolean> = _followResult

    private val _unfollowResult = MutableLiveData<Boolean>()
    val unfollowResult: LiveData<Boolean> = _unfollowResult

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> = _error

    private val _passwordUpdateResult = MutableLiveData<Boolean>()
    val passwordUpdateResult: LiveData<Boolean> = _passwordUpdateResult

    private val _myId = MutableLiveData<Int?>()
    val myId: LiveData<Int?> = _myId

    private val _followingList = MutableLiveData<List<FollowItem>>()
    val followingList: LiveData<List<FollowItem>> = _followingList

    private val _followerList = MutableLiveData<List<FollowItem>>()
    val followerList: LiveData<List<FollowItem>> = _followerList

    private val _memberProfile = MutableLiveData<ProfileEntity?>()
    val memberProfile: LiveData<ProfileEntity?> = _memberProfile

    private val _top3Recipes = MutableLiveData<List<RecipeEntity>>()
    val top3Recipes: LiveData<List<RecipeEntity>> = _top3Recipes

    private val _isFollowingUser = MutableLiveData<Boolean>()
    val isFollowingUser: LiveData<Boolean> = _isFollowingUser

    private val _followActionSuccess = MutableLiveData<Boolean>()
    val followActionSuccess: LiveData<Boolean> = _followActionSuccess

    // 설문 관련 LiveData 추가
    private val _survey = MutableLiveData<SurveyData?>()
    val survey: LiveData<SurveyData?> = _survey

    private val _surveyActionSuccess = MutableLiveData<Boolean>()
    val surveyActionSuccess: LiveData<Boolean> = _surveyActionSuccess

    fun signup(signupRequest: SignupRequest, profileImageFile: File?) {
        viewModelScope.launch {
            try {
                val result = repository.signup(signupRequest, profileImageFile)
                _signupResult.value = result
            } catch (e: Exception) {
                _signupResult.value = null
                _error.value = "회원가입 실패: ${e.message}"
            }
        }
    }

    fun requestEmailVerification(email: String) {
        viewModelScope.launch {
            try {
                val result = repository.requestEmailVerification(email)
                _emailVerificationResult.value = result
            } catch (e: Exception) {
                _error.value = "이메일 인증을 요청할 수 없습니다: ${e.message}"
            }
        }
    }

    fun verifyEmailCode(email: String, code: String) {
        viewModelScope.launch {
            try {
                val result = repository.verifyEmailCode(email, code)
                _emailCodeVerifyResult.value = result
            } catch (e: Exception) {
                _error.value = "인증번호를 확인할 수 없습니다: ${e.message}"
            }
        }
    }

    fun follow(targetMemberId: Int) {
        viewModelScope.launch {
            val result = repository.follow(targetMemberId)
            _followResult.value = result
            if (!result) {
                _error.value = "팔로우 실패"
            }
        }
    }

    fun unfollow(targetMemberId: Int) {
        viewModelScope.launch {
            val result = repository.unfollow(targetMemberId)
            _unfollowResult.value = result
            if (!result) {
                _error.value = "언팔로우 실패"
            }
        }
    }

    fun loadMember() {
        viewModelScope.launch {
            try {
                _member.value = repository.fetchAndSaveMember()
            } catch (e: Exception) {
                _error.value = "회원 정보를 불러올 수 없습니다: ${e.message}"
            }
        }
    }

    fun loadProfile() {
        viewModelScope.launch {
            try {
                _profile.value = repository.fetchAndSaveProfile()
            } catch (e: Exception) {
                _error.value = "프로필 정보를 불러올 수 없습니다: ${e.message}"
            }
        }
    }

    fun loadAchievements() {
        viewModelScope.launch {
            try {
                _achievements.value = repository.fetchAndSaveAchievements()
            } catch (e: Exception) {
                _error.value = "업적을 불러올 수 없습니다: ${e.message}"
            }
        }
    }

    fun loadTop3Achievements() {
        viewModelScope.launch {
            try {
                _top3Achievements.value = repository.fetchAndSaveTop3Achievements()
            } catch (e: Exception) {
                _error.value = "상위 3개 업적을 불러올 수 없습니다: ${e.message}"
            }
        }
    }

    fun loadMyRecipes() {
        viewModelScope.launch {
            try {
                _myRecipes.value = repository.fetchAndSaveMyRecipes()
            } catch (e: Exception) {
                _error.value = "내 레시피를 불러올 수 없습니다: ${e.message}"
            }
        }
    }

    fun loadScrapRecipes() {
        viewModelScope.launch {
            try {
                _scrapRecipes.value = repository.fetchAndSaveScrapRecipes()
            } catch (e: Exception) {
                _error.value = "스크랩 레시피를 불러올 수 없습니다: ${e.message}"
            }
        }
    }

    fun loadMyFollowing() {
        viewModelScope.launch {
            try {
                _myFollowing.value = repository.fetchAndSaveMyFollowing()
            } catch (e: Exception) {
                _error.value = "팔로잉 목록을 불러올 수 없습니다: ${e.message}"
            }
        }
    }

    fun loadMyFollower() {
        viewModelScope.launch {
            try {
                _myFollower.value = repository.fetchAndSaveMyFollower()
            } catch (e: Exception) {
                _error.value = "팔로워 목록을 불러올 수 없습니다: ${e.message}"
            }
        }
    }

    fun loadIdFollowing(targetMemberId: Int) {
        viewModelScope.launch {
            try {
                _idFollowing.value = repository.fetchAndSaveIdFollowing(targetMemberId)
            } catch (e: Exception) {
                _error.value = "특정 사용자 팔로잉 목록을 불러올 수 없습니다: ${e.message}"
            }
        }
    }

    fun loadIdFollower(targetMemberId: Int) {
        viewModelScope.launch {
            try {
                _idFollower.value = repository.fetchAndSaveIdFollower(targetMemberId)
            } catch (e: Exception) {
                _error.value = "특정 사용자 팔로워 목록을 불러올 수 없습니다: ${e.message}"
            }
        }
    }

    fun updatePassword(email: String, newPassword: String) {
        viewModelScope.launch {
            try {
                val result = repository.updatePassword(email, newPassword)
                _passwordUpdateResult.value = result
            } catch (e: Exception) {
                _passwordUpdateResult.value = false
                _error.value = "비밀번호 변경 실패: ${e.message}"
            }
        }
    }

    fun loadMyId() {
        viewModelScope.launch {
            try {
                val member = repository.fetchAndSaveMember() // MemberEntity에 id가 있다면
                _myId.value = member?.id
            } catch (e: Exception) {
                _error.value = "내 아이디를 불러올 수 없습니다: ${e.message}"
                _myId.value = null
            }
        }
    }

    fun loadFollowingList(targetMemberId: Int = -1) {
        viewModelScope.launch {
            try {
                val follows: List<FollowEntity> = if (targetMemberId != -1) {
                    repository.fetchAndSaveIdFollowing(targetMemberId)
                } else {
                    repository.fetchAndSaveMyFollowing()
                }
                val myFollowingIds = repository.getMyFollowingIds() // 내 팔로잉 ID 목록 가져오기

                val followItems = follows.map {
                    FollowItem(
                        followId = it.followId,
                        targetMemberId = it.id ?: it.followId,
                        profileImageRes = it.profileUrl ?: "",
                        memberTitle = it.title,
                        memberNickname = it.nickname,
                        isFollowing = myFollowingIds.contains(it.id ?: it.followId)
                    )
                }
                _followingList.value = followItems
            } catch (e: Exception) {
                _error.value = "팔로잉 목록을 불러올 수 없습니다: ${e.message}"
                _followingList.value = emptyList()
            }
        }
    }

    fun loadFollowerList(targetMemberId: Int = -1) {
        viewModelScope.launch {
            try {
                val follows: List<FollowEntity> = if (targetMemberId != -1) {
                    repository.fetchAndSaveIdFollower(targetMemberId)
                } else {
                    repository.fetchAndSaveMyFollower()
                }
                val myFollowingIds = repository.getMyFollowingIds() // 내 팔로잉 ID 목록 가져오기

                val followItems = follows.map {
                    FollowItem(
                        followId = it.followId,
                        targetMemberId = it.id ?: it.followId,
                        profileImageRes = it.profileUrl ?: "",
                        memberTitle = it.title,
                        memberNickname = it.nickname,
                        isFollowing = myFollowingIds.contains(it.id ?: it.followId)
                    )
                }
                _followerList.value = followItems
            } catch (e: Exception) {
                _error.value = "팔로워 목록을 불러올 수 없습니다: ${e.message}"
                _followerList.value = emptyList()
            }
        }
    }

    // 특정 회원의 프로필 로드
    fun loadMemberProfile(memberId: Int) {
        viewModelScope.launch {
            try {
                val profile = repository.fetchAndSaveProfile(memberId) // 특정 회원 프로필 가져오는 메서드
                _memberProfile.value = profile
            } catch (e: Exception) {
                _error.value = "회원 프로필 로드 실패: ${e.message}"
            }
        }
    }

    // 팔로우 상태 확인
    fun checkFollowStatus(targetMemberId: Int) {
        viewModelScope.launch {
            try {
                val isFollowing = repository.checkIsFollowing(targetMemberId) // Repository에서 팔로우 상태 확인
                _isFollowingUser.value = isFollowing
            } catch (e: Exception) {
                _error.value = "팔로우 상태 확인 실패: ${e.message}"
                _isFollowingUser.value = false // 오류 시 기본값 설정
            }
        }
    }

}