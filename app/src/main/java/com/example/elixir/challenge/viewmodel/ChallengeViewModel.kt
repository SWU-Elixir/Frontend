package com.example.elixir.challenge.ui

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.elixir.challenge.data.ChallengeDetailEntity
import com.example.elixir.challenge.network.ChallengeCompletionRawData
import com.example.elixir.challenge.network.ChallengeProgressData
import com.example.elixir.challenge.network.ChallengeRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ChallengeViewModel(
    private val repository: ChallengeRepository
) : ViewModel() {

    private val _challenges = MutableStateFlow<List<ChallengeDetailEntity>>(emptyList())
    val challenges: StateFlow<List<ChallengeDetailEntity>> = _challenges.asStateFlow()

    private val _selectedChallenge = MutableStateFlow<ChallengeDetailEntity?>(null)
    val selectedChallenge: StateFlow<ChallengeDetailEntity?> = _selectedChallenge.asStateFlow()

    private val _challengeCompletion = MutableStateFlow(ChallengeCompletionRawData())
    val challengeCompletion: StateFlow<ChallengeCompletionRawData> = _challengeCompletion.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    companion object {
        private const val TAG = "ChallengeViewModel"
    }

    fun clearError() {
        _errorMessage.value = null
    }

    fun loadChallengesByYear(year: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                val challenges = repository.fetchAndSaveChallengesByYear(year)
                _challenges.value = challenges
                if (challenges.isEmpty()) {
                    _errorMessage.value = null // 빈 상태
                }
            } catch (e: Exception) {
                Log.e(TAG, "챌린지 로드 실패", e)
                val local = repository.getChallengesByYearFromDb(year)
                _challenges.value = local
                if (local.isEmpty()) {
                    _errorMessage.value = when (e) {
                        is java.net.UnknownHostException -> "인터넷 연결을 확인해주세요"
                        is retrofit2.HttpException -> "서버 오류가 발생했습니다."
                        else -> "챌린지를 불러오는데 실패했습니다."
                    }
                }
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * 상세 + 진행도 한번에 로드하여 머지
     */
    fun loadChallengeWithProgress(id: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                // 1) 상세 불러오기
                val detail = repository.fetchAndSaveChallengeById(id).firstOrNull()
                    ?: repository.getChallengeByIdFromDb(id).firstOrNull()

                if (detail == null) {
                    _errorMessage.value = "해당 챌린지를 찾을 수 없습니다."
                    _isLoading.value = false
                    return@launch
                }

                // 2) 진행도 불러오기
                val progress = try {
                    repository.fetchChallengeProgress(id)
                } catch (e: Exception) {
                    Log.w(TAG, "진행도 로드 실패, 기본값 사용", e)
                    ChallengeProgressData(challengeId = detail.id)
                }

                // 3) 진행도 병합
                val merged = progress?.let {
                    detail.copy(
                        id = progress?.challengeId.takeIf { it != 0 } ?: detail.id,
                        step1Goal1Achieved = it.step1Goal1Achieved,
                        step1Goal2Achieved = progress.step1Goal2Achieved,
                        step2Goal1Active = progress.step2Goal1Active,
                        step2Goal1Achieved = progress.step2Goal1Achieved,
                        step2Goal2Active = progress.step2Goal2Active,
                        step2Goal2Achieved = progress.step2Goal2Achieved,
                        step3Goal1Active = progress.step3Goal1Active,
                        step3Goal1Achieved = progress.step3Goal1Achieved,
                        step3Goal2Active = progress.step3Goal2Active,
                        step3Goal2Achieved = progress.step3Goal2Achieved,
                        step4Goal1Active = progress.step4Goal1Active,
                        step4Goal1Achieved = progress.step4Goal1Achieved,
                        step4Goal2Active = progress.step4Goal2Active,
                        step4Goal2Achieved = progress.step4Goal2Achieved,
                        challengeCompleted = progress.challengeCompleted
                    )
                }

                // 4) 최종 세팅
                _selectedChallenge.value = merged
                if (merged != null) {
                    Log.d(TAG, "챌린지 상세+진행도 로드 완료: ${merged.name}, id=${merged.id}")
                }
            } catch (e: Exception) {
                Log.e(TAG, "챌린지+진행도 로드 실패", e)
                _errorMessage.value = "데이터 로드 실패: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun loadChallengeCompletionForPopup(callback: (Boolean, String?, String?) -> Unit) {
        viewModelScope.launch {
            try {
                val completion = repository.fetchChallengeCompletion()
                _challengeCompletion.value = completion
                callback(
                    completion.challengeCompleted ?: false,
                    completion.achievementName,
                    completion.achievementImageUrl
                )
            } catch (e: Exception) {
                _challengeCompletion.value = ChallengeCompletionRawData()
                callback(false, null, null)
            }
        }
    }
}
