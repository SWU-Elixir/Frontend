package com.example.elixir

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import com.example.elixir.databinding.ActivityLoginBinding
import java.security.MessageDigest


class LoginActivity : AppCompatActivity() {
    // 선언부
    private lateinit var loginBinding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        // 화면 전체 사용, 상태 바를 투명하게
        enableEdgeToEdge()
        WindowCompat.setDecorFitsSystemWindows(window, false)

        super.onCreate(savedInstanceState)

        // 바인딩 정의
        loginBinding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(loginBinding.root)

        // 로그인 버튼을 누르면 홈 액티비티로
        loginBinding.btnLogin.setOnClickListener {
            // bring enter id, pw
            val id = loginBinding.enterId.text.toString().trim()
            val pw = loginBinding.enterPw.text.toString().trim()
            val hashedPw = hashPassword(pw)
        }

        // 회원가입 버튼을 누르면 등록 액티비티로
        loginBinding.btnSignup.setOnClickListener {
            val intent = Intent(this, ToolbarActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
            intent.putExtra("mode", 1)
            startActivity(intent)
        }
    }

    private fun hashPassword(input: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(input.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }
}