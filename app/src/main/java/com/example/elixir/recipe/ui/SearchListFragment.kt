package com.example.elixir.recipe.ui

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.AdapterView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.elixir.R
import com.example.elixir.databinding.FragmentRecipeSearchListBinding
import com.example.elixir.recipe.data.RecipeData
import java.math.BigInteger

/**
 * 레시피 검색 결과를 표시하는 프래그먼트
 * 검색어와 필터 조건에 따라 레시피를 필터링하고 표시
 */
class SearchListFragment : Fragment() {

    // ViewBinding
    private var _binding: FragmentRecipeSearchListBinding? = null
    private val binding get() = _binding!!

    // 어댑터 및 데이터
    private lateinit var recipeListAdapter: RecipeListAdapter
    private lateinit var sampleRecipes: List<RecipeData>
    private var hasNavigatedToSearch = false // 중복 이동 방지 플래그

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRecipeSearchListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 1. 검색어 초기화 및 설정
        initializeSearchKeyword()

        // 2. 검색 관련 이벤트 설정
        setupSearchEvents()

        // 3. 스피너 설정
        setupSpinners()

        // 4. 리스트 초기화
        initializeRecipeList()

        // 5. 뒤로가기 버튼 설정
        setupBackButton()
    }

    /**
     * 검색어 초기화 및 설정
     */
    private fun initializeSearchKeyword() {
        val keyword = arguments?.getString("search_keyword")?.trim()
        binding.searchEditText.setText(keyword)
    }

    /**
     * 검색 관련 이벤트 설정
     * - EditText 입력 감지
     * - 검색 버튼 클릭
     * - 키보드 검색 버튼
     */
    private fun setupSearchEvents() {
        // EditText 입력 감지
        binding.searchEditText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {}
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (!hasNavigatedToSearch) {
                    navigateToSearch()
                }
            }
        })

        // 검색 버튼 클릭
        binding.searchButton.setOnClickListener {
            if (!hasNavigatedToSearch) {
                navigateToSearch()
            }
        }

        // 키보드 검색 버튼
        binding.searchEditText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                val keyword = binding.searchEditText.text.toString().trim()
                if (keyword.isNotEmpty()) {
                    filterRecipes(keyword)
                    resetSpinners()
                    updateResetButtonVisibility()
                } else {
                    Toast.makeText(requireContext(), R.string.search_put_something, Toast.LENGTH_SHORT).show()
                }
                true
            } else {
                false
            }
        }
    }

    /**
     * SearchFragment로 이동
     */
    private fun navigateToSearch() {
        hasNavigatedToSearch = true
        val currentKeyword = binding.searchEditText.text.toString().trim()
        val searchFragment = SearchFragment().apply {
            arguments = Bundle().apply {
                putString("search_keyword", currentKeyword)
            }
        }
        parentFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, searchFragment)
            .addToBackStack(null)
            .commit()
    }

    /**
     * 스피너 설정
     * - 저속노화 방법 스피너
     * - 레시피 종류 스피너
     */
    private fun setupSpinners() {
        // 저속노화 방법 스피너
        setupMethodSpinner()
        
        // 레시피 종류 스피너
        setupTypeSpinner()
        
        // 리셋 버튼 클릭 이벤트
        binding.resetButton.setOnClickListener {
            resetSpinners()
            updateResetButtonVisibility()
            filterRecipes(binding.searchEditText.text.toString())
        }
    }

    /**
     * 저속노화 방법 스피너 설정
     */
    private fun setupMethodSpinner() {
        val methodItems = resources.getStringArray(R.array.method_list).toList()
        val methodAdapter = RecipeListSpinnerAdapter(requireContext(), methodItems)
        binding.spinnerDifficulty.adapter = methodAdapter
        binding.spinnerDifficulty.setSelection(0)
        
        binding.spinnerDifficulty.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                val selectedItem = parent.getItemAtPosition(position).toString()
                Log.d("Spinner", "선택된 항목: $selectedItem")
                binding.spinnerDifficulty.isSelected = position != 0
                updateResetButtonVisibility()
                filterRecipes(binding.searchEditText.text.toString())
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }

    /**
     * 레시피 종류 스피너 설정
     */
    private fun setupTypeSpinner() {
        val typeItems = resources.getStringArray(R.array.type_list).toList()
        val typeAdapter = RecipeListSpinnerAdapter(requireContext(), typeItems)
        binding.spinnerType.adapter = typeAdapter
        binding.spinnerType.setSelection(0)
        
        binding.spinnerType.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                val selectedItem = parent.getItemAtPosition(position).toString()
                Log.d("Spinner", "선택된 항목: $selectedItem")
                binding.spinnerType.isSelected = position != 0
                updateResetButtonVisibility()
                filterRecipes(binding.searchEditText.text.toString())
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }

    /**
     * 레시피 리스트 초기화
     */
    private fun initializeRecipeList() {
        // 더미 데이터 초기화
        sampleRecipes = getDummyRecipeData()

        // RecyclerView 설정
        binding.recipeList.layoutManager = LinearLayoutManager(requireContext())
        recipeListAdapter = RecipeListAdapter(
            sampleRecipes.toMutableList(),
            onBookmarkClick = { recipe ->
                recipe.isBookmarked = !recipe.isBookmarked
                recipeListAdapter.notifyItemChanged(sampleRecipes.indexOf(recipe))
            },
            onHeartClick = { recipe ->
                recipe.isLiked = !recipe.isLiked
                recipeListAdapter.notifyItemChanged(sampleRecipes.indexOf(recipe))
            },
            fragmentManager = parentFragmentManager
        )
        binding.recipeList.adapter = recipeListAdapter

        // 검색어가 있으면 필터 적용
        val keyword = binding.searchEditText.text.toString()
        if (keyword.isNotEmpty()) {
            filterRecipes(keyword)
        }
    }

    /**
     * 뒤로가기 버튼 설정
     */
    private fun setupBackButton() {
        binding.backButton.setOnClickListener {
            parentFragmentManager.popBackStack()
        }
    }

    /**
     * 리셋 버튼 표시 여부 업데이트
     */
    private fun updateResetButtonVisibility() {
        val isMethodSelected = binding.spinnerDifficulty.selectedItemPosition != 0
        val isTypeSelected = binding.spinnerType.selectedItemPosition != 0
        binding.resetButton.visibility = if (isMethodSelected || isTypeSelected) View.VISIBLE else View.GONE
    }

    /**
     * 레시피 필터링
     * 검색어, 저속노화 방법, 레시피 종류에 따라 필터링
     */
    private fun filterRecipes(keyword: String) {
        val selectedMethod = binding.spinnerDifficulty.selectedItem?.toString()
        val selectedType = binding.spinnerType.selectedItem?.toString()

        val filtered = sampleRecipes.filter { recipe ->
            val keywordMatch = recipe.title.contains(keyword, ignoreCase = true)
                    || recipe.ingredients.any { it.contains(keyword, ignoreCase = true) }
            val methodMatch = selectedMethod == "저속노화" || recipe.categorySlowAging == selectedMethod
            val typeMatch = selectedType == "종류" || recipe.categoryType == selectedType
            keywordMatch && methodMatch && typeMatch
        }

        // 결과 적용
        recipeListAdapter.updateData(filtered)

        // 결과 없을 시 안내 텍스트 표시
        if (filtered.isEmpty()) {
            binding.recipeList.visibility = View.GONE
            binding.emptyRecipeText.visibility = View.VISIBLE
        } else {
            binding.recipeList.visibility = View.VISIBLE
            binding.emptyRecipeText.visibility = View.GONE
        }
    }

    /**
     * 스피너 초기화
     */
    private fun resetSpinners() {
        binding.spinnerDifficulty.setSelection(0)
        binding.spinnerType.setSelection(0)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    /**
     * 더미 레시피 데이터 생성
     */
    private fun getDummyRecipeData(): List<RecipeData> =
        listOf(
            RecipeData(
                id = BigInteger.valueOf(1),
                memberId = BigInteger.valueOf(1001),
                title = "블루베리 항산화 스무디",
                imageUrl = "android.resource://com.example.elixir.recipe/${R.drawable.ic_recipe_white}",
                categorySlowAging = "항산화 강화",
                categoryType = "음료/차",
                difficulty = "쉬움",
                timeHours = 0,
                timeMinutes = 5,
                ingredients = listOf("블루베리", "그릭요거트", "꿀"),
                seasoning = listOf("얼음", "시나몬 파우더"),
                recipeOrder = listOf("모든 재료를 믹서에 넣는다", "곱게 갈아 컵에 담는다"),
                tips = "시나몬을 추가하면 향과 항산화 성분이 강화됩니다.",
                createdAt = "2025-04-22",
                updateAt = "2025-04-22",
                isBookmarked = false,
                isLiked = false,
                likeCount = 42
            ),
            RecipeData(
                id = BigInteger.valueOf(2),
                memberId = BigInteger.valueOf(1001),
                title = "아보카도 혈당 조절 샐러드",
                imageUrl = "android.resource://com.example.elixir.recipe/${R.drawable.ic_recipe_white}",
                categorySlowAging = "혈당 조절",
                categoryType = "샐러드",
                difficulty = "보통",
                timeHours = 0,
                timeMinutes = 10,
                ingredients = listOf("아보카도", "시금치", "방울토마토"),
                seasoning = listOf("올리브오일", "발사믹식초", "소금"),
                recipeOrder = listOf("야채를 씻고 손질한다", "재료를 접시에 올리고 드레싱을 뿌린다"),
                tips = "견과류를 추가하면 포만감이 높아집니다.",
                createdAt = "2025-04-22",
                updateAt = "2025-04-22",
                isBookmarked = true,
                isLiked = true,
                likeCount = 129
            ),
            RecipeData(
                id = BigInteger.valueOf(3),
                memberId = BigInteger.valueOf(1001),
                title = "토마토 올리브 항염 마리네이드",
                imageUrl = "android.resource://com.example.elixir.recipe/${R.drawable.ic_recipe_white}",
                categorySlowAging = "염증 감소",
                categoryType = "양념/소스/잼",
                difficulty = "쉬움",
                timeHours = 0,
                timeMinutes = 7,
                ingredients = listOf("방울토마토", "올리브오일", "바질잎"),
                seasoning = listOf("소금", "후추", "레몬즙"),
                recipeOrder = listOf("토마토를 반으로 자르고 양념과 섞는다", "냉장 보관 후 30분 숙성"),
                tips = "마늘을 다져 넣으면 향미가 더 풍부해져요.",
                createdAt = "2025-04-22",
                updateAt = "2025-04-22",
                isBookmarked = false,
                isLiked = false,
                likeCount = 58
            ),
            RecipeData(
                id = BigInteger.valueOf(4),
                memberId = BigInteger.valueOf(1001),
                title = "케일 항염 그린 스무디",
                imageUrl = "android.resource://com.example.elixir.recipe/${R.drawable.ic_recipe_white}",
                categorySlowAging = "염증 감소",
                categoryType = "음료/차",
                difficulty = "쉬움",
                timeHours = 0,
                timeMinutes = 3,
                ingredients = listOf("케일", "바나나", "아몬드밀크"),
                seasoning = listOf("얼음", "꿀"),
                recipeOrder = listOf("모든 재료를 믹서기에 넣고 갈기", "컵에 담아 마신다"),
                tips = "단맛이 부족하면 꿀 대신 대추즙도 좋아요.",
                createdAt = "2025-04-22",
                updateAt = "2025-04-22",
                isBookmarked = false,
                isLiked = true,
                likeCount = 312
            ),
            RecipeData(
                id = BigInteger.valueOf(5),
                memberId = BigInteger.valueOf(1001),
                title = "견과류 에너지볼",
                imageUrl = "android.resource://com.example.elixir.recipe/${R.drawable.ic_recipe_white}",
                categorySlowAging = "항산화 강화",
                categoryType = "디저트",
                difficulty = "보통",
                timeHours = 0,
                timeMinutes = 8,
                ingredients = listOf("아몬드", "대추야자", "코코넛"),
                seasoning = listOf("카카오닙스", "시나몬"),
                recipeOrder = listOf("재료를 잘 섞어 공 모양으로 만든다", "냉장 보관 후 굳힌다"),
                tips = "프로틴 파우더를 섞어도 좋아요.",
                createdAt = "2025-04-22",
                updateAt = "2025-04-22",
                isBookmarked = true,
                isLiked = true,
                likeCount = 253
            ),
            RecipeData(
                id = BigInteger.valueOf(6),
                memberId = BigInteger.valueOf(1001),
                title = "그릭요거트 베리볼",
                imageUrl = "android.resource://com.example.elixir.recipe/${R.drawable.ic_recipe_white}",
                categorySlowAging = "항산화 강화",
                categoryType = "디저트",
                difficulty = "쉬움",
                timeHours = 0,
                timeMinutes = 2,
                ingredients = listOf("그릭요거트", "블루베리", "라즈베리"),
                seasoning = listOf("아몬드슬라이스", "꿀"),
                recipeOrder = listOf("재료를 그릇에 층층이 담는다", "견과류를 위에 뿌린다"),
                tips = "생꿀 대신 메이플시럽도 잘 어울립니다.",
                createdAt = "2025-04-22",
                updateAt = "2025-04-22",
                isBookmarked = false,
                isLiked = false,
                likeCount = 19
            )
        )
}

