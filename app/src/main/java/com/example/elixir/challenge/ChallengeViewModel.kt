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

    fun loadAllChallenges() {
        viewModelScope.launch {
            try {
                Log.d("ChallengeViewModel", "Loading all challenges")
                val result = service.getAllChallenges()
                Log.d("ChallengeViewModel", "Loaded ${result.size} challenges")
                _challenges.value = result
            } catch (e: Exception) {
                Log.e("ChallengeViewModel", "Error loading all challenges", e)
                _error.value = e.message
            }
        }
    }

    fun loadChallengesByYear(year: Int) {
        viewModelScope.launch {
            try {
                Log.d("ChallengeViewModel", "Loading challenges for year $year")
                val result = service.getChallengesByYear(year)
                Log.d("ChallengeViewModel", "Loaded ${result.size} challenges for year $year")
                _challenges.value = result
            } catch (e: Exception) {
                Log.e("ChallengeViewModel", "Error loading challenges for year $year", e)
                _error.value = e.message
            }
        }
    }

    fun loadChallengesById(id: Int) {
        viewModelScope.launch {
            try {
                Log.d("ChallengeViewModel", "Loading challenge with id $id")
                val result = service.getChallengeById(id)
                if (result.isNotEmpty()) {
                    Log.d("ChallengeViewModel", "Loaded challenge details: name=${result[0].name}, " +
                        "description=${result[0].description}, purpose=${result[0].purpose}")
                    _challenges.value = result
                } else {
                    Log.w("ChallengeViewModel", "No challenge found with id $id")
                }
            } catch (e: Exception) {
                Log.e("ChallengeViewModel", "Error loading challenge with id $id", e)
                _error.value = e.message
            }
        }
    }

    fun updateChallenge(challenge: ChallengeEntity) {
        viewModelScope.launch {
            try {
                Log.d("ChallengeViewModel", "Updating challenge ${challenge.id}")
                service.updateChallenge(challenge)
                Log.d("ChallengeViewModel", "Successfully updated challenge ${challenge.id}")
                // 업데이트 후 목록 새로고침
                loadAllChallenges()
            } catch (e: Exception) {
                Log.e("ChallengeViewModel", "Error updating challenge ${challenge.id}", e)
                _error.value = e.message
            }
        }
    }

    fun clearError() {
        _error.value = null
    }

    fun loadChallengeProgress(challengeId: Int) {
        viewModelScope.launch {
            try {
                Log.d("ChallengeViewModel", "Loading progress for challenge $challengeId")
                val result = service.getChallengeProgress(challengeId)
                Log.d("ChallengeViewModel", "Loaded progress for challenge $challengeId")
                _challenges.value = result
            } catch (e: Exception) {
                Log.e("ChallengeViewModel", "Error loading progress for challenge $challengeId", e)
                _error.value = e.message
            }
        }
    }

    fun loadChallengeCompletion(challengeId: Int) {
        viewModelScope.launch {
            try {
                Log.d("ChallengeViewModel", "Loading completion for challenge $challengeId")
                val result = service.getChallengeCompletion(challengeId)
                Log.d("ChallengeViewModel", "Loaded completion for challenge $challengeId")
                _challenges.value = result
            } catch (e: Exception) {
                Log.e("ChallengeViewModel", "Error loading completion for challenge $challengeId", e)
                _error.value = e.message
            }
        }
    }
}


