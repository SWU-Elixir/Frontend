package com.example.elixir

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.PopupMenu
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import com.example.elixir.calendar.data.MealDto
import com.example.elixir.calendar.network.DietApi
import com.example.elixir.calendar.network.db.DietLogDao
import com.example.elixir.calendar.network.db.DietLogRepository
import com.example.elixir.calendar.ui.MealDetailFragment
import com.example.elixir.chatbot.ChatBotActivity
import com.example.elixir.databinding.ActivityToolbarBinding
import com.example.elixir.dialog.AlertExitDialog
import com.example.elixir.calendar.ui.DietLogFragment
import com.example.elixir.calendar.viewmodel.MealViewModel
import com.example.elixir.calendar.viewmodel.MealViewModelFactory
import com.example.elixir.ingredient.data.IngredientDao
import com.example.elixir.ingredient.network.IngredientApi
import com.example.elixir.ingredient.network.IngredientDB
import com.example.elixir.ingredient.network.IngredientRepository
import com.example.elixir.member.MyPageImageGridFragment
import com.example.elixir.member.MypageFollowListFragment
import com.example.elixir.member.data.MemberDao
import com.example.elixir.member.network.MemberApi
import com.example.elixir.member.network.MemberDB
import com.example.elixir.member.network.MemberRepository
import com.example.elixir.network.AppDatabase
import com.example.elixir.recipe.ui.RecipeLogFragment
import com.example.elixir.signup.CreateAccountFragment
import com.example.elixir.signup.SettingProfileFragment

open class ToolbarActivity : AppCompatActivity() {
    // 선언부
    protected lateinit var toolBinding: ActivityToolbarBinding
    private val mealViewModel: MealViewModel by viewModels {
        MealViewModelFactory(dietRepository, memberRepository, ingredientRepository)
    }

    // DB, API 관련
    private lateinit var dietRepository: DietLogRepository
    private lateinit var memberRepository: MemberRepository
    private lateinit var ingredientRepository: IngredientRepository

    private lateinit var dietDao: DietLogDao
    private lateinit var memberDao: MemberDao
    private lateinit var ingredientDao: IngredientDao

    private lateinit var dietApi: DietApi
    private lateinit var memberApi: MemberApi
    private lateinit var ingredientApi: IngredientApi

    private var mealDataJson: String? = null
    private var dietId: Int = -1

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        // 화면 전체 사용, 상태 바를 투명하게 하기
        enableEdgeToEdge()
        WindowCompat.setDecorFitsSystemWindows(window, true)

        super.onCreate(savedInstanceState)
        // 바인딩 정의
        toolBinding = ActivityToolbarBinding.inflate(layoutInflater)
        setContentView(toolBinding.root)

        // 데이터베이스와 API 초기화
        dietDao = AppDatabase.getInstance(this).dietLogDao()
        dietApi = RetrofitClient.instanceDietApi
        dietRepository = DietLogRepository(dietDao, dietApi)

        memberDao = MemberDB.getInstance(this).memberDao()
        memberApi = RetrofitClient.instanceMemberApi
        memberRepository = MemberRepository(memberApi, memberDao)

        ingredientDao = IngredientDB.getInstance(this).ingredientDao()
        ingredientApi = RetrofitClient.instanceIngredientApi
        ingredientRepository = IngredientRepository(ingredientApi, ingredientDao)

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
                mealDataJson = intent.getStringExtra("mealData")

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

                // 삭제 전송이 다 끝났는지 관찰
                mealViewModel.deleteResult.observe(this) { result ->
                    result.onSuccess {
                        // 성공: 결과 intent 전달 후 종료
                        val resultIntent = Intent().putExtra("deletedDietLogId", dietId)
                        setResult(Activity.RESULT_OK, resultIntent)
                        Toast.makeText(this, "삭제되었습니다", Toast.LENGTH_SHORT).show()
                        finish()
                    }.onFailure { exception ->
                        // 실패: 예외 메시지 활용 가능
                        val message = exception?.localizedMessage ?: "삭제하지 못했습니다."
                        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
                    }
                }

                // 더보기 버튼(수정/삭제)을 눌렀을 때
                toolBinding.btnMore.setOnClickListener {
                    // 드롭 메뉴 보여주기
                    val popupMenu = PopupMenu(this, it)
                    popupMenu.menuInflater.inflate(R.menu.item_menu_drop, popupMenu.menu)
                    dietId = intent.getIntExtra("dietLogId", -1)

                    // 드롭 메뉴 아이템 선택
                    popupMenu.setOnMenuItemClickListener { menuItem ->
                        when (menuItem.itemId) {
                            // 수정 모드
                            R.id.menu_edit -> {
                                mealDataJson = intent.getStringExtra("mealData")
                                val year = intent.getIntExtra("year", -1)
                                val month = intent.getIntExtra("month", -1)
                                val day = intent.getIntExtra("day", -1)

                                val editIntent = Intent(this, ToolbarActivity::class.java).apply {
                                    putExtra("mode", 2)
                                    putExtra("year", year)
                                    putExtra("month", month)
                                    putExtra("day", day)
                                    putExtra("dietLogId", dietId)
                                    putExtra("mealData", mealDataJson) // 추가: 수정할 데이터 전달
                                }
                                startActivity(editIntent)
                                finish()
                                true
                            }

                            // 삭제 모드
                            R.id.menu_delete -> {
                                AlertDialog.Builder(this)
                                    .setTitle("식단 삭제")
                                    .setMessage("식단을 삭제하시겠습니까?")
                                    .setPositiveButton("삭제") { _, _ ->
                                        if (dietId != -1) {
                                            mealViewModel.deleteDietLog(dietId)
                                        } else {
                                            Toast.makeText(this, "삭제할 식단 ID가 없습니다.", Toast.LENGTH_SHORT).show()
                                        }
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

                mealDataJson = intent.getStringExtra("mealData")

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

            // 팔로워 모드
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
                setFragment(MypageFollowListFragment.newInstance(MypageFollowListFragment.MODE_FOLLOWER))
            }

            // 팔로잉 모드
            11 -> {
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
                setFragment(MypageFollowListFragment.newInstance(MypageFollowListFragment.MODE_FOLLOWING))
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