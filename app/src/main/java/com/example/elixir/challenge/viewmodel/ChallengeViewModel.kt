package com.example.elixir.challenge.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.launch
import android.util.Log
import com.example.elixir.challenge.data.ChallengeEntity

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

    private fun loadChallengeDetails(id: Int) {
        viewModelScope.launch {
            try {
                _error.value = null
                Log.d("ChallengeViewModel", "챌린지 상세 정보 로드 시작: id=$id")

                // [1] 챌린지 상세 정보 API 호출 시도
                try {
                    val apiChallenge = service.getChallengeById(id)
                    if (apiChallenge.isNotEmpty()) {
                        Log.d("ChallengeViewModel", "API에서 챌린지 상세 정보 로드됨: id=$id")
                        _selectedChallenge.value = apiChallenge[0]
                    }
                } catch (e: Exception) {
                    Log.e("ChallengeViewModel", "API 상세 정보 로드 실패", e)
                }

                // [2] 상세 정보가 아직 없다면 로컬 DB에서 시도
                if (_selectedChallenge.value == null) {
                    val dbChallenge = service.getChallengeById(id)
                    if (dbChallenge.isNotEmpty()) {
                        Log.d("ChallengeViewModel", "DB에서 챌린지 상세 정보 로드됨: id=$id")
                        _selectedChallenge.value = dbChallenge[0]
                    } else {
                        val basicInfo = currentYearChallenges.find { it.id == id }
                        if (basicInfo != null) {
                            Log.d("ChallengeViewModel", "현재 연도 목록에서 기본 정보 사용: id=$id")
                            _selectedChallenge.value = basicInfo
                        } else {
                            _error.value = "상세 정보를 찾을 수 없습니다"
                        }
                    }
                }

                // [3] 챌린지 진행 정보도 함께 로드
                try {
                    val progress = service.getChallengeProgress()
                    Log.d("ChallengeViewModel", "챌린지 진행도 로드됨: id=$id, progress=$progress")
                    _challengeProgress.value = challengeProgress.value
                } catch (e: Exception) {
                    Log.e("ChallengeViewModel", "챌린지 진행도 로드 실패", e)
                    // 실패해도 UI에는 영향 없이 넘어가도록 처리
                }

            } catch (e: Exception) {
                Log.e("ChallengeViewModel", "상세 정보 로드 중 오류 발생", e)
                _error.value = "상세 정보 로드 실패: ${e.message}"
            }
        }
    }

    fun loadChallengeCompletion() {
        viewModelScope.launch {
            try {
                _error.value = null
                // API에서 데이터 가져오기 시도
                _challenges.value = service.getChallengeCompletion()
            } catch (e: Exception) {
                    _error.value = "데이터 로드 실패: ${e.message}"
                }

        }
    }


    fun loadChallengesById(id: Int) {
        loadChallengeDetails(id)
    }


    fun clearError() {
        _error.value = null
    }
}


