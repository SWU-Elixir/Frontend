package com.example.elixir.login

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import com.example.elixir.signup.RetrofitClient
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
            val autoLogin = loginBinding.autoLogin.isChecked

            //login(email, password)

            // 로그인 성공 시
            if (checkLogin(email, password)) {
                Toast.makeText(this, "로그인 성공!", Toast.LENGTH_SHORT).show()

                if (autoLogin) {
                    saveAutoLogin(email, password)
                }

                /* CalenderActivity로 이동
                startActivity(Intent(this, CalendarActivity::class.java))
                finish()*/
            }

            else {
                loginBinding.errorLogin.text = "이메일이나 비밀번호가 일치하지 않습니다."
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

    // 로그인 성공 시
    private fun login(email: String, password: String){
        val request = LoginRequest(email, password)

        RetrofitClient.instance.login(request).enqueue(object : retrofit2.Callback<LoginResponse> {
            override fun onResponse(
                call: Call<LoginResponse>,
                response: retrofit2.Response<LoginResponse>
            ) {
                if (response.isSuccessful) {
                    val result = response.body()
                    Toast.makeText(
                        this@LoginActivity,
                        "Login success : status: ${result?.status}, message: ${result?.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    Toast.makeText(
                        this@LoginActivity,
                        "LoginError : Code: ${response.code()} - ${response.errorBody()?.string()}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                Toast.makeText(this@LoginActivity, "LoginFailure : Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}