package com.example.elixir

import android.os.Bundle
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat


class NavigationActivity : AppCompatActivity() {

    //하단바 ----------
    lateinit var calendarButton: ImageButton
    lateinit var recipeButton: ImageButton
    lateinit var challengeButton: ImageButton
    lateinit var mypageButton: ImageButton


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_calendar)
        WindowCompat.setDecorFitsSystemWindows(window, false)

        //하단바 ----------
        calendarButton = findViewById(R.id.calendar_icon)
        recipeButton = findViewById(R.id.recipe_icon)
        challengeButton = findViewById(R.id.challenge_icon)
        mypageButton = findViewById(R.id.mypage_icon)

        calendarButton.setOnClickListener {
//            val intent = Intent(
//                this@Home_Activity,
//                Locate_Activity::class.java
//            )
//            startActivity(intent)
            Toast.makeText(this, "캘린더 탭 선택됨", Toast.LENGTH_SHORT).show()
        }

        recipeButton.setOnClickListener {
//            val intent = Intent(
//                this@Home_Activity,
//                Travbot_activity::class.java
//            )
//            startActivity(intent)
            Toast.makeText(this, "레시피 탭 선택됨", Toast.LENGTH_SHORT).show()
        }

        challengeButton.setOnClickListener {
//            val intent = Intent(
//                this@Home_Activity,
//                Home_Activity::class.java
//            )
//            startActivity(intent)
            Toast.makeText(this, "챌린지 탭 선택됨", Toast.LENGTH_SHORT).show()
        }


        mypageButton.setOnClickListener {
//            val intent = Intent(
//                this@Home_Activity,
//                Mypage_Activity::class.java
//            )
//            startActivity(intent)
            Toast.makeText(this, "마이페이지 탭 선택됨", Toast.LENGTH_SHORT).show()
        }

    }

}