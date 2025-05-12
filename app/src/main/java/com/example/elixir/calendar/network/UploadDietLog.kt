package com.example.elixir.calendar.network

import com.example.elixir.calendar.data.toDto
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File

suspend fun uploadDietLog(
    dietLogDao: DietLogDao,
    api: DietLogApi,
    dietLogId: Int,
    imageFile: File
) {
    // Room DB에서 데이터 조회
    val dietLogEntity = dietLogDao.getDietLogById(dietLogId)
    val dietLogDto = dietLogEntity.toDto()

    // DTO를 RequestBody로 변환
    val dtoRequestBody = RequestBody.create(
        "application/json".toMediaTypeOrNull(),
        Gson().toJson(dietLogDto)
    )

    // 이미지 파일 준비
    val imageRequestBody = imageFile.asRequestBody("image/png".toMediaTypeOrNull())
    val imagePart = MultipartBody.Part.createFormData("image", imageFile.name, imageRequestBody)

    // 서버로 전송
    withContext(Dispatchers.IO) {
        api.uploadDietLog(dtoRequestBody, imagePart)
    }
}