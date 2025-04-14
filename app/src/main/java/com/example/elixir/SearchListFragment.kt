package com.example.elixir

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Spinner
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.flexbox.JustifyContent


class SearchListFragment : Fragment() {

    private lateinit var searchButton: ImageButton
    private lateinit var backButton: ImageButton
    private lateinit var searchEditText: EditText

    private lateinit var recipeListView: RecyclerView
    private lateinit var recipeListAdapter: RecipeListAdapter

    private lateinit var methodSpinner: Spinner
    private lateinit var typeSpinner: Spinner

    private lateinit var resetButton: Button

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

        // 전달받은 값 꺼내기
        val keyword = arguments?.getString("search_keyword")
        // EditText에 미리 입력값 세팅
        searchEditText.setText(keyword)

        // 텍스트 변경 감지
        searchEditText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                Log.d("EditText", "입력된 값: ${s.toString()}")
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        fun updateResetButtonVisibility() {
            val isMethodSelected = methodSpinner.selectedItemPosition != 0
            val isTypeSelected = typeSpinner.selectedItemPosition != 0
            resetButton.visibility = if (isMethodSelected || isTypeSelected) View.VISIBLE else View.GONE
        }

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
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                // 선택되지 않은 경우 처리할 내용
            }
        }

        resetButton.setOnClickListener {
            // 모든 스피너를 첫 번째 아이템으로 설정하는 로직
            resetSpinners()
            updateResetButtonVisibility()
        }


        // 더미 데이터 추가
        val sampleRecipes = listOf(
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
                likeCount = 57
            ),
            RecipeItem(
                recipeTitle = "간단한 토마토 올리브 오일 마리네이드",
                categorySlowAging = "항산화 강화",
                categoryType = "반찬/마리네이드",
                recipeIngredients = listOf("토마토", "올리브오일", "허브", "소금"),
                recipeImageRes = R.drawable.png_recipe_sample,
                isBookmarked = false,
                timeHours = 0,
                timeMinutes = 7,
                difficulty = "쉬움",
                isLiked = false,
                likeCount = 14
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
                likeCount = 31
            ),
            RecipeItem(
                recipeTitle = "견과류 에너지볼",
                categorySlowAging = "항산화 강화",
                categoryType = "디저트/간식",
                recipeIngredients = listOf("아몬드", "대추야자", "카카오닙스"),
                recipeImageRes = R.drawable.png_recipe_sample,
                isBookmarked = true,
                timeHours = 0,
                timeMinutes = 8,
                difficulty = "보통",
                isLiked = true,
                likeCount = 49
            ),
            RecipeItem(
                recipeTitle = "그릭요거트 베리볼",
                categorySlowAging = "항산화 강화",
                categoryType = "디저트/간식",
                recipeIngredients = listOf("그릭요거트", "블루베리", "라즈베리"),
                recipeImageRes = R.drawable.png_recipe_sample,
                isBookmarked = false,
                timeHours = 0,
                timeMinutes = 2,
                difficulty = "쉬움",
                isLiked = false,
                likeCount = 9
            )
        )

        // RecyclerView에 LinearLayoutManager 설정
        recipeListView.layoutManager = LinearLayoutManager(requireContext())

        // 어댑터 초기화 및 클릭 이벤트 처리
        recipeListAdapter = RecipeListAdapter(
            sampleRecipes,
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

        // 뒤로가기: 프래그먼트 닫기 (BackStack)
        backButton.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        // 검색 버튼 → 프래그먼트 닫고 다른 곳에 새 프래그먼트 띄우기
        searchButton.setOnClickListener {
            // EditText에서 텍스트 가져오기
            val keyword = searchEditText.text.toString().trim()

            if (keyword.isNotEmpty()) {
                // 원하는 로직 실행: 예시로 로그 출력
                Log.d("SearchFragment", "검색어: $keyword")

                // 새로운 프래그먼트 생성 + Bundle 전달
                val searchListFragment = SearchListFragment()
                val bundle = Bundle().apply {
                    putString("search_keyword", keyword)
                }
                searchListFragment.arguments = bundle

                // 현재 프래그먼트 닫기
                parentFragmentManager.popBackStack()

                // 새로운 프래그먼트로 전환
                val transaction = parentFragmentManager.beginTransaction()
                transaction.replace(R.id.fragmentContainer, searchListFragment)
                transaction.addToBackStack(null)
                transaction.commit()

            } else {
                Toast.makeText(requireContext(), "검색어를 입력하세요", Toast.LENGTH_SHORT).show()
            }
        }



        return view
    }

    private fun resetSpinners() {
        // methodSpinner를 첫 번째 아이템으로 설정
        methodSpinner.setSelection(0)

        // typeSpinner를 첫 번째 아이템으로 설정
        typeSpinner.setSelection(0)

        // 필요하다면 다른 스피너들에 대해서도 동일한 작업을 수행
    }
}