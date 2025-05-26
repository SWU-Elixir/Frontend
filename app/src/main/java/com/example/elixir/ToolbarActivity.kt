package com.example.elixir

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.PopupMenu
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.fragment.app.Fragment
import com.example.elixir.calendar.ui.MealDetailFragment
import com.example.elixir.chatbot.ChatBotActivity
import com.example.elixir.databinding.ActivityToolbarBinding
import com.example.elixir.dialog.AlertExitDialog
import com.example.elixir.calendar.ui.DietLogFragment
import com.example.elixir.recipe.ui.RecipeFragment
import com.example.elixir.recipe.ui.RecipeLogFragment
import com.example.elixir.signup.CreateAccountFragment
import com.example.elixir.signup.SettingProfileFragment

open class ToolbarActivity : AppCompatActivity() {
    // 선언부
    protected lateinit var toolBinding: ActivityToolbarBinding

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        // 화면 전체 사용, 상태 바를 투명하게 하기
        enableEdgeToEdge()
        WindowCompat.setDecorFitsSystemWindows(window, true)

        super.onCreate(savedInstanceState)
        // 바인딩 정의
        toolBinding = ActivityToolbarBinding.inflate(layoutInflater)
        setContentView(toolBinding.root)

        //val recipeData = intent.getParcelableExtra<RecipeData>("recipeData")

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
                val mealDataJson = intent.getStringExtra("mealData")

                // 타이틀: yyyy년 m월 d일로
                toolBinding.title.text = "${year}년 ${month}월 ${day}일"

                // 뒤로가기 버튼을 누르면 캘린더 페이지로 돌아가기
                // 돌아가기 전 다이얼로그 띄우기
                toolBinding.btnBack.setOnClickListener {
                    AlertExitDialog(this).show()
                }

                val fragment = DietLogFragment().apply {
                    arguments = Bundle().apply {
                        putString("mealData", mealDataJson)
                    }
                }

