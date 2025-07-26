package com.example.elixir

import android.app.Application
import com.jakewharton.threetenabp.AndroidThreeTen
import com.kakao.sdk.common.KakaoSdk
import com.kakao.sdk.user.UserApiClient
import com.navercorp.nid.NaverIdLoginSDK

class Elixir : Application() {
    override fun onCreate() {
        super.onCreate()

        // 날짜 초기화
        AndroidThreeTen.init(this)

        // 카카오 로그인
        KakaoSdk.init(this, BuildConfig.KAKAO_NATIVE_KEY) // 추가
        NaverIdLoginSDK.initialize(this, BuildConfig.NAVER_CLIENT_KEY, BuildConfig.NAVER_CLIENT_SECRET_KEY, "com.example.elixir")
    }
}