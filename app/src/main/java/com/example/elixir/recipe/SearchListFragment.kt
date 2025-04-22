package com.example.elixir.recipe

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.elixir.R
import java.math.BigInteger

class SearchListFragment : Fragment() {

    private lateinit var searchButton: ImageButton
    private lateinit var backButton: ImageButton
    private lateinit var searchEditText: EditText

    private lateinit var recipeListView: RecyclerView
    private lateinit var recipeListAdapter: RecipeListAdapter

    private lateinit var methodSpinner: Spinner
    private lateinit var typeSpinner: Spinner

    private lateinit var resetButton: Button
    private lateinit var emptyRecipeText: TextView

    private lateinit var sampleRecipes: List<RecipeData>
    private var hasNavigatedToSearch = false // 중복 방지 플래그

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_recipe_search_list, container, false)

        backButton = view.findViewById(R.id.backButton)
        searchButton = view.findViewById(R.id.searchButton)
        searchEditText = view.findViewById(R.id.searchEditText)
        methodSpinner = view.findViewById(R.id.spinner_difficulty)
        typeSpinner = view.findViewById(R.id.spinner_type)
        resetButton = view.findViewById(R.id.resetButton)
        recipeListView = view.findViewById(R.id.recipeList)
        emptyRecipeText = view.findViewById(R.id.emptyRecipeText)

        // 전달받은 검색어 및 필터값
        val keyword = arguments?.getString("search_keyword")?.trim()
        searchEditText.setText(keyword)

        // EditText 입력 감지 → SearchFragment로 이동 (한 번만)
        searchEditText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {}
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (!hasNavigatedToSearch) {
                    hasNavigatedToSearch = true
                    val currentKeyword = searchEditText.text.toString().trim()
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
            }
        })

        // 검색 버튼 클릭 시 SearchFragment로 이동
        searchButton.setOnClickListener {
            if (!hasNavigatedToSearch) {
                hasNavigatedToSearch = true
                val currentKeyword = searchEditText.text.toString().trim()
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
        }

        fun updateResetButtonVisibility() {
            val isMethodSelected = methodSpinner.selectedItemPosition != 0
            val isTypeSelected = typeSpinner.selectedItemPosition != 0
            resetButton.visibility = if (isMethodSelected || isTypeSelected) View.VISIBLE else View.GONE
        }

        // 스피너 초기화
        val methodItems = resources.getStringArray(R.array.method_list).toList()
        val methodAdapter = RecipeListSpinnerAdapter(requireContext(), methodItems)
        methodSpinner.adapter = methodAdapter
        methodSpinner.setSelection(0) // 초기 선택값 설정
        methodSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                val selectedItem = parent.getItemAtPosition(position).toString()
                Log.d("Spinner", "선택된 항목: $selectedItem")
                // 첫 번째 아이템이 아닌 경우에만 선택된 상태로 설정
                if (position != 0) {
                    methodSpinner.isSelected = true
                } else {
                    methodSpinner.isSelected = false
                }
                updateResetButtonVisibility()
                filterRecipes(searchEditText.text.toString())
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                // 선택되지 않은 경우 처리할 내용
            }
        }

        val typeItems = resources.getStringArray(R.array.type_list).toList()
        val typeAdapter = RecipeListSpinnerAdapter(requireContext(), typeItems)
        typeSpinner.adapter = typeAdapter
        typeSpinner.setSelection(0) // 초기 선택값 설정
        typeSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                val selectedItem = parent.getItemAtPosition(position).toString()
                Log.d("Spinner", "선택된 항목: $selectedItem")
                // 첫 번째 아이템이 아닌 경우에만 선택된 상태로 설정
                if (position != 0) {
                    typeSpinner.isSelected = true
                } else {
                    typeSpinner.isSelected = false
                }
                updateResetButtonVisibility()
                filterRecipes(searchEditText.text.toString())
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                // 선택되지 않은 경우 처리할 내용
            }
        }

        resetButton.setOnClickListener {
            resetSpinners()
            updateResetButtonVisibility()
            filterRecipes(searchEditText.text.toString())
        }

        // 더미 레시피 데이터 추가
        sampleRecipes = getDummyRecipeData()

        // 리사이클러뷰 설정
        recipeListView.layoutManager = LinearLayoutManager(requireContext())
        recipeListAdapter = RecipeListAdapter(
            sampleRecipes.toMutableList(),
            onBookmarkClick = { recipe ->
                recipe.isBookmarked = !recipe.isBookmarked
                recipeListAdapter.notifyDataSetChanged()
            },
            onHeartClick = { recipe ->
                recipe.isLiked = !recipe.isLiked
                recipeListAdapter.notifyDataSetChanged()
            }
        )
        recipeListView.adapter = recipeListAdapter

        // 검색어가 있으면 필터 적용
        if (!keyword.isNullOrEmpty()) {
            filterRecipes(keyword)
        }

        // 뒤로 가기 버튼
        backButton.setOnClickListener {
            parentFragmentManager.popBackStack()
        }


        // 키보드의 검색 버튼 누르면 검색 실행
        searchEditText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                val keyword = searchEditText.text.toString().trim()
                if (keyword.isNotEmpty()) {
                    filterRecipes(keyword)
                    resetSpinners()
                    updateResetButtonVisibility()
                } else {
                    Toast.makeText(requireContext(), "검색어를 입력하세요", Toast.LENGTH_SHORT).show()
                }
                true
            } else {
                false
            }
        }

        return view
    }

    /**
     * 레시피 필터 함수
     * 검색어, 카테고리 필터 두 조건을 모두 반영
     */
    private fun filterRecipes(keyword: String) {
        val selectedMethod = methodSpinner.selectedItem?.toString()
        val selectedType = typeSpinner.selectedItem?.toString()

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
            recipeListView.visibility = View.GONE
            emptyRecipeText.visibility = View.VISIBLE
        } else {
            recipeListView.visibility = View.VISIBLE
            emptyRecipeText.visibility = View.GONE
        }
    }

    /**
     * 스피너 필터 초기화
     */
    private fun resetSpinners() {
        methodSpinner.setSelection(0)
        typeSpinner.setSelection(0)
    }

    private fun getDummyRecipeData(): List<RecipeData> =
        listOf(
            RecipeData(
                id = BigInteger.valueOf(1),
                memberId = BigInteger.valueOf(1001),
                title = "블루베리 항산화 스무디",
                imageUrl = R.drawable.png_recipe_sample,
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
                imageUrl = R.drawable.png_recipe_sample,
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
                imageUrl = R.drawable.png_recipe_sample,
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
                imageUrl = R.drawable.png_recipe_sample,
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
                imageUrl = R.drawable.png_recipe_sample,
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
                imageUrl = R.drawable.png_recipe_sample,
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
