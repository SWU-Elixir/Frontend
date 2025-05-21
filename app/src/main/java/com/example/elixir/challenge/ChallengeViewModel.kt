import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.elixir.challenge.*
import kotlinx.coroutines.launch
import android.util.Log

class ChallengeViewModel(
    private val service: ChallengeService
) : ViewModel() {
    private val _challenges = MutableLiveData<List<ChallengeEntity>>()
    val challenges: LiveData<List<ChallengeEntity>> get() = _challenges

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> get() = _error

    private val _challengeProgress = MutableLiveData<ChallengeEntity?>()
    val challengeProgress: LiveData<ChallengeEntity?> get() = _challengeProgress

    private val _challengeCompletion = MutableLiveData<ChallengeEntity?>()
    val challengeCompletion: LiveData<ChallengeEntity?> get() = _challengeCompletion

//    fun loadAllChallenges() {
//        viewModelScope.launch {
//            try {
//                Log.d("ChallengeViewModel", "Loading all challenges")
//                val result = service.getAllChallenges()
//                Log.d("ChallengeViewModel", "Loaded ${result.size} challenges")
//                _challenges.value = result
//            } catch (e: Exception) {
//                Log.e("ChallengeViewModel", "Error loading all challenges", e)
//                _error.value = e.message
//            }
//        }
//    }

    fun loadChallengesByYear(year: Int) {
        viewModelScope.launch {
            try {
                Log.d("ChallengeViewModel", "=== API 호출: /api/challenge/year/$year ===")
                val result = service.getChallengesByYear(year)
                Log.d("ChallengeViewModel", "연도별 챌린지 목록 로드 완료: ${result.size}개")
                result.forEach { challenge ->
                    Log.d("ChallengeViewModel", "챌린지 정보: id=${challenge.id}, name=${challenge.name}")
                }
                _challenges.value = result

                // 첫 번째 챌린지의 상세 정보 로드
                if (result.isNotEmpty()) {
                    loadChallengesById(result[0].id)
                }
            } catch (e: Exception) {
                Log.e("ChallengeViewModel", "연도별 챌린지 로드 실패", e)
                _error.value = e.message
            }
        }
    }

    fun loadChallengesById(id: Int) {
        viewModelScope.launch {
            try {
                Log.d("ChallengeViewModel", "=== API 호출: /api/challenge/$id ===")
                val result = service.getChallengeById(id)
                if (result.isNotEmpty()) {
                    val challenge = result[0]
                    Log.d("ChallengeViewModel", "챌린지 상세 정보 로드 완료:")
                    Log.d("ChallengeViewModel", "- id: ${challenge.id}")
                    Log.d("ChallengeViewModel", "- name: ${challenge.name}")
                    Log.d("ChallengeViewModel", "- description: ${challenge.description}")
                    Log.d("ChallengeViewModel", "- purpose: ${challenge.purpose}")
                    Log.d("ChallengeViewModel", "- achievementName: ${challenge.achievementName}")
                    Log.d("ChallengeViewModel", "- challengeCompleted: ${challenge.challengeCompleted}")
                    Log.d("ChallengeViewModel", "- 스테이지 1: ${challenge.step1Goal1Desc}, ${challenge.step1Goal2Desc}")
                    Log.d("ChallengeViewModel", "- 스테이지 2: ${challenge.step2Goal1Desc}, ${challenge.step2Goal2Desc}")
                    Log.d("ChallengeViewModel", "- 스테이지 3: ${challenge.step3Goal1Desc}, ${challenge.step3Goal2Desc}")
                    Log.d("ChallengeViewModel", "- 스테이지 4: ${challenge.step4Goal1Desc}, ${challenge.step4Goal2Desc}")
                    _challenges.value = result

                    // 진행상황 로드
                    loadChallengeProgress(id)
                } else {
                    Log.w("ChallengeViewModel", "챌린지 상세 정보 없음: id=$id")
                }
            } catch (e: Exception) {
                Log.e("ChallengeViewModel", "챌린지 상세 정보 로드 실패", e)
                _error.value = e.message
            }
        }
    }

//    fun updateChallenge(challenge: ChallengeEntity) {
//        viewModelScope.launch {
//            try {
//                Log.d("ChallengeViewModel", "Updating challenge ${challenge.id}")
//                service.updateChallenge(challenge)
//                Log.d("ChallengeViewModel", "Successfully updated challenge ${challenge.id}")
//                // 업데이트 후 목록 새로고침
//                loadAllChallenges()
//            } catch (e: Exception) {
//                Log.e("ChallengeViewModel", "Error updating challenge ${challenge.id}", e)
//                _error.value = e.message
//            }
//        }
//    }

    fun loadChallengeProgress(challengeId: Int) {
        viewModelScope.launch {
            try {
                Log.d("ChallengeViewModel", "=== API 호출: /api/challenge/progress/$challengeId ===")
                val result = service.getChallengeProgress(challengeId)
                if (result.isNotEmpty()) {
                    val progress = result[0]
                    Log.d("ChallengeViewModel", "챌린지 진행상황 로드 완료:")
                    Log.d("ChallengeViewModel", "- 스테이지 1 달성: ${progress.step1Goal1Achieved}, ${progress.step1Goal2Achieved}")
                    Log.d("ChallengeViewModel", "- 스테이지 2 달성: ${progress.step2Goal1Achieved}, ${progress.step2Goal2Achieved}")
                    Log.d("ChallengeViewModel", "- 스테이지 3 달성: ${progress.step3Goal1Achieved}, ${progress.step3Goal2Achieved}")
                    Log.d("ChallengeViewModel", "- 스테이지 4 달성: ${progress.step4Goal1Achieved}, ${progress.step4Goal2Achieved}")
                    _challenges.value = result
                } else {
                    Log.w("ChallengeViewModel", "챌린지 진행상황 없음: id=$challengeId")
                }
            } catch (e: Exception) {
                Log.e("ChallengeViewModel", "챌린지 진행상황 로드 실패", e)
                _error.value = e.message
            }
        }
    }

//    fun loadChallengeCompletion(challengeId: Int) {
//        viewModelScope.launch {
//            try {
//                Log.d("ChallengeViewModel", "Loading completion for challenge $challengeId")
//                val result = service.getChallengeCompletion(challengeId)
//                if (result.isNotEmpty()) {
//                    Log.d("ChallengeViewModel", "Loaded completion for challenge $challengeId")
//                    _challengeCompletion.value = result[0]
//                    // 완료 상태가 변경되면 챌린지 목록도 업데이트
//                    _challenges.value = result
//                } else {
//                    Log.w("ChallengeViewModel", "No completion found for challenge $challengeId")
//                }
//            } catch (e: Exception) {
//                Log.e("ChallengeViewModel", "Error loading completion for challenge $challengeId", e)
//                _error.value = e.message
//            }
//        }
//    }

    fun clearError() {
        _error.value = null
    }
}


