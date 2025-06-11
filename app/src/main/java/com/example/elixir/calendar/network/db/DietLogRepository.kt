package com.example.elixir.calendar.network.db

import android.util.Log
import com.example.elixir.calendar.data.DietLogEntity
import com.example.elixir.calendar.data.MealDto
import com.example.elixir.calendar.data.ScoreData
import com.example.elixir.calendar.data.toDto
import com.example.elixir.calendar.network.DietApi
import com.example.elixir.calendar.network.response.GetMealResponse
import com.google.gson.Gson
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File

class DietLogRepository (private val dietLogDao: DietLogDao, private val dietApi: DietApi) {

    // 식단 기록을 DB에 업로드하는 메서드
    suspend fun insertDietLog(dietLogEntity: DietLogEntity) {
        dietLogDao.insertDietLog(dietLogEntity)
    }

    suspend fun updateDietLog(dietLogEntity: DietLogEntity) {
        dietLogDao.updateDietLog(dietLogEntity)
    }

    // 날짜 별로 식단 기록을 저장
    suspend fun getDietLogsByDate(date: String): List<MealDto>? {
        return try {
            val response = dietApi.getDietLogsByDate(date)
            if (response.isSuccessful) response.body()?.data else null
        } catch (e: Exception) {
            Log.e("DietRepo", "getDietLogsByDate 오류: ${e.message}")
            null
        }
    }

    // 서버
    // 식단 기록 등록
    suspend fun uploadDietLog(dietLog: DietLogEntity, imageFile: File): GetMealResponse? {
        return try {
            val dto = dietLog.toDto()
            Log.d("DietLogRepository", "type: ${dto.type}")
            val json = Gson().toJson(dto)
            val dtoRequestBody = json.toRequestBody("application/json".toMediaTypeOrNull())

            val imageRequestBody = imageFile.asRequestBody("image/*".toMediaTypeOrNull())
            val imagePart =
                MultipartBody.Part.createFormData("image", imageFile.name, imageRequestBody)

            val response = dietApi.uploadDietLog(dtoRequestBody, imagePart)
            if (response.isSuccessful) {
                Log.d("DietLogRepository", "업로드 성공!: ${response.body()?.toString()}")
                val result = response.body() // Ensure type safety
                result
            } else {
                Log.e("DietLogRepository", "Upload failed: ${response.errorBody()?.string()}")
                null
            }
        } catch (e: Exception) {
            Log.e("DietLogRepository", "Exception: ${e.message}")
            null
        }
    }

    suspend fun updateDietLog(dietLog: DietLogEntity, imageFile: File?): GetMealResponse? {
        val dto = dietLog.toDto()
        val json = Gson().toJson(dto)
        val dtoRequestBody = json.toRequestBody("application/json".toMediaTypeOrNull())

        val dietLogId = dietLog.id // 혹은 dietLog.dietLogId 등 실제 PK 필드명에 맞게

        val response = if (imageFile != null && imageFile.exists()) {
            val imageRequestBody = imageFile.asRequestBody("image/*".toMediaTypeOrNull())
            val imagePart = MultipartBody.Part.createFormData("image", imageFile.name, imageRequestBody)
            dietApi.updateDietLog(dietLogId, dtoRequestBody, imagePart)
        } else {
            // 이미지 없이 PATCH (API에서 image 파트가 nullable이어야 함)
            dietApi.updateDietLog(dietLogId, dtoRequestBody, null)
        }

        return if (response.isSuccessful) response.body() else null
    }


    // 식단 기록 삭제
    suspend fun deleteDietLog(dietLogId: Int): Boolean {
        return try {
            val response = dietApi.deleteDietLog(dietLogId)
            if (response.isSuccessful) {
                Log.d("DietLogRepository", "삭제 성공")
                true
            } else {
                Log.e("DietLogRepository", "삭제 실패: ${response.errorBody()?.string()}")
                false
            }
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    // 멤버 아이디로 식단 가져오기
    suspend fun getDietLogById(dietLogId: Int): MealDto? {
        return try {
            val response = dietApi.getDietLogById(dietLogId)
            if (response.isSuccessful) response.body()?.data else null
        } catch (e: Exception) {
            Log.e("DietRepo", "getDietLogById 오류: ${e.message}")
            null
        }
    }

    // 월별 점수 가져오기
    suspend fun getMonthlyScore(year: Int, month: Int): List<ScoreData>? {
        return try {
            val response = dietApi.getMonthlyScore(year, month)
            if (response.isSuccessful) response.body()?.data else null
        } catch (e: Exception) {
            Log.e("DietRepo", "getMonthlyScore 오류: ${e.message}")
            null
        }
    }
}