package com.example.elixir.signup

import com.example.elixir.login.LoginService
import com.example.elixir.Ingredient.IngredientApi
import com.example.elixir.challenge.ChallengeApi
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    // 서버 주소
    private const val BASE_URL = "https://port-0-elixir-backend-g0424l70py8py.gksl2.cloudtype.app/"
    private var authToken: String? = null // Bearer 토큰을 저장할 변수

    fun setAuthToken(token: String) {
        authToken = token
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

    private val client = OkHttpClient.Builder()
        .addInterceptor(authInterceptor)
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }).build()

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
}