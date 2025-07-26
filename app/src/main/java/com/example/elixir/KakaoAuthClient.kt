package com.example.elixir

import android.content.Context
import com.kakao.sdk.user.UserApiClient
import javax.inject.Inject

class KakaoAuthClient @Inject constructor() {
    fun loginWithKakaoTalk(
        context: Context,
        onSuccess: (String) -> Unit,
        onFailure: (Throwable) -> Unit,
    ) {
        UserApiClient.instance.loginWithKakaoTalk(context) { token, error ->
            if (error != null) {
                onFailure(error)
            } else if (token != null) {
                onSuccess(token.accessToken)
            }
        }
    }
}