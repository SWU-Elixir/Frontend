package com.example.elixir.calendar.network.db

import com.example.elixir.calendar.network.DietLogApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MultipartBody
import okhttp3.RequestBody

class DietLogRemoteDataSource (private val api: DietLogApi) {
    suspend fun uploadDietLog(dtoRequestBody: RequestBody, imagePart: MultipartBody.Part) {
        withContext(Dispatchers.IO) {
            api.uploadDietLog(dtoRequestBody, imagePart)
        }
    }
}