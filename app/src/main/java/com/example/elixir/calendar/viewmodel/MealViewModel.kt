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
import com.example.elixir.ingredient.data.IngredientEntity
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

    private val _allDietLogs = MutableLiveData<List<MealDto>>()
    val allDietLogs: LiveData<List<MealDto>> = _allDietLogs

    private val _monthlyScore = MutableLiveData<GetScoreResponse>()
    val monthlyScore: LiveData<GetScoreResponse> = _monthlyScore

    private val _uploadResult = MutableLiveData<Result<String>>() // 성공 시 이미지 URL 반환
    val uploadResult: LiveData<Result<String>> get() = _uploadResult

    private val _deleteResult = MutableLiveData<Result<Boolean>>()
    val deleteResult: LiveData<Result<Boolean>> get() = _deleteResult

    val ingredientList = MutableLiveData<List<IngredientEntity>>()


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
            try {
                dietRepository.insertDietLog(entity)
                Log.d("RoomTest", "로컬 저장됨: $entity")
            } catch (e: Exception) {
                Log.e("MealViewModel", "로컬 DB 저장 실패: ${e.message}", e)
            }
        }
    }

    // 로컬 DB + 서버 업로드
    fun saveAndUpload(data: DietLogData, imageFile: File) {
        viewModelScope.launch {
            try {
                // 1. 이미지 업로드 및 서버로부터 이미지 URL 받기
                // 이 시점의 data는 로컬 경로(android.resource:// 또는 content://)를 가지고 있을 수 있음
                // dietRepository.uploadDietLog는 DietLogEntity와 File을 받으므로,
                // data.toEntity()를 넘겨주면서 이미지 파일을 함께 보냄.
                val uploadImageResponse: GetMealResponse? = dietRepository.uploadDietLog(data.toEntity(), imageFile)
                val uploadedImageUrl: String? = uploadImageResponse?.data?.imageUrl // **여기서 명확히 선언**

                if (uploadedImageUrl.isNullOrEmpty()) {
                    // 이미지 URL이 없으면 실패 처리
                    _uploadResult.postValue(Result.failure(Exception("이미지 업로드 후 URL을 받지 못했습니다.")))
                    return@launch
                }

                // 2. 서버에서 받은 이미지 URL로 DietLogData 업데이트
                val dietLogDataWithImageUrl = data.copy(dietImg = uploadedImageUrl)

                // 3. Room DB 저장 (업데이트된 이미지 URL 포함)
                dietRepository.insertDietLog(dietLogDataWithImageUrl.toEntity())
                Log.d("Upload", "로컬 DB에 서버 이미지 URL로 저장 완료.")

                // 4. Fragment에 성공과 최종 이미지 URL을 전달
                Log.d("Upload", "서버 업로드 성공, imageUrl=$uploadedImageUrl")
                _uploadResult.postValue(Result.success(uploadedImageUrl))

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

    // 식단 기록 수정 (이미지 포함)
    fun updateDietLog(data: DietLogData, imageFile: File?) {
        viewModelScope.launch {
            try {
                // 수정 API를 직접 호출 (등록 API 호출하지 않음)
                val updateResponse = dietRepository.updateDietLog(data.toEntity(), imageFile)
                val updatedMealDto = updateResponse?.data // 서버에서 반환된 최종 MealDto

                if (updatedMealDto != null) {
                    // Room DB 업데이트 (서버에서 받은 최신 정보로)
                    dietRepository.updateDietLog(updatedMealDto.toEntity())
                    Log.d("Update", "식단 기록 수정 성공 및 로컬 DB 업데이트 완료")

                    // 성공과 최종 이미지 URL을 전달
                    _uploadResult.postValue(Result.success(updatedMealDto.imageUrl))

                    if (imageFile != null) {
                        Log.d("Update", "새 이미지와 함께 수정 완료. URL: ${updatedMealDto.imageUrl}")
                    } else {
                        Log.d("Update", "기존 이미지로 수정 완료. URL: ${updatedMealDto.imageUrl}")
                    }
                } else {
                    Log.e("Update", "식단 기록 수정 실패: 서버 응답 없음")
                    _uploadResult.postValue(Result.failure(Exception("식단 기록 수정 실패: 서버 응답 없음")))
                }
            } catch (e: CancellationException) {
                Log.w("Update", "코루틴 취소됨: ${e.message}")
                _uploadResult.postValue(Result.failure(e))
            } catch (e: Exception) {
                Log.e("Update", "식단 기록 수정 중 예외 발생: ${e.message}", e)
                _uploadResult.postValue(Result.failure(e))
            }
        }
    }


    // 로컬 DB에만 업데이트
    fun updateToLocalDB(data: DietLogData) {
        val entity = data.toEntity()
        viewModelScope.launch {
            try {
                dietRepository.updateDietLog(entity)
                Log.d("RoomTest", "로컬 DB 업데이트됨: $entity")
            } catch (e: Exception) {
                Log.e("MealViewModel", "로컬 DB 업데이트 실패: ${e.message}", e)
            }
        }
    }

    // 식재료 정보 불러오기
    fun loadIngredients() {

        viewModelScope.launch {
            try {
                ingredientList.value = ingredientRepository.fetchAndSaveIngredients()
            } catch (e: Exception) {
                Log.e("MealViewModel", "식재료 로드 실패: ${e.message}", e)
                ingredientList.value = emptyList() // 실패 시 빈 리스트
            }
        }
    }

    // 전체 식단 기록 목록 가져오기 (최근 N일)
    fun get30daysDietLogs(days: Int = 30) {
        viewModelScope.launch {
            val result = dietRepository.getDietLogsForLastDays(days)
            _dailyDietLogs.value = result ?: emptyList()
        }
    }

    // 전체 식단 기록 목록 가져오기
    fun getAllDietLogs(days: Int = 30) {
        viewModelScope.launch {
            val result = dietRepository.getAllDietLogs()
            _dailyDietLogs.value = result ?: emptyList()
        }
    }


}