                // 식단 기록 프래그먼트 띄우기
                setFragment(fragment)
            }

            // 레시피 기록 모드
            9 -> {
                // 툴바의 제목, 더보기 버튼 안보이게, 작동 x
                toolBinding.title.visibility = View.INVISIBLE
                toolBinding.btnMore.visibility = View.INVISIBLE

                // 뒤로가기 버튼을 누르면 레시피 리스트 페이지로 이동
                toolBinding.btnBack.setOnClickListener {
                    AlertExitDialog(this).show()
                }

                val recipeDataJson = intent.getStringExtra("recipeData")
                if( recipeDataJson != null) {
                    val fragment = RecipeLogFragment().apply {
                        arguments = Bundle().apply {
                            putString("recipeData", recipeDataJson)
                            putBoolean("isEdit", true)
                        }
                    }
                    // 레시피 기록 프래그먼트 띄워주기
                    setFragment(fragment)
                }

                // 레시피 프레그먼트 띄워주기
                setFragment(RecipeLogFragment())
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
                    finish()
                }

                toolBinding.btnMore.setOnClickListener {
                    val popupMenu = PopupMenu(this, it)
                    popupMenu.menuInflater.inflate(R.menu.item_menu_drop, popupMenu.menu)

                    popupMenu.setOnMenuItemClickListener { menuItem ->
                        when (menuItem.itemId) {
                            R.id.menu_edit -> {
                                // 기존 상세 intent에서 받은 mealData를 그대로 사용
                                val mealDataJson = intent.getStringExtra("mealData")
                                val year = intent.getIntExtra("year", -1)
                                val month = intent.getIntExtra("month", -1)
                                val day = intent.getIntExtra("day", -1)

                                val editIntent = Intent(this, ToolbarActivity::class.java).apply {
                                    putExtra("mode", 2)
                                    putExtra("year", year)
                                    putExtra("month", month)
                                    putExtra("day", day)
                                    putExtra("mealData", mealDataJson) // 추가: 수정할 데이터 전달
                                }
                                startActivity(editIntent)
                                finish()
                                true
                            }
                            R.id.menu_delete -> {
                                // 식단 삭제 확인 다이얼로그 표시
                                AlertDialog.Builder(this)
                                    .setTitle("식단 삭제")
                                    .setMessage("식단을 삭제하시겠습니까?")
                                    .setPositiveButton("삭제") { _, _ ->
                                        Toast.makeText(this, "식단이 삭제되었습니다", Toast.LENGTH_SHORT).show()
                                        finish()
                                    }
                                    .setNegativeButton("취소", null)
                                    .show()
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

                val mealDataJson = intent.getStringExtra("mealData")

                // MealDetailFragment에 Bundle로 전달
                val fragment = MealDetailFragment()
                val bundle = Bundle().apply {
                    putString("mealData", mealDataJson)
                }
                fragment.arguments = bundle

                setFragment(fragment)
            }

            // 내 레시피 모드
            5 -> {
                // 툴바의 제목은 보이게, 더보기 버튼 안보이게
                toolBinding.title.visibility = View.VISIBLE
                toolBinding.btnMore.visibility = View.INVISIBLE

                // 뒤로가기 버튼을 누르면 이전 화면으로 돌아가기
                toolBinding.btnBack.setOnClickListener {
                    finish()
                }

                // 제목 설정
                val title = intent.getStringExtra("title")
                toolBinding.title.text = title

                // 내 레시피 프래그먼트 띄워주기
                setFragment(MyPageImageGridFragment.newInstance(MyPageImageGridFragment.TYPE_RECIPE))
            }

            // 내 스크랩 모드
            6 -> {
                // 툴바의 제목은 보이게, 더보기 버튼 안보이게
                toolBinding.title.visibility = View.VISIBLE
                toolBinding.btnMore.visibility = View.INVISIBLE

                // 뒤로가기 버튼을 누르면 이전 화면으로 돌아가기
                toolBinding.btnBack.setOnClickListener {
                    finish()
                }

                // 제목 설정
                val title = intent.getStringExtra("title")
                toolBinding.title.text = title

                // 내 스크랩 프래그먼트 띄워주기
                setFragment(MyPageImageGridFragment.newInstance(MyPageImageGridFragment.TYPE_SCRAP))
            }

            // 내 뱃지 모드
            7 -> {
                // 툴바의 제목은 보이게, 더보기 버튼 안보이게
                toolBinding.title.visibility = View.VISIBLE
                toolBinding.btnMore.visibility = View.INVISIBLE

                // 뒤로가기 버튼을 누르면 이전 화면으로 돌아가기
                toolBinding.btnBack.setOnClickListener {
                    finish()
                }

                // 제목 설정
                val title = intent.getStringExtra("title")
                toolBinding.title.text = title

                // 내 스크랩 프래그먼트 띄워주기
                setFragment(MyPageImageGridFragment.newInstance(MyPageImageGridFragment.TYPE_BADGE))
            }

            // 내 뱃지 모드
            8 -> {
                // 툴바의 제목은 보이게, 더보기 버튼 안보이게
                toolBinding.title.visibility = View.VISIBLE
                toolBinding.btnMore.visibility = View.INVISIBLE

                // 뒤로가기 버튼을 누르면 이전 화면으로 돌아가기
                toolBinding.btnBack.setOnClickListener {
                    finish()
                }

                // 제목 설정
                val title = intent.getStringExtra("title")
                toolBinding.title.text = title

                // 내 스크랩 프래그먼트 띄워주기
                setFragment(MypageFollowListFragment())
            }

            // 프로필 수정 모드
            10 -> {
                // 툴바의 제목, 더보기 버튼 안보이게, 작동 x
                toolBinding.title.visibility = View.INVISIBLE
                toolBinding.btnMore.visibility = View.INVISIBLE

                // 뒤로가기 버튼을 누르면 로그인 페이지로 돌아가기
                // 돌아가기 전 다이얼로그 띄우기
                toolBinding.btnBack.setOnClickListener {
                    AlertExitDialog(this).show()
                }

                // 계정 생성 프래그먼트 띄워주기
                setFragment(SettingProfileFragment())
            }
        }
    }

    // 툴바 아래 올 프래그먼트 설정
    protected fun setFragment(frag: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_registration, frag)
            .commit()
    }
}