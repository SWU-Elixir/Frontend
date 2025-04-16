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
import com.example.elixir.calendar.CalendarFragment
import com.example.elixir.recipe.RecipeListFragment

class HomeActivity : AppCompatActivity() {

    // 하단 네비게이션 바의 아이콘 버튼들
    private lateinit var calendarButton: ImageButton
    private lateinit var recipeButton: ImageButton
    private lateinit var challengeButton: ImageButton
    private lateinit var mypageButton: ImageButton
    private lateinit var chatbotButton: ImageButton

    // 버튼 하단의 텍스트 라벨들
    private lateinit var calendarTitle: TextView
    private lateinit var recipeTitle: TextView
    private lateinit var challengeTitle: TextView
    private lateinit var mypageTitle: TextView
    private lateinit var chatbotTitle: TextView

    // 현재 선택된 텍스트와 버튼 추적용 변수
    private var selectedTitle: TextView? = null
    private var selectedButton: ImageButton? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 시스템 UI(edge-to-edge) 설정
        enableEdgeToEdge()
        setContentView(R.layout.activity_home)
        WindowCompat.setDecorFitsSystemWindows(window, false)

        // 하단 네비게이션 버튼 및 텍스트 초기화
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

        // 최초 실행 시 캘린더 탭을 기본 선택 상태로 설정
        calendarTitle.setTextColor(ContextCompat.getColor(this, R.color.elixir_orange))
        selectedTitle = calendarTitle
        calendarButton.setImageResource(R.drawable.ic_navi_calendar_selected)
        selectedButton = calendarButton

        // 기본 프래그먼트로 CalendarFragment 표시
        replaceFragment(CalendarFragment())

        // 탭 버튼 클릭 이벤트 설정
        setupButtonListeners()
    }

    /**
     * 각 하단 탭 버튼 클릭 시 호출되는 리스너 설정
     */
    private fun setupButtonListeners() {
        calendarButton.setOnClickListener {
            updateSelectedButton(calendarButton, calendarTitle)
            replaceFragment(CalendarFragment())
        }

        recipeButton.setOnClickListener {
            updateSelectedButton(recipeButton, recipeTitle)
            replaceFragment(RecipeListFragment())
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

    /**
     * 선택된 버튼 및 텍스트 색상/아이콘 상태 변경 처리
     */
    private fun updateSelectedButton(newButton: ImageButton, newTitle: TextView) {
        // 이전에 선택된 버튼/텍스트를 원래 상태로 복구
        selectedButton?.setImageResource(getUnselectedResId(selectedButton))
        selectedTitle?.setTextColor(ContextCompat.getColor(this, R.color.elixir_gray))

        // 새로 선택된 버튼/텍스트 강조
        newButton.setImageResource(getSelectedResId(newButton))
        newTitle.setTextColor(ContextCompat.getColor(this, R.color.elixir_orange))

        // 현재 상태 갱신
        selectedButton = newButton
        selectedTitle = newTitle
    }

    /**
     * 버튼에 해당하는 선택된 상태 아이콘 리소스를 반환
     */
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

    /**
     * 버튼에 해당하는 비활성화(일반) 상태 아이콘 리소스를 반환
     */
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

    /**
     * 프래그먼트로 화면을 전환하는 함수
     */
    private fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .commit()
    }
}
