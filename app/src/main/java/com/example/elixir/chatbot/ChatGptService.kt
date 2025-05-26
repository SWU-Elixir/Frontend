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


// ChatGPT API와 통신하는 서비스 클래스
class ChatGptService(private val context: Context) {
    private val client: OkHttpClient
    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()
    private val apiKey = BuildConfig.CHATGPT_API_KEY
    
    // 마지막 요청 시간을 추적
    private var lastRequestTime = 0L
    // 최소 요청 간격 (2초)
    private val minRequestInterval = 2000L // 최소 2초 간격

    init {
        // OkHttpClient 설정 (타임아웃 30초)
        client = OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    // ChatGPT에 메시지를 보내고 응답을 받는 suspend 함수
    suspend fun sendMessage(message: String): ChatResponseDto = withContext(Dispatchers.IO) {
        // API 키가 설정되지 않았을 경우 예외 발생
        if (apiKey.isBlank()) {
            throw IOException("API 키가 설정되지 않았습니다. local.properties 파일에 CHATGPT_API_KEY를 추가해주세요.")
        }

        try {
            // 요청 간격 조절을 위한 로직
            val currentTime = System.currentTimeMillis()
            val timeSinceLastRequest = currentTime - lastRequestTime
            // 최소 간격보다 짧으면 대기
            if (timeSinceLastRequest < minRequestInterval) {
                delay(minRequestInterval - timeSinceLastRequest)
            }
            // 마지막 요청 시간 업데이트
            lastRequestTime = System.currentTimeMillis()

            // 메시지 목록 (system 메시지와 user 메시지 포함)
            val messages = JSONArray().apply {
                // system 메시지: AI의 역할 정의
                put(JSONObject().apply {
                    put("role", "system")
                    put("content", "당신은 건강한 식단과 영양에 대해 전문적인 조언을 제공하는 AI 어시스턴트입니다.")
                })
                // user 메시지: 사용자의 입력
                put(JSONObject().apply {
                    put("role", "user")
                    put("content", message)
                })
            }

            // 요청 본문 (JSON 형식)
            val jsonBody = JSONObject().apply {
                put("model", "gpt-3.5-turbo") // 사용할 모델 지정
                put("messages", messages) // 메시지 목록 추가
                put("temperature", 0.7) // 응답의 다양성 조절
            }.toString()

            // HTTP 요청 객체 생성
            val request = Request.Builder()
                .url("https://api.openai.com/v1/chat/completions")
                .addHeader("Authorization", "Bearer $apiKey")
                .addHeader("Content-Type", "application/json")
                .post(jsonBody.toRequestBody(jsonMediaType))
                .build()

            // 비동기 HTTP 호출 및 응답 처리
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    val errorBody = response.body?.string()
                    Log.e("ChatBot", "API Error: ${response.code} - $errorBody")
                    when (response.code) {
                        401 -> throw IOException("API 키가 유효하지 않습니다. API 키를 확인해주세요.")
                        429 -> {
                            // rate limit 초과 시 5초 대기 후 재시도
                            delay(5000)
                            throw IOException("API 호출 한도를 초과했습니다. 잠시 후 다시 시도해주세요.")
                        }
                        else -> throw IOException("API 호출 실패: ${response.code} - $errorBody")
                    }
                }

                // 응답 본문 가져오기
                val responseBody = response.body?.string()
                    ?: throw IOException("응답 본문이 비어있습니다")

                // JSON 응답 파싱
                val jsonResponse = JSONObject(responseBody)
                val choices = jsonResponse.getJSONArray("choices")
                // 응답에서 첫 번째 선택지의 content 추출
                if (choices.length() > 0) {
                    val firstChoice = choices.getJSONObject(0)
                    val messageContent = firstChoice.getJSONObject("message").getString("content")
                    // Assuming chatSessionId is not in the response JSON based on typical OpenAI responses
                    ChatResponseDto(chatSessionId = null, message = messageContent)
                } else {
                    // 선택지가 없을 경우 기본 메시지 반환
                    ChatResponseDto(chatSessionId = null, message = "죄송합니다. 응답을 생성하는데 실패했습니다.")
                }
            }
        } catch (e: Exception) {
            // 예외 발생 시 로그 출력 및 오류 메시지 반환
            Log.e("ChatBot", "ChatGPT API Error", e)
            val errorMessage = when (e) {
                is IOException -> e.message ?: "API 호출 중 오류가 발생했습니다."
                else -> "죄송합니다. 오류가 발생했습니다: ${e.message}"
            }
            // 에러 메시지를 담은 ChatResponseDto 반환
            ChatResponseDto(chatSessionId = null, message = errorMessage)
        }
    }
} 