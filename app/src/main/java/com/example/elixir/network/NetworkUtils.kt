package com.example.elixir.network

import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File

fun String.toMultipart(partName: String): MultipartBody.Part {
    val file = File(this)
    val requestBody = file.asRequestBody("image/*".toMediaTypeOrNull())
    return MultipartBody.Part.createFormData(partName, file.name, requestBody)
}
