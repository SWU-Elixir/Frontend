import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.elixir.challenge.*
import kotlinx.coroutines.launch

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
                val result = service.getAllChallenges()
                _challenges.value = result
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }

    fun loadChallengesByYear(year: Int) {
        viewModelScope.launch {
            try {
                val result = service.getChallengesByYear(year)
                _challenges.value = result
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }

    fun updateChallenge(challenge: ChallengeEntity) {
        viewModelScope.launch {
            try {
                service.updateChallenge(challenge)
                // 업데이트 후 목록 새로고침
                loadAllChallenges()
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }

    fun clearError() {
        _error.value = null
    }
}


