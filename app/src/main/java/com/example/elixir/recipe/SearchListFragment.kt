package com.example.elixir.recipe

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.elixir.R


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

    private lateinit var sampleRecipes: List<RecipeItem>

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
        val methodArg = arguments?.getString("method_filter")
        val typeArg = arguments?.getString("type_filter")
        searchEditText.setText(keyword)

        // EditText에 텍스트 변경 감지 리스너 등록
        searchEditText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val input = s.toString().trim()
                searchButton.setImageResource(
                    if (input.isNotEmpty()) R.drawable.ic_delete else R.drawable.ic_recipe_search
                )
                Log.d("EditText", "입력된 값: $input")
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

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
        sampleRecipes = getDummyData()

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

        // 검색 버튼 누르면 입력 초기화 및 SearchFragment로 이동
        searchButton.setOnClickListener {
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
            val keywordMatch = recipe.recipeTitle.contains(keyword, ignoreCase = true)
                    || recipe.recipeIngredients.any { it.contains(keyword, ignoreCase = true) }
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

    private fun getDummyData(): List<RecipeItem>{
        return listOf( // ← 여기 수정됨
            RecipeItem(
                recipeTitle = "블루베리 스무디",
                categorySlowAging = "항산화 강화",
                categoryType = "음료/차",
                recipeIngredients = listOf("블루베리", "요거트", "꿀"),
                recipeImageRes = R.drawable.png_recipe_sample,
                isBookmarked = false,
                timeHours = 0,
                timeMinutes = 5,
                difficulty = "쉬움",
                isLiked = false,
                likeCount = 23
            ),
            RecipeItem(
                recipeTitle = "아보카도 샐러드볼",
                categorySlowAging = "항산화 강화",
                categoryType = "샐러드/무침",
                recipeIngredients = listOf("아보카도", "방울토마토", "올리브오일", "레몬즙"),
                recipeImageRes = R.drawable.png_recipe_sample,
                isBookmarked = true,
                timeHours = 0,
                timeMinutes = 10,
                difficulty = "보통",
                isLiked = true,
                likeCount = 57674
            ),
            RecipeItem(
                recipeTitle = "간단한 토마토 올리브 오일 마리네이드",
                categorySlowAging = "항산화 강화",
                categoryType = "양념/소스/잼",
                recipeIngredients = listOf("토마토", "올리브오일", "허브", "소금"),
                recipeImageRes = R.drawable.png_recipe_sample,
                isBookmarked = false,
                timeHours = 0,
                timeMinutes = 7,
                difficulty = "쉬움",
                isLiked = false,
                likeCount = 999
            ),
            RecipeItem(
                recipeTitle = "그린 스무디",
                categorySlowAging = "항산화 강화",
                categoryType = "음료/차",
                recipeIngredients = listOf("케일", "바나나", "사과", "아몬드밀크"),
                recipeImageRes = R.drawable.png_recipe_sample,
                isBookmarked = false,
                timeHours = 0,
                timeMinutes = 3,
                difficulty = "쉬움",
                isLiked = true,
                likeCount = 1200
            ),
            RecipeItem(
                recipeTitle = "견과류 에너지볼",
                categorySlowAging = "항산화 강화",
                categoryType = "디저트",
                recipeIngredients = listOf("아몬드", "대추야자", "카카오닙스"),
                recipeImageRes = R.drawable.png_recipe_sample,
                isBookmarked = true,
                timeHours = 0,
                timeMinutes = 8,
                difficulty = "보통",
                isLiked = true,
                likeCount = 5534
            ),
            RecipeItem(
                recipeTitle = "그릭요거트 베리볼",
                categorySlowAging = "염증 감소",
                categoryType = "디저트",
                recipeIngredients = listOf("그릭요거트", "블루베리", "라즈베리"),
                isBookmarked = false,
                timeHours = 0,
                timeMinutes = 2,
                difficulty = "쉬움",
                isLiked = false,
                likeCount = 9
            )
        )
    }

}
