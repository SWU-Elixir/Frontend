package com.example.elixir

import android.util.Log
import com.example.elixir.login.LoginService
import com.example.elixir.ingredient.network.IngredientApi
import com.example.elixir.challenge.network.ChallengeApi
import com.example.elixir.member.network.MemberApi
import com.example.elixir.chatbot.ChatApi
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {
    // 서버 주소
    private const val BASE_URL = "https://port-0-elixir-backend-g0424l70py8py.gksl2.cloudtype.app/"
    private var authToken: String? = "" // Bearer 토큰을 저장할 변수

    fun setAuthToken(token: String) {
        authToken = token
        Log.d("RetrofitClient", "setAuthToken: $authToken")
    }

    private val authInterceptor = object : Interceptor {
        override fun intercept(chain: Interceptor.Chain): Response {
            val originalRequest = chain.request()
            val builder = originalRequest.newBuilder()

            // Bearer 토큰이 있다면 Authorization 헤더에 추가
            authToken?.let {
                builder.header("Authorization", "Bearer $it")
            }

            val newRequest = builder.build()
            return chain.proceed(newRequest)
        }
    }

    private val refreshInterceptor = object : Interceptor {
        override fun intercept(chain: Interceptor.Chain): Response {
            val originalRequest = chain.request()
            val response = chain.proceed(originalRequest)

            // 401 Unauthorized(토큰 만료) 감지
            if (response.code == 401) {
                // 토큰 재발급 시도 (동기)
                val refreshResponse = RetrofitClient.instance.refreshToken().execute()
                if (refreshResponse.isSuccessful) {
                    val newToken = refreshResponse.body()?.data?.accessToken
                    if (!newToken.isNullOrEmpty()) {
                        setAuthToken(newToken) // 토큰 저장

                        // 원래 요청을 새 토큰으로 재시도
                        val newRequest = originalRequest.newBuilder()
                            .header("Authorization", "Bearer $newToken")
                            .build()
                        response.close() // 기존 응답 닫기
                        return chain.proceed(newRequest)
                    }
                }
            }
            return response
        }
    }

    private val client: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .addInterceptor(authInterceptor)
            .addInterceptor(refreshInterceptor)
            .addInterceptor(HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            }).build()
    }

    val instance: LoginService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(LoginService::class.java)
    }

    val instanceIngredientApi: IngredientApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(IngredientApi::class.java)
    }

    val instanceChallengeApi: ChallengeApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ChallengeApi::class.java)
    }

    val instanceMemberApi: MemberApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(MemberApi::class.java)
    }

    val instanceChatApi: ChatApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ChatApi::class.java)
    }
}