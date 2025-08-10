package com.example.elixir.challenge.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.launch
import android.util.Log
import com.example.elixir.challenge.data.ChallengeDetailEntity
import com.example.elixir.challenge.network.ChallengeRepository
import kotlinx.coroutines.async

class ChallengeViewModel(
    private val repository: ChallengeRepository
) : ViewModel() {

    companion object {
        private const val TAG = "ChallengeViewModel"
    }

    // 연도별 챌린지 목록
    private val _challenges = MutableLiveData<List<ChallengeDetailEntity>>()
    val challenges: LiveData<List<ChallengeDetailEntity>> get() = _challenges

    // 현재 선택된 챌린지 상세 정보
    private val _selectedChallenge = MutableLiveData<ChallengeDetailEntity?>()
    val selectedChallenge: LiveData<ChallengeDetailEntity?> get() = _selectedChallenge

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> get() = _error

    private val _challengeProgress = MutableLiveData<ChallengeDetailEntity?>()
    val challengeProgress: LiveData<ChallengeDetailEntity?> get() = _challengeProgress

    private val _challengeCompletion = MutableLiveData<ChallengeDetailEntity?>()
    val challengeCompletion: LiveData<ChallengeDetailEntity?> get() = _challengeCompletion

    // 현재 연도의 챌린지 목록 저장
    private var currentYearChallenges: List<ChallengeDetailEntity> = emptyList()

    /**
     * 연도별 챌린지 로드
     * Repository에서 API 실패 시 자동으로 DB 폴백 처리
     */
    fun loadChallengesByYear(year: Int) {
        viewModelScope.launch {
            try {
                _error.value = null
                Log.d(TAG, "연도별 챌린지 로드 시작: $year")

                // Repository에서 이미 API 실패 시 DB 폴백 처리됨
                val challenges = repository.fetchAndSaveChallengesByYear(year)

                if (challenges.isNotEmpty()) {
                    Log.d(TAG, "${challenges.size}개의 챌린지 로드됨")
                    currentYearChallenges = challenges
                    _challenges.value = challenges
                } else {
                    Log.w(TAG, "로드된 챌린지가 없음")
                    _error.value = "해당 연도의 챌린지를 찾을 수 없습니다"
                }

            } catch (e: Exception) {
                Log.e(TAG, "챌린지 로드 중 오류 발생", e)
                _error.value = when (e) {
                    is java.net.UnknownHostException -> "인터넷 연결을 확인해주세요"
                    is retrofit2.HttpException -> "서버 오류가 발생했습니다"
                    else -> "챌린지 로드 실패: ${e.message}"
                }
            }
        }
    }

    /**
     * 챌린지 상세 정보와 진행도를 함께 로드
     */
    fun loadChallengeWithProgress(id: Int) {
        viewModelScope.launch {
            try {
                _error.value = null
                Log.d(TAG, "챌린지 상세 정보 및 진행도 로드 시작: $id")

                // 상세 정보와 진행도 정보를 병렬로 가져오기
                val detailDeferred = async {
                    // Repository에서 이미 API 실패 시 DB 폴백 처리됨
                    repository.fetchAndSaveChallengeById(id).firstOrNull()
                }
                val progressDeferred = async {
                    try {
                        repository.fetchChallengeProgress(id)
                    } catch (e: Exception) {
                        Log.w(TAG, "진행도 로드 실패, 기본값 사용", e)
                        null
                    }
                }

                // 두 결과를 동시에 기다림
                val detail = detailDeferred.await()
                val progress = progressDeferred.await()

                if (detail == null) {
                    _error.value = "챌린지 상세 정보를 찾을 수 없습니다"
                    return@launch
                }

                // 진행도 정보가 있으면 합치고, 없으면 기본 상세 정보만 사용
                val merged = if (progress != null) {
                    detail.copy(
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
                } else {
                    detail
                }

                _selectedChallenge.value = merged
                Log.d(TAG, "챌린지 정보 로드 완료")

            } catch (e: Exception) {
                Log.e(TAG, "챌린지 정보 로드 실패", e)
                _error.value = when (e) {
                    is java.net.UnknownHostException -> "인터넷 연결을 확인해주세요"
                    is retrofit2.HttpException -> "서버 오류가 발생했습니다"
                    else -> "챌린지 정보 로드 실패: ${e.message}"
                }
            }
        }
    }

    /**
     * 챌린지 완료 정보를 팝업용으로 로드
     */
    fun loadChallengeCompletionForPopup(onResult: (Boolean, String?, String?) -> Unit) {
        viewModelScope.launch {
            try {
                Log.d(TAG, "챌린지 완료 정보 로드 시작")
                val response = repository.fetchChallengeCompletionRaw()
                Log.d(TAG, "챌린지 완료 정보 로드 성공")
                onResult(response.challengeCompleted, response.achievementName, response.achievementImageUrl)
            } catch (e: Exception) {
                Log.e(TAG, "챌린지 완료 정보 로드 실패", e)
                // 실패 시 기본값 반환
                onResult(false, null, null)
            }
        }
    }

    /**
     * DB에서만 연도별 챌린지 조회 (오프라인 전용)
     */
    fun loadChallengesByYearFromDbOnly(year: Int) {
        viewModelScope.launch {
            try {
                _error.value = null
                Log.d(TAG, "DB에서만 연도별 챌린지 로드: $year")

                val challenges = repository.getChallengesByYearFromDb(year)

                if (challenges.isNotEmpty()) {
                    Log.d(TAG, "DB에서 ${challenges.size}개의 챌린지 로드됨")
                    currentYearChallenges = challenges
                    _challenges.value = challenges
                } else {
                    Log.w(TAG, "DB에 해당 연도의 챌린지가 없음")
                    _error.value = "저장된 챌린지를 찾을 수 없습니다"
                }

            } catch (e: Exception) {
                Log.e(TAG, "DB 챌린지 로드 실패", e)
                _error.value = "저장된 데이터 로드 실패: ${e.message}"
            }
        }
    }

    /**
     * 에러 상태 초기화
     */
    fun clearError() {
        _error.value = null
    }
}