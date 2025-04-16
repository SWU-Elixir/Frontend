package com.example.elixir

import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.fragment.app.Fragment
import com.example.elixir.databinding.ActivityToolbarBinding
import com.example.elixir.signup.CreateAccountFragment

class ToolbarActivity : AppCompatActivity() {
    // 선언부
    private lateinit var toolBinding: ActivityToolbarBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        // 화면 전체 사용, 상태 바를 투명하게 하기
        enableEdgeToEdge()
        WindowCompat.setDecorFitsSystemWindows(window, false)

        super.onCreate(savedInstanceState)
        // 바인딩 정의
        toolBinding = ActivityToolbarBinding.inflate(layoutInflater)
        setContentView(toolBinding.root)

        // 전 액티비티에서 정보 불러오기
        // 어떤 모드인지 확인하고, 맞는 화면 띄워주기
        val mode = intent.getIntExtra("mode", 0)
        when(mode) {
            // 회원가입 모드
            1 -> {
                // 툴바의 제목, 더보기 버튼 안보이게, 작동 x
                toolBinding.title.visibility = View.INVISIBLE
                toolBinding.btnMore.visibility = View.INVISIBLE

                // 뒤로가기 버튼을 누르면 로그인 페이지로 돌아가기
                // 돌아가기 전 다이얼로그 띄우기
                toolBinding.btnBack.setOnClickListener {
                    AlertExitDialogFragment(this).show()
                }

                // 계정 생성 프래그먼트 띄워주기
                setFragment(CreateAccountFragment())
            }
        }
    }

    private fun setFragment(frag: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_registration, frag)
            .commit()
    }
}