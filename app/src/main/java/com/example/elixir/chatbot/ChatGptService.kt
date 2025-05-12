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


class ChatGptService(private val context: Context) {
    private val client: OkHttpClient
    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()
    private val apiKey = BuildConfig.CHATGPT_API_KEY
    
    // 마지막 요청 시간을 추적
    private var lastRequestTime = 0L
    private val minRequestInterval = 2000L // 최소 2초 간격

    init {
        client = OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    suspend fun sendMessage(message: String): String = withContext(Dispatchers.IO) {
        if (apiKey.isBlank()) {
            throw IOException("API 키가 설정되지 않았습니다. local.properties 파일에 CHATGPT_API_KEY를 추가해주세요.")
        }

        try {
            // 요청 간격 조절
            val currentTime = System.currentTimeMillis()
            val timeSinceLastRequest = currentTime - lastRequestTime
            if (timeSinceLastRequest < minRequestInterval) {
                delay(minRequestInterval - timeSinceLastRequest)
            }
            lastRequestTime = System.currentTimeMillis()

            val messages = JSONArray().apply {
                put(JSONObject().apply {
                    put("role", "system")
                    put("content", "당신은 건강한 식단과 영양에 대해 전문적인 조언을 제공하는 AI 어시스턴트입니다.")
                })
                put(JSONObject().apply {
                    put("role", "user")
                    put("content", message)
                })
            }

            val jsonBody = JSONObject().apply {
                put("model", "gpt-3.5-turbo")
                put("messages", messages)
                put("temperature", 0.7)
            }.toString()

            val request = Request.Builder()
                .url("https://api.openai.com/v1/chat/completions")
                .addHeader("Authorization", "Bearer $apiKey")
                .addHeader("Content-Type", "application/json")
                .post(jsonBody.toRequestBody(jsonMediaType))
                .build()

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

                val responseBody = response.body?.string()
                    ?: throw IOException("응답 본문이 비어있습니다")

                val jsonResponse = JSONObject(responseBody)
                val choices = jsonResponse.getJSONArray("choices")
                if (choices.length() > 0) {
                    val firstChoice = choices.getJSONObject(0)
                    val message = firstChoice.getJSONObject("message")
                    message.getString("content")
                } else {
                    "죄송합니다. 응답을 생성하는데 실패했습니다."
                }
            }
        } catch (e: Exception) {
            Log.e("ChatBot", "ChatGPT API Error", e)
            when (e) {
                is IOException -> e.message ?: "API 호출 중 오류가 발생했습니다."
                else -> "죄송합니다. 오류가 발생했습니다: ${e.message}"
            }
        }
    }
} 