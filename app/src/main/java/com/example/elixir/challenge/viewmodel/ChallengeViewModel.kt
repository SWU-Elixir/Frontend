package com.example.elixir.challenge.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.launch
import android.util.Log
import com.example.elixir.challenge.data.ChallengeEntity
import kotlinx.coroutines.async

class ChallengeViewModel(
    private val service: ChallengeService
) : ViewModel() {
    // 연도별 챌린지 목록
    private val _challenges = MutableLiveData<List<ChallengeEntity>>()
    val challenges: LiveData<List<ChallengeEntity>> get() = _challenges

    // 현재 선택된 챌린지 상세 정보
    private val _selectedChallenge = MutableLiveData<ChallengeEntity?>()
    val selectedChallenge: LiveData<ChallengeEntity?> get() = _selectedChallenge

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> get() = _error

    private val _challengeProgress = MutableLiveData<ChallengeEntity?>()
    val challengeProgress: LiveData<ChallengeEntity?> get() = _challengeProgress

    private val _challengeCompletion = MutableLiveData<ChallengeEntity?>()
    val challengeCompletion: LiveData<ChallengeEntity?> get() = _challengeCompletion

    // 현재 연도의 챌린지 목록 저장
    private var currentYearChallenges: List<ChallengeEntity> = emptyList()

    fun loadChallengesByYear(year: Int) {
        viewModelScope.launch {
            try {
                _error.value = null
                Log.d("ChallengeViewModel", "연도별 챌린지 로드 시작: $year")

                // API에서 데이터 로드 시도
                try {
                    val apiChallenges = service.getChallengesByYear(year)
                    if (apiChallenges.isNotEmpty()) {
                        Log.d("ChallengeViewModel", "API에서 ${apiChallenges.size}개의 챌린지 로드됨")
                        currentYearChallenges = apiChallenges
                        _challenges.value = apiChallenges

                        return@launch
                    }
                } catch (e: Exception) {
                    Log.e("ChallengeViewModel", "API 로드 실패", e)
                }

                // API 실패 시 DB에서 데이터 로드
                val dbChallenges = service.getChallengesByYearFromDb(year)
                if (dbChallenges.isNotEmpty()) {
                    Log.d("ChallengeViewModel", "DB에서 ${dbChallenges.size}개의 챌린지 로드됨")
                    currentYearChallenges = dbChallenges
                    _challenges.value = dbChallenges
                } else {
                    _error.value = "데이터를 찾을 수 없습니다"
                }
            } catch (e: Exception) {
                Log.e("ChallengeViewModel", "데이터 로드 중 오류 발생", e)
                _error.value = "데이터 로드 실패: ${e.message}"
            }
        }
    }

    fun loadChallengeWithProgress(id: Int) {
        viewModelScope.launch {
            try {
                _error.value = null
                // 상세 정보와 진행도 정보를 병렬로 가져오기
                val detailDeferred = async { service.getChallengeById(id).firstOrNull() }
                val progressDeferred = async { service.getChallengeProgress(id) }

                // 두 결과를 동시에 기다림
                val detail = detailDeferred.await()
                val progress = progressDeferred.await()

                if (detail == null) {
                    _error.value = "상세 정보를 찾을 수 없습니다"
                    return@launch
                }

                // 상세 정보 + 진행도 합쳐서 emit
                val merged = detail.copy(
                    step1Goal1Achieved = progress.step1Goal1Achieved,
                    step1Goal2Achieved = progress.step1Goal2Achieved,
                    step2Goal1Active = progress.step2Goal1Active,
                    step2Goal2Active = progress.step2Goal2Active,
                    step2Goal1Achieved = progress.step2Goal1Achieved,
                    step2Goal2Achieved = progress.step2Goal2Achieved,
                    step3Goal1Active = progress.step3Goal1Active,
                    step3Goal2Active = progress.step3Goal2Active,
                    step3Goal1Achieved = progress.step3Goal1Achieved,
                    step3Goal2Achieved = progress.step3Goal2Achieved,
                    step4Goal1Active = progress.step4Goal1Active,
                    step4Goal2Active = progress.step4Goal2Active,
                    step4Goal1Achieved = progress.step4Goal1Achieved,
                    step4Goal2Achieved = progress.step4Goal2Achieved,
                    challengeCompleted = progress.challengeCompleted
                )
                _selectedChallenge.value = merged
            } catch (e: Exception) {
                Log.e("ChallengeViewModel", "챌린지 정보 로드 실패", e)
                _error.value = when (e) {
                    is java.net.UnknownHostException -> "인터넷 연결을 확인해주세요"
                    is retrofit2.HttpException -> "서버 오류가 발생했습니다"
                    else -> "챌린지 정보 로드 실패: ${e.message}"
                }
            }
        }
    }

    fun loadChallengeCompletionForPopup(onResult: (Boolean, String?, String?) -> Unit) {
        viewModelScope.launch {
            try {
                val response = service.getChallengeCompletionRaw()
                // 팝업에 필요한 정보만 콜백으로 전달
                onResult(response.challengeCompleted, response.achievementName, response.achievementImageUrl)
            } catch (e: Exception) {
                onResult(false, null, null)
            }
        }
    }


    fun clearError() {
        _error.value = null
    }
}


