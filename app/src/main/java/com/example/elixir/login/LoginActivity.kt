package com.example.elixir.login

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat
import com.example.elixir.HomeActivity
import com.example.elixir.RetrofitClient
import com.example.elixir.ToolbarActivity
import com.example.elixir.databinding.ActivityLoginBinding
import retrofit2.Call

class LoginActivity : AppCompatActivity() {
    // 선언부
    private lateinit var loginBinding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        // 화면 전체 사용, 상태 바를 투명하게
        enableEdgeToEdge()
        WindowCompat.setDecorFitsSystemWindows(window, false)

        super.onCreate(savedInstanceState)
        installSplashScreen()

        // 앱 시작 시 저장된 토큰 불러와서 RetrofitClient에 세팅
        loadToken()?.let { token ->
            RetrofitClient.setAuthToken(token)
        }

        // 초기화
        // 바인딩 정의
        loginBinding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(loginBinding.root)

        // 에러 메시지는 기본적으로 숨김
        loginBinding.errorLogin.visibility = View.GONE

        // 로그인 버튼 클릭
        loginBinding.btnLogin.setOnClickListener {
            val email = loginBinding.enterEmail.text.toString()
            val password = loginBinding.enterPw.text.toString()

            if (email.isNotEmpty() && password.isNotEmpty()) {
                login(email, password)
            } else {
                loginBinding.errorLogin.text = "이메일과 비밀번호를 입력해주세요."
                loginBinding.errorLogin.visibility = View.VISIBLE
            }
        }

        // 회원가입 버튼을 누르면 등록 액티비티로
        loginBinding.btnSignup.setOnClickListener {
            val intent = Intent(this, ToolbarActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
            intent.putExtra("mode", 1)
            startActivity(intent)
        }

        loginBinding.findPw.setOnClickListener {
            val intent = Intent(this, ToolbarActivity::class.java).apply {
                putExtra("mode", 12)
            }
            startActivity(intent)
        }
    }

    // 로그인 검증
    private fun checkLogin(email: String, password: String): Boolean {
        return email == "example@example.com" && password == "123456"
    }

    // 자동 로그인 저장
    private fun saveAutoLogin(email: String, password: String) {
        val prefs = getSharedPreferences("autoLoginPrefs", MODE_PRIVATE)
        prefs.edit()
            .putString("email", email)
            .putString("password", password)
            .apply()
    }

    //login(email, password)

    // 로그인 성공 시 현재 액티비티를 종료하고 홈 액티비티를 실행
//    if (checkLogin(email, password)) {
//        Toast.makeText(this, "로그인 성공!", Toast.LENGTH_SHORT).show()
//        val intent = Intent(this, HomeActivity::class.java)
//        startActivity(intent)
//        finish()
//    }
//
//    else {
//        loginBinding.errorLogin.text = "이메일이나 비밀번호가 일치하지 않습니다."

    // 로그인 성공 시
    private fun login(email: String, password: String) {
        val request = LoginRequest(email, password)

        RetrofitClient.instance.login(request).enqueue(object : retrofit2.Callback<LoginResponse> {
            override fun onResponse(
                call: Call<LoginResponse>,
                response: retrofit2.Response<LoginResponse>
            ) {
                if (response.isSuccessful) {
                    val result = response.body()
                    Log.d("LoginActivity", "API Response: $result")

                    if (result?.status == 200) {
                        // 토큰 저장 - data.accessToken으로 접근
                        result.data?.accessToken?.let { token ->
                            Log.d("LoginActivity", "Access Token: $token")
                            RetrofitClient.setAuthToken(token)
                            saveToken(token)

                            // 저장 확인
                            val savedToken = loadToken()
                            Log.d("LoginActivity", "Saved Token: $savedToken")
                        } ?: run {
                            Log.e("LoginActivity", "Access token is null")
                        }

                        result.data?.refreshToken?.let { refreshToken ->
                            saveRefreshToken(refreshToken)
                            Log.d("LoginActivity", "Refresh Token saved")
                        }

                        // 자동 로그인 정보 저장
                        saveAutoLogin(email, password)
                        Toast.makeText(this@LoginActivity, "로그인 성공!", Toast.LENGTH_SHORT).show()
                        val intent = Intent(this@LoginActivity, HomeActivity::class.java)
                        startActivity(intent)
                        finish()
                    } else {
                        loginBinding.errorLogin.text = result?.message ?: "로그인에 실패했습니다."
                        loginBinding.errorLogin.visibility = View.VISIBLE
                    }
                } else {
                    loginBinding.errorLogin.text = "서버 오류가 발생했습니다."
                    loginBinding.errorLogin.visibility = View.VISIBLE
                }
            }

            override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                Log.e("LoginActivity", "Network error: ${t.message}")
                loginBinding.errorLogin.text = "네트워크 오류가 발생했습니다."
                loginBinding.errorLogin.visibility = View.VISIBLE
            }
        })
    }

    // Refresh Token 저장 메서드 추가
    private fun saveRefreshToken(refreshToken: String) {
        val prefs = getSharedPreferences("authPrefs", MODE_PRIVATE)
        prefs.edit().putString("refreshToken", refreshToken).apply()
    }

    // 토큰을 SharedPreferences에 저장
    private fun saveToken(token: String) {
        Log.d("LoginActivity", "Saving token: $token")
        val prefs = getSharedPreferences("authPrefs", MODE_PRIVATE)
        val success = prefs.edit()
            .putString("accessToken", token)
            .commit() // apply() 대신 commit()으로 즉시 저장 확인
        Log.d("LoginActivity", "Token save success: $success")
    }

    private fun loadToken(): String? {
        val prefs = getSharedPreferences("authPrefs", MODE_PRIVATE)
        val token = prefs.getString("accessToken", null)
        Log.d("LoginActivity", "Loaded token: $token")
        return token
    }

}