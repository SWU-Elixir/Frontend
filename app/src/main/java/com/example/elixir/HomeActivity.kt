package com.example.elixir

import android.os.Bundle
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.fragment.app.Fragment

class HomeActivity : AppCompatActivity() {

    // 하단바 버튼
    private lateinit var calendarButton: ImageButton
    private lateinit var recipeButton: ImageButton
    private lateinit var challengeButton: ImageButton
    private lateinit var mypageButton: ImageButton
    private lateinit var chatbotButton: ImageButton

    // 버튼 텍스트
    private lateinit var calendarTitle: TextView
    private lateinit var recipeTitle: TextView
    private lateinit var challengeTitle: TextView
    private lateinit var mypageTitle: TextView
    private lateinit var chatbotTitle: TextView

    private var selectedTitle: TextView? = null

    // 현재 선택된 버튼
    private var selectedButton: ImageButton? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_home)
        WindowCompat.setDecorFitsSystemWindows(window, false)

        calendarButton = findViewById(R.id.calendar_button)
        recipeButton = findViewById(R.id.recipe_button)
        challengeButton = findViewById(R.id.challenge_button)
        mypageButton = findViewById(R.id.mypage_button)
        chatbotButton = findViewById(R.id.chatbot_button)

        calendarTitle = findViewById(R.id.calendar_title)
        recipeTitle = findViewById(R.id.recipe_title)
        challengeTitle = findViewById(R.id.challenge_title)
        mypageTitle = findViewById(R.id.mypage_title)
        chatbotTitle = findViewById(R.id.chatbot_title)

        // 기본 선택 텍스트 색상
        calendarTitle.setTextColor(ContextCompat.getColor(this, R.color.elixir_orange))
        selectedTitle = calendarTitle

        // 기본 프래그먼트 및 버튼 설정
        replaceFragment(CalendarFragment())
        calendarButton.setImageResource(R.drawable.ic_navi_calendar_selected)
        selectedButton = calendarButton

        setupButtonListeners()
    }

    private fun setupButtonListeners() {
        calendarButton.setOnClickListener {
            updateSelectedButton(calendarButton, calendarTitle)
            replaceFragment(CalendarFragment())
            Toast.makeText(this, "캘린더 탭 선택됨", Toast.LENGTH_SHORT).show()
        }

        recipeButton.setOnClickListener {
            updateSelectedButton(recipeButton, recipeTitle)
            replaceFragment(RecipeListFragment())
            Toast.makeText(this, "레시피 탭 선택됨", Toast.LENGTH_SHORT).show()
        }

        challengeButton.setOnClickListener {
            updateSelectedButton(challengeButton, challengeTitle)
            Toast.makeText(this, "챌린지 탭 선택됨", Toast.LENGTH_SHORT).show()
        }

        chatbotButton.setOnClickListener {
            updateSelectedButton(chatbotButton, chatbotTitle)
            Toast.makeText(this, "챗봇 탭 선택됨", Toast.LENGTH_SHORT).show()
        }

        mypageButton.setOnClickListener {
            updateSelectedButton(mypageButton, mypageTitle)
            Toast.makeText(this, "마이페이지 탭 선택됨", Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateSelectedButton(newButton: ImageButton, newTitle: TextView) {
        // 이전 버튼 이미지 원래대로
        selectedButton?.setImageResource(getUnselectedResId(selectedButton))
        selectedTitle?.setTextColor(ContextCompat.getColor(this, R.color.elixir_gray))

        // 새로 선택된 버튼 이미지와 텍스트 색상 변경
        newButton.setImageResource(getSelectedResId(newButton))
        newTitle.setTextColor(ContextCompat.getColor(this, R.color.elixir_orange))

        selectedButton = newButton
        selectedTitle = newTitle
    }

    private fun getSelectedResId(button: ImageButton?): Int {
        return when (button?.id) {
            R.id.calendar_button -> R.drawable.ic_navi_calendar_selected
            R.id.recipe_button -> R.drawable.ic_navi_recipe_selected
            R.id.challenge_button -> R.drawable.ic_navi_challenge_selected
            R.id.chatbot_button -> R.drawable.ic_navi_chatbot_selected
            R.id.mypage_button -> R.drawable.ic_navi_mypage_selected
            else -> R.drawable.ic_navi_calendar_selected
        }
    }

    private fun getUnselectedResId(button: ImageButton?): Int {
        return when (button?.id) {
            R.id.calendar_button -> R.drawable.ic_navi_calendar_normal
            R.id.recipe_button -> R.drawable.ic_navi_recipe_normal
            R.id.challenge_button -> R.drawable.ic_navi_challenge_normal
            R.id.chatbot_button -> R.drawable.ic_navi_chatbot_normal
            R.id.mypage_button -> R.drawable.ic_navi_mypage_normal
            else -> R.drawable.ic_navi_calendar_normal
        }
    }



    private fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .commit()
    }
}
