package com.example.elixir

import android.annotation.SuppressLint
import android.app.Activity
import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.fragment.app.Fragment
import com.example.elixir.databinding.ActivityToolbarBinding
import com.example.elixir.dialog.AlertExitDialog
import com.example.elixir.dietlog.DietLogFragment
import com.example.elixir.recipe.RecipeData
import com.example.elixir.recipe.RecipeFragment
import com.example.elixir.recipe.RecipeLogFragment
import com.example.elixir.signup.CreateAccountFragment

open class ToolbarActivity : AppCompatActivity() {
    // 선언부
    protected lateinit var toolBinding: ActivityToolbarBinding

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        // 화면 전체 사용, 상태 바를 투명하게 하기
        enableEdgeToEdge()
        WindowCompat.setDecorFitsSystemWindows(window, false)

        super.onCreate(savedInstanceState)
        // 바인딩 정의
        toolBinding = ActivityToolbarBinding.inflate(layoutInflater)
        setContentView(toolBinding.root)

        val recipeData = intent.getParcelableExtra<RecipeData>("recipeData")

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
                    AlertExitDialog(this).show()
                }

                // 계정 생성 프래그먼트 띄워주기
                setFragment(CreateAccountFragment())
            }

            // 식단 기록 모드
            2 -> {
                // 더보기 버튼만 숨기기
                toolBinding.btnMore.visibility = View.INVISIBLE

                // 날짜 받아오기
                val year = intent.getIntExtra("year", -1)
                val month = intent.getIntExtra("month", -1)
                val day = intent.getIntExtra("day", -1)

                // 타이틀: yyyy년 m월 d일로
                toolBinding.title.text = "${year}년 ${month}월 ${day}일"

                // 뒤로가기 버튼을 누르면 캘린더 페이지로 돌아가기
                // 돌아가기 전 다이얼로그 띄우기
                toolBinding.btnBack.setOnClickListener {
                    AlertExitDialog(this).show()
                }

                // 식단 기록 프래그먼트 띄우기
                setFragment(DietLogFragment())
            }

            // 레시피 기록 모드
            3 -> {
                // 툴바의 제목, 더보기 버튼 안보이게, 작동 x
                toolBinding.title.visibility = View.INVISIBLE
                toolBinding.btnMore.visibility = View.INVISIBLE

                // 뒤로가기 버튼을 누르면 레시피 리스트 페이지로 이동
                toolBinding.btnBack.setOnClickListener {
                    AlertExitDialog(this).show()
                }
                val fragment = RecipeLogFragment().apply {
                    arguments = Bundle().apply {
                        putParcelable("recipeData", recipeData)
                    }
                }

                // 레시피 프레그먼트 띄워주기
                setFragment(fragment)
            }
        }
    }

    // 툴바 아래 올 프래그먼트 설정
    protected fun setFragment(frag: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_registration, frag)
            .commit()
    }

    fun onDietLogCompleted() {
        setResult(Activity.RESULT_OK)
        finish()
    }
}