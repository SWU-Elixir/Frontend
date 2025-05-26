package com.example.elixir.chatbot

import android.content.Context
import android.util.Log
import com.example.elixir.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.TimeUnit
import com.example.elixir.RetrofitClient
import retrofit2.HttpException


// ChatGPT API와 통신하는 서비스 클래스
class ChatGptService {
    private val client: OkHttpClient

    init {
        // OkHttpClient 설정 (타임아웃 30초)
        client = OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    // /api/chat 엔드포인트로 메시지 전송 (서버 연동)
    suspend fun sendChatRequest(request: ChatRequestDto): ChatResponseDto = withContext(Dispatchers.IO) {
        try {
            val api = RetrofitClient.instanceChatApi
            val response = api.sendChat(request)
            if (response.isSuccessful) {
                response.body()?.data ?: ChatResponseDto(message = "응답이 비어 있습니다.")
            } else {
                ChatResponseDto(message = "서버 오류: ${response.code()}")
            }
        } catch (e: HttpException) {
            ChatResponseDto(message = "서버 오류: ${e.code()}")
        } catch (e: Exception) {
            ChatResponseDto(message = "네트워크 오류: ${e.localizedMessage}")
        }
    }
} 