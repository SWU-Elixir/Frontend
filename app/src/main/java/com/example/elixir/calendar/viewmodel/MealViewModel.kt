package com.example.elixir.calendar.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.elixir.calendar.data.DietLogData
import com.example.elixir.calendar.data.MealDto
import com.example.elixir.calendar.network.db.DietLogRepository
import com.example.elixir.calendar.network.response.GetScoreResponse
import com.example.elixir.member.network.MemberRepository
import kotlinx.coroutines.launch
import com.example.elixir.calendar.data.toEntity
import com.example.elixir.calendar.network.response.GetMealResponse
import com.example.elixir.calendar.network.response.toEntity
import com.example.elixir.ingredient.data.IngredientData
import com.example.elixir.ingredient.network.IngredientRepository
import java.io.File
import kotlin.coroutines.cancellation.CancellationException

class MealViewModel(
    private val dietRepository: DietLogRepository,
    private val memberRepository: MemberRepository,
    private val ingredientRepository: IngredientRepository
) : ViewModel() {

    private val _dietLogDetail = MutableLiveData<MealDto>()
    val dietLogDetail: LiveData<MealDto> = _dietLogDetail

    private val _dailyDietLogs = MutableLiveData<List<MealDto>>()
    val dailyDietLogs: LiveData<List<MealDto>> = _dailyDietLogs

    private val _monthlyScore = MutableLiveData<GetScoreResponse>()
    val monthlyScore: LiveData<GetScoreResponse> = _monthlyScore

    private val _uploadResult = MutableLiveData<Result<Boolean>>() // 성공/실패 구분
    val uploadResult: LiveData<Result<Boolean>> get() = _uploadResult

    private val _deleteResult = MutableLiveData<Result<Boolean>>()
    val deleteResult: LiveData<Result<Boolean>> get() = _deleteResult

    // ViewModel에서 전체 식재료 리스트를 불러와 LiveData로 노출
    val ingredientList = MutableLiveData<List<IngredientData>>()

    private val _updateResult = MutableLiveData<Result<Boolean>>()
    val updateResult: LiveData<Result<Boolean>> = _updateResult


    // 특정 ID의 식단 기록 가져오기
    fun getDietLogById(dietLogId: Int) {
        viewModelScope.launch {
            val result = dietRepository.getDietLogById(dietLogId)
            result?.let { _dietLogDetail.value = it }
        }
    }

    // 로컬 DB에 저장
    fun saveToLocalDB(data: DietLogData) {
        val entity = data.toEntity()
        viewModelScope.launch {
            dietRepository.insertDietLog(entity)
            Log.d("RoomTest", "로컬 저장됨: $entity")
        }
    }

    // 로컬 DB + 서버 업로드
    fun saveAndUpload(data: DietLogData, imageFile: File) {
        val entity = data.toEntity().copy(imageUrl = "")
        viewModelScope.launch {
            try {
                dietRepository.insertDietLog(entity)
                val uploadResult: GetMealResponse? = dietRepository.uploadDietLog(entity, imageFile)

                val updatedEntity = uploadResult?.toEntity()
                if (updatedEntity != null && updatedEntity.imageUrl.isNotEmpty()) {
                    dietRepository.updateDietLog(updatedEntity)
                    Log.d("Upload", "서버 업로드 성공, imageUrl=${updatedEntity.imageUrl}")
                    _uploadResult.postValue(Result.success(true))
                } else {
                    Log.e("Upload", "서버 업로드 실패, entity=$entity, imageFile=${imageFile.path}")
                    _uploadResult.postValue(Result.failure(Exception("서버 업로드 실패")))
                }
            } catch (e: CancellationException) {
                Log.w("Upload", "코루틴 취소됨: ${e.message}")
                _uploadResult.postValue(Result.failure(e))
            } catch (e: Exception) {
                Log.e("Upload", "예외 발생: ${e.message}", e)
                _uploadResult.postValue(Result.failure(e))
            }
        }
    }


    // 특정 날짜의 식단 기록 목록
    fun getDietLogsByDate(date: String) {
        viewModelScope.launch {
            val result = dietRepository.getDietLogsByDate(date)
            _dailyDietLogs.value = result ?: emptyList()
        }
    }

    // 월별 점수 가져오기
    fun getMonthlyScore(year: Int, month: Int) {
        viewModelScope.launch {
            val score = dietRepository.getMonthlyScore(year, month)
            score?.let {
                _monthlyScore.value = GetScoreResponse(
                    code = "200 OK",
                    status = 200,
                    data = it,
                    message = "Success"
                )
            }
        }
    }

    // 식단 삭제
    fun deleteDietLog(dietId: Int) {
        viewModelScope.launch {
            try {
                dietRepository.deleteDietLog(dietId)
                _deleteResult.postValue(Result.success(true))
            } catch (e: Exception) {
                _deleteResult.postValue(Result.failure(e))
            }
        }
    }

    // 멤버 ID 가져오기
    fun getMemberID(onResult: (Int?) -> Unit) {
        viewModelScope.launch {
            try {
                val member = memberRepository.getMemberFromDb()
                onResult(member?.id)
            } catch (e: Exception) {
                Log.e("MealViewModel", "Failed to load member: ${e.message}")
                onResult(null)
            }
        }
    }

    // 식단 기록 수정
    fun updateDietLog(data: DietLogData, imageFile: File?) {
        val entity = data.toEntity()
        viewModelScope.launch {
            val response = dietRepository.updateDietLog(entity, imageFile)
            val updatedEntity = response?.data?.toEntity()

            if (updatedEntity != null) {
                dietRepository.updateDietLog(updatedEntity)
                Log.d("Update", "식단 기록 수정 성공")
                _updateResult.postValue(Result.success(true))
            } else {
                Log.e("Update", "식단 기록 수정 실패")
                _updateResult.postValue(Result.failure(Exception("식단 기록 수정 실패")))
            }
        }
    }

    //
    fun updateToLocalDB(data: DietLogData) {
        val entity = data.toEntity()
        viewModelScope.launch {
            dietRepository.updateDietLog(entity)
            Log.d("RoomTest", "로컬 저장됨: $entity")
        }
    }

    // 식재료 정보 불러오기
    fun loadIngredients() {
        viewModelScope.launch {
            ingredientList.value = ingredientRepository.fetchAndSaveIngredients()
        }
    }
}