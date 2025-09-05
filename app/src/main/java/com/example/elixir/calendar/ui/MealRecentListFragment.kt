package com.example.elixir.calendar.ui

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import com.example.elixir.RetrofitClient
import com.example.elixir.calendar.data.DietLogData
import com.example.elixir.calendar.data.MealDto
import com.example.elixir.calendar.network.db.DietLogRepository
import com.example.elixir.calendar.viewmodel.MealViewModel
import com.example.elixir.calendar.viewmodel.MealViewModelFactory
import com.example.elixir.databinding.FragmentMealRecentListBinding
import com.example.elixir.ingredient.network.IngredientRepository
import com.example.elixir.member.network.MemberRepository
import com.example.elixir.network.AppDatabase
import com.google.gson.Gson
import org.threeten.bp.LocalDateTime

class MealRecentListFragment : Fragment(), OnMealClickListener {

    private var _binding: FragmentMealRecentListBinding? = null
    private val binding get() = _binding!!

    private lateinit var dietRepository: DietLogRepository
    private lateinit var memberRepository: MemberRepository
    private lateinit var ingredientRepository: IngredientRepository
    private lateinit var mealAdapter: MealRecentListAdapter

    private val mealViewModel: MealViewModel by viewModels {
        MealViewModelFactory(dietRepository, memberRepository, ingredientRepository)
    }

    // 전체 식단 데이터 리스트
    private var allMealList = mutableListOf<DietLogData>()
    private var filteredMealList = mutableListOf<DietLogData>()

    // 상세 화면 런처 등록
    private lateinit var mealDetailLauncher: ActivityResultLauncher<Intent>

    override fun onMealClick(item: DietLogData) {
        // 선택된 식단 데이터를 Gson을 이용해 String으로 변환
        val resultBundle = Bundle().apply {
            putString("selected_meal_data", Gson().toJson(item))
        }

        // 결과를 DietLogFragment로 전달하고 Fragment 닫기
        setFragmentResult("meal_selection_request", resultBundle)
        parentFragmentManager.popBackStack()

        // 선택된 식단 데이터 로깅 (디버깅용)
        Log.d("MealRecentListFragment", "Meal selected and sent: ${item.dietTitle}")
        Log.d("MealRecentListFragment", "Meal Data sent: ${Gson().toJson(item)}")
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMealRecentListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        try {
            // 레포지토리 및 뷰모델 초기화
            initializeRepositories()

            // UI 초기화
            setupRecyclerView()
            setupSearchListeners()
            setupBackButton()

            // 상세 화면 런처 등록
            setupMealDetailLauncher()

            // 초기 데이터 로드
            loadInitialData()

        } catch (e: Exception) {
            Log.e("MealRecentList", "Initialization error: ${e.message}", e)
            Toast.makeText(requireContext(), "초기화 중 오류가 발생했습니다: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun initializeRepositories() {
        // DietLogRepository 초기화
        val appDB = AppDatabase.getInstance(requireContext())

        val dietDao = appDB.dietLogDao()
        val dietApi = RetrofitClient.instanceDietApi
        dietRepository = DietLogRepository(dietDao, dietApi)

        // MemberRepository 초기화
        val memberDao = appDB.memberDao()
        val memberApi = RetrofitClient.instanceMemberApi
        memberRepository = MemberRepository(memberApi, memberDao)

        // IngredientRepository 초기화
        val ingredientDao = appDB.ingredientDao()
        val ingredientApi = RetrofitClient.instanceIngredientApi
        ingredientRepository = IngredientRepository(ingredientApi, ingredientDao)
    }

    private fun setupRecyclerView() {
        // 어댑터 초기화
        mealAdapter = MealRecentListAdapter(requireContext(), mutableListOf(), this)

        // ListView 설정
        binding.listMeal.adapter = mealAdapter
    }

    private fun setupMealDetailLauncher() {
        mealDetailLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                // 상세 화면에서 돌아온 후 전체 식단 데이터 다시 로드
                loadAllMealData()
            }
        }
    }

    private fun loadInitialData() {
        // 로딩 상태 표시
        showLoading(true)

        // 식재료 데이터 먼저 로드
        mealViewModel.loadIngredients()

        // 전체 식단 데이터 로드
        mealViewModel.getAllDietLogs()
    }

    private fun loadAllMealData() {
        showLoading(true)
        mealViewModel.getAllDietLogs()
    }

    private fun setupSearchListeners() {
        // 텍스트 변경 리스너
        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val input = s.toString().trim()
                updateSearchUI(input.isNotEmpty())
                // 실시간 검색 수행
                filterMealsBySearch(input)
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        // 키보드 검색 버튼
        binding.etSearch.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                hideKeyboard()
                true
            } else {
                false
            }
        }

        // 삭제 버튼
        binding.btnClear.setOnClickListener {
            binding.etSearch.setText("")
            hideKeyboard()
            // 전체 리스트 다시 표시
            filteredMealList.clear()
            filteredMealList.addAll(allMealList)
            mealAdapter.updateData(filteredMealList)
            updateSearchUI(false)
            updateListVisibility()
        }
    }

    private fun setupBackButton() {
        binding.btnBack.setOnClickListener {
            hideKeyboard()
            parentFragmentManager.popBackStack()
        }
    }

    private fun observeViewModelData() {
        // 전체 식단 데이터 관찰
        mealViewModel.dailyDietLogs.observe(viewLifecycleOwner) { mealList ->
            showLoading(false)

            if (mealList != null) {
                // MealDto를 DietLogData로 변환
                val dietLogList = mealList.map { convertMealDtoToDietLogData(it) }

                // 전체 데이터 업데이트
                allMealList.clear()
                allMealList.addAll(dietLogList)

                // 필터링된 데이터 업데이트 (검색 중이 아니면 전체 데이터)
                val searchText = binding.etSearch.text.toString().trim()
                if (searchText.isEmpty()) {
                    filteredMealList.clear()
                    filteredMealList.addAll(allMealList)
                } else {
                    filterMealsBySearch(searchText)
                }

                // 어댑터 데이터 업데이트
                mealAdapter.updateData(filteredMealList)
                updateListVisibility()
            } else {
                // 데이터가 없는 경우
                allMealList.clear()
                filteredMealList.clear()
                mealAdapter.updateData(filteredMealList)
                updateListVisibility()
            }
        }

        // 식재료 데이터 관찰
        mealViewModel.ingredientList.observe(viewLifecycleOwner) { ingredientList ->
            val ingredientMap = ingredientList.associateBy { it.id }
            mealAdapter.setIngredientMap(ingredientMap)
        }
    }

    private fun filterMealsBySearch(searchText: String) {
        filteredMealList.clear()

        if (searchText.isEmpty()) {
            filteredMealList.addAll(allMealList)
        } else {
            val filtered = allMealList.filter { meal ->
                // 1. 식단 제목으로 검색
                val titleMatch = meal.dietTitle.contains(searchText, ignoreCase = true)

                // 2. 식재료 이름으로 검색
                val ingredientMatch = searchInIngredients(meal.ingredientTags, searchText)

                // 3. 날짜로 검색 (여러 형식 지원)
                val dateMatch = searchInDate(meal.time, searchText)

                // 4. 식단 카테고리로 검색
                val categoryMatch = meal.dietCategory.contains(searchText, ignoreCase = true)

                // 하나라도 매치되면 결과에 포함
                titleMatch || ingredientMatch || dateMatch || categoryMatch
            }
            filteredMealList.addAll(filtered)
        }

        mealAdapter.updateData(filteredMealList)
        updateListVisibility()
    }


    private fun updateSearchUI(isSearching: Boolean) {
        binding.btnClear.visibility = if (isSearching) View.VISIBLE else View.GONE
    }

    private fun updateListVisibility() {
        if (filteredMealList.isEmpty()) {
            binding.listMeal.visibility = View.GONE
            binding.tvNoMeal.visibility = View.VISIBLE
        } else {
            binding.listMeal.visibility = View.VISIBLE
            binding.tvNoMeal.visibility = View.GONE
        }
    }

    private fun showLoading(show: Boolean) {
        if (show) {
            binding.tvProgress.visibility = View.VISIBLE
            binding.listMeal.visibility = View.GONE
            binding.tvNoMeal.visibility = View.GONE
        } else {
            binding.tvProgress.visibility = View.GONE
            updateListVisibility()
        }
    }

    private fun searchInIngredients(ingredientTags: List<Int>, searchText: String): Boolean {
        return try {
            // 식재료 맵이 있는지 확인
            val ingredientMap = mealAdapter.getIngredientMap()

            // 각 식재료 ID에 대해 이름 확인
            ingredientTags.any { tagId ->
                val ingredient = ingredientMap[tagId]
                ingredient?.name?.contains(searchText, ignoreCase = true) == true
            }
        } catch (e: Exception) {
            Log.e("MealRecentList", "Error searching ingredients: ${e.message}")
            false
        }
    }

    private fun searchInDate(dateTime: LocalDateTime, searchText: String): Boolean {
        return try {
            val searchLower = searchText.lowercase()

            // 다양한 날짜 형식으로 검색 지원
            val year = dateTime.year.toString()
            val month = dateTime.monthValue.toString().padStart(2, '0')
            val day = dateTime.dayOfMonth.toString().padStart(2, '0')
            val hour = dateTime.hour.toString().padStart(2, '0')
            val minute = dateTime.minute.toString().padStart(2, '0')

            // 검색 가능한 날짜 형식들
            val dateFormats = listOf(
                // 년도
                year,
                // 월
                month,
                "${month}월",
                // 일
                day,
                "${day}일",
                // 년-월
                "$year-$month",
                "${year}년 ${month}월",
                // 년-월-일
                "$year-$month-$day",
                "${year}년 ${month}월 ${day}일",
                "$year.$month.$day",
                "$year/$month/$day",
                // 월-일
                "$month-$day",
                "${month}월 ${day}일",
                "$month.$day",
                "$month/$day",
                // 시간
                "$hour:$minute",
                "${hour}시",
                "${hour}시 ${minute}분",
                // 요일 (한국어)
                getKoreanDayOfWeek(dateTime)
            )

            // 검색어가 날짜 형식 중 하나와 일치하는지 확인
            dateFormats.any { format ->
                format.lowercase().contains(searchLower)
            }
        } catch (e: Exception) {
            Log.e("MealRecentList", "Error searching date: ${e.message}")
            false
        }
    }

    private fun getKoreanDayOfWeek(dateTime: LocalDateTime): String {
        return when (dateTime.dayOfWeek.value) {
            1 -> "월요일"
            2 -> "화요일"
            3 -> "수요일"
            4 -> "목요일"
            5 -> "금요일"
            6 -> "토요일"
            7 -> "일요일"
            else -> ""
        }
    }


    private fun convertMealDtoToDietLogData(mealDto: MealDto): DietLogData {
        return DietLogData(
            id = mealDto.id,
            dietImg = mealDto.imageUrl ?: "",
            time = LocalDateTime.parse(mealDto.time),
            dietTitle = mealDto.name,
            dietCategory = mealDto.type,
            ingredientTags = mealDto.ingredientTagId,
            score = mealDto.score
        )
    }

    private fun hideKeyboard() {
        val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(binding.etSearch.windowToken, 0)
    }

    override fun onResume() {
        super.onResume()
        // 데이터 관찰 시작
        observeViewModelData()

        // 키보드 자동 표시 제거
        binding.etSearch.requestFocus()
    }

    override fun onPause() {
        super.onPause()
        hideKeyboard()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        hideKeyboard()
        _binding = null
    }
}