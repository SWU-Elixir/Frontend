package com.example.elixir

import android.os.Bundle
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.fragment.app.Fragment


class NavigationActivity : AppCompatActivity() {

    //하단바 ----------
    lateinit var calendarButton: ImageButton
    lateinit var recipeButton: ImageButton
    lateinit var challengeButton: ImageButton
    lateinit var mypageButton: ImageButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        WindowCompat.setDecorFitsSystemWindows(window, false)

        // 기본 프래그먼트 설정
        replaceFragment(CalendarFragment())

        calendarButton = findViewById(R.id.calendar_icon)
        recipeButton = findViewById(R.id.recipe_icon)
        challengeButton = findViewById(R.id.challenge_icon)
        mypageButton = findViewById(R.id.mypage_icon)

        setupButtonListeners()
    }

    private fun setupButtonListeners() {
        calendarButton.setOnClickListener {
            replaceFragment(CalendarFragment())
            Toast.makeText(this, "캘린더 탭 선택됨", Toast.LENGTH_SHORT).show()
        }

        recipeButton.setOnClickListener {
            //replaceFragment(RecipeFragment())
            Toast.makeText(this, "레시피 탭 선택됨", Toast.LENGTH_SHORT).show()
        }

        challengeButton.setOnClickListener {
            //replaceFragment(ChallengeFragment())
            Toast.makeText(this, "챌린지 탭 선택됨", Toast.LENGTH_SHORT).show()
        }

        mypageButton.setOnClickListener {
            //replaceFragment(MyPageFragment())
            Toast.makeText(this, "마이페이지 탭 선택됨", Toast.LENGTH_SHORT).show()
        }
    }

    private fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .commit()
    }
}