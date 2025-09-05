package com.example.elixir.login.network

import android.content.Context
import android.util.Log
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import com.example.elixir.BuildConfig
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class GoogleSignInHelper(
    private val context: Context
) {
    private val credentialManager: CredentialManager = CredentialManager.create(context)

    private val googleIdOption = GetGoogleIdOption.Builder()
        .setServerClientId(BuildConfig.GOOGLE_CLIENT_KEY)
        .setFilterByAuthorizedAccounts(false)
        .setAutoSelectEnabled(true)
        .build()

    private val request = GetCredentialRequest.Builder()
        .addCredentialOption(googleIdOption)
        .build()

    private val coroutineScope = CoroutineScope(Dispatchers.Main)


    fun startGoogleLogin(
        onSuccess: (googleIdToken: String, email: String?) -> Unit,
        onFailure: (String) -> Unit
    ) {
        coroutineScope.launch {
            try {
                val result = credentialManager.getCredential(
                    context = context,
                    request = request
                )

                Log.d("GoogleSignInHelper", "Credential fetch 성공: $result")

                val credential = result.credential
                Log.d("GoogleSignInHelper", "Credential 타입: ${credential::class.java.name}")

                if (credential is CustomCredential && credential.type == "com.google.android.libraries.identity.googleid.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL") {
                    val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                    val idToken = googleIdTokenCredential.idToken     // ID 토큰 문자열
                    val email = googleIdTokenCredential.id            // 이메일

                    // 👇👇 여기에 로그를 찍으면 된다!
                    Log.d("GoogleSignInHelper", "우회 추출된 Google ID Token: $idToken")
                    Log.d("GoogleSignInHelper", "우회 추출된 Google Email: $email")
                    for (key in credential.data.keySet()) {
                        Log.d("GoogleSignInHelper", "CustomCredential DATA KEY: $key, value: ${credential.data.get(key)}")
                    }

                    // 토큰 값 전달
                    onSuccess(idToken ?: "", email)
                }
                else if (credential is androidx.credentials.CustomCredential) {
                    val type = credential.type
                    Log.e("GoogleSignInHelper", "CustomCredential type: $type")
                    Log.e("GoogleSignInHelper", "CustomCredential data: ${credential.data}")
                    onFailure("알 수 없는 종류의 인증 결과가 반환되었습니다. type=$type")
                } else {
                    Log.e("GoogleSignInHelper", "예상하지 못한 Credential 객체: $credential")
                    onFailure("구글 인증에 실패하였습니다. 다시 시도해주세요.")
                }

            } catch (e: Exception) {
                Log.e("GoogleSignInHelper", "예외 발생: ${e::class.java.name}")
                Log.e("GoogleSignInHelper", "메시지: ${e.message}")
                Log.e("GoogleSignInHelper", "스택트레이스: \n${Log.getStackTraceString(e)}")

                when {
                    e.message?.contains("User cancelled the selector", ignoreCase = true) == true -> {
                        onFailure("사용자가 구글 로그인을 취소했습니다.")
                    }
                    else -> {
                        onFailure("Google 로그인 실패: ${e.message}")
                    }
                }
            }
        }
    }

}
