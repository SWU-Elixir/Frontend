package com.example.elixir

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.fragment.app.Fragment
import com.example.elixir.calendar.ui.CalendarFragment
import com.example.elixir.challenge.ChallengeFragment
import com.example.elixir.chatbot.ChatBotActivity
import com.example.elixir.databinding.ActivityHomeBinding
import com.example.elixir.recipe.ui.RecipeFragment

class HomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHomeBinding

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

    // 이전 프래그먼트와 버튼 상태 저장
    private var previousFragment: Fragment? = null
    private var previousButton: ImageButton? = null
    private var previousTitle: TextView? = null

    private val chatBotLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        // 챗봇 액티비티에서 돌아왔을 때 이전 상태로 복구
        if (previousFragment != null && previousButton != null && previousTitle != null) {
            updateSelectedButton(previousButton!!, previousTitle!!)
            replaceFragment(previousFragment!!)
        } else {
            // 이전 상태가 없는 경우 캘린더로 복구
            updateSelectedButton(calendarButton, calendarTitle)
            replaceFragment(CalendarFragment())
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 시스템 UI(edge-to-edge) 설정
        enableEdgeToEdge()
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        WindowCompat.setDecorFitsSystemWindows(window, false)

        // 하단 네비게이션 버튼 및 텍스트 초기화
        calendarButton = binding.calendarButton
        recipeButton = binding.recipeButton
        challengeButton = binding.challengeButton
        mypageButton = binding.mypageButton
        chatbotButton = binding.chatbotButton

        calendarTitle = binding.calendarTitle
        recipeTitle = binding.recipeTitle
        challengeTitle = binding.challengeTitle
        mypageTitle = binding.mypageTitle
        chatbotTitle = binding.chatbotTitle

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
            savePreviousState()
            updateSelectedButton(calendarButton, calendarTitle)
            replaceFragment(CalendarFragment())
        }

        recipeButton.setOnClickListener {
            savePreviousState()
            updateSelectedButton(recipeButton, recipeTitle)
            replaceFragment(RecipeFragment())
        }

        challengeButton.setOnClickListener {
            savePreviousState()
            updateSelectedButton(challengeButton, challengeTitle)
            replaceFragment(ChallengeFragment())
        }

        chatbotButton.setOnClickListener {
            savePreviousState()
            updateSelectedButton(chatbotButton, chatbotTitle)

            chatBotLauncher.launch(Intent(this, ChatBotActivity::class.java))
        }

        mypageButton.setOnClickListener {
            savePreviousState()
            updateSelectedButton(mypageButton, mypageTitle)
            replaceFragment(MyPageFragment())
        }
    }

    /**
     * 현재 상태를 이전 상태로 저장
     */
    private fun savePreviousState() {
        previousFragment = supportFragmentManager.findFragmentById(R.id.fragmentContainer)
        previousButton = selectedButton
        previousTitle = selectedTitle
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
