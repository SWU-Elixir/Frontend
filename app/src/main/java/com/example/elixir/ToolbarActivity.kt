package com.example.elixir

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.PopupMenu
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.fragment.app.Fragment
import com.example.elixir.calendar.MealDetailFragment
import com.example.elixir.chatbot.ChatBotActivity
import com.example.elixir.databinding.ActivityToolbarBinding
import com.example.elixir.signup.CreateAccountFragment

open class ToolbarActivity : AppCompatActivity() {
    // 선언부
    protected lateinit var toolBinding: ActivityToolbarBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        // 화면 전체 사용, 상태 바를 투명하게 하기
        enableEdgeToEdge()
        WindowCompat.setDecorFitsSystemWindows(window, true)

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
                    AlertExitDialog(this).show()
                }

                // 계정 생성 프래그먼트 띄워주기
                setFragment(CreateAccountFragment())
            }

            // 식단 기록 모드
            2 -> {
                // 툴바의 제목은 보이게, 더보기 버튼 안보이게
                toolBinding.title.visibility = View.VISIBLE
                toolBinding.btnMore.visibility = View.INVISIBLE

                // 뒤로가기 버튼을 누르면 로그인 페이지로 돌아가기
                // 돌아가기 전 다이얼로그 띄우기
                toolBinding.btnBack.setOnClickListener {
                    AlertExitDialog(this).show()
                }

                // 날짜 불러오기
                val date = intent.getStringExtra("date")
                toolBinding.title.text = date

                // 식단 기록 프래그먼트 띄워주기
                setFragment(DietLogFragment())
            }

            // 챗봇 모드
            3 -> {
                // ChatBotActivity로 이동
                val intent = Intent(this, ChatBotActivity::class.java)
                startActivity(intent)
                finish()
            }

            // 식단 상세 모드
            4 -> {
                // 툴바의 제목은 보이게, 더보기 버튼 보이게
                toolBinding.title.visibility = View.VISIBLE
                toolBinding.btnMore.visibility = View.VISIBLE

                // 뒤로가기 버튼을 누르면 이전 화면으로 돌아가기
                toolBinding.btnBack.setOnClickListener {
                    onBackPressed()
                }

                toolBinding.btnMore.setOnClickListener {
                    val popupMenu = PopupMenu(this, it)
                    popupMenu.menuInflater.inflate(R.menu.item_menu_drop, popupMenu.menu)

                    popupMenu.setOnMenuItemClickListener { menuItem ->
                        when (menuItem.itemId) {
                            R.id.menu_edit -> {
                                Toast.makeText(this, "댓글 수정 클릭됨", Toast.LENGTH_SHORT).show()
                                true
                            }
                            R.id.menu_delete -> {
                                Toast.makeText(this, "댓글 삭제 클릭됨", Toast.LENGTH_SHORT).show()
                                true
                            }
                            else -> false
                        }
                    }
                    popupMenu.show()
                }

                // 식단 이름 불러오기
                val mealName = intent.getStringExtra("mealName")
                toolBinding.title.text = mealName

                // 식단 상세 프래그먼트 띄워주기
                setFragment(MealDetailFragment())
            }
        }
    }

    protected fun setFragment(frag: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_registration, frag)
            .commit()
    }
}