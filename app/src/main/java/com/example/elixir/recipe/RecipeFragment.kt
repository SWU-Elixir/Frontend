package com.example.elixir.recipe

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.Button
import android.widget.ImageButton
import android.widget.Spinner
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.example.elixir.R
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.tbuonomo.viewpagerdotsindicator.SpringDotsIndicator

data class RecipeItem(
    val recipeTitle: String,              // 레시피 이름
    val categorySlowAging: String,   // 레시피 카테고리
    val categoryType: String,
    val recipeIngredients: List<String>, // 사용된 재료 목록
    val recipeImageRes: Int? = null,     // 레시피 이미지 리소스 ID (nullable)

    val timeHours: Int,                  // 조리 시간 (시간 단위)
    val timeMinutes: Int,                // 조리 시간 (분 단위)
    val difficulty: String,              // 난이도 (예: 쉬움, 중간, 어려움)

    var isBookmarked: Boolean = false,   // 북마크 여부
    var isLiked: Boolean = false,        // 좋아요 클릭 여부
    val likeCount: Int                   // 좋아요 수
)

data class RecommendationRecipeItem(
    val recipeTitle: String,              // 레시피 이름
    val categorySlowAging: String,   // 레시피 카테고리
    val categoryType: String,
    val recipeIngredients: List<String>, // 사용된 재료 목록
    val recipeImageRes: Int? = null,     // 레시피 이미지 리소스 ID (nullable)
    var isBookmarked: Boolean = false,   // 북마크 여부
)

class RecipeFragment : Fragment() {

    private lateinit var searchButton: ImageButton

    private lateinit var recommendationViewPager: ViewPager2
    private lateinit var recommendationAdapter: RecipeRecommendationListAdapter
    private lateinit var dotsIndicator:SpringDotsIndicator

    private lateinit var recipeListView: RecyclerView
    private lateinit var recipeListAdapter: RecipeListAdapter

    private lateinit var methodSpinner: Spinner
    private lateinit var typeSpinner: Spinner

    private lateinit var resetButton: Button

    private lateinit var fab: FloatingActionButton

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_recipe, container, false)

        searchButton = view.findViewById(R.id.searchButton)
        recommendationViewPager = view.findViewById(R.id.recommendationList)
        dotsIndicator = view.findViewById(R.id.indicator)
        recipeListView = view.findViewById(R.id.recipeList)

        methodSpinner = view.findViewById(R.id.spinner_difficulty)
        typeSpinner = view.findViewById(R.id.spinner_type)
        resetButton = view.findViewById(R.id.resetButton)

        fab = view.findViewById(R.id.fab)

        // FAB 클릭 이벤트
        fab.setOnClickListener {
            Log.d("CalendarFragment", "FAB 클릭됨")
        }

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
                categoryType = "디저트/간식",
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
                categorySlowAging = "항산화 강화",
                categoryType = "디저트/간식",
                recipeIngredients = listOf("그릭요거트", "블루베리", "라즈베리"),
                isBookmarked = false,
                timeHours = 0,
                timeMinutes = 2,
                difficulty = "쉬움",
                isLiked = false,
                likeCount = 9
            )
        )

        val sampleRecommendationRecipes = listOf(
                RecommendationRecipeItem(
                    recipeTitle = "블루베리 요거트 스무디",
                    categorySlowAging = "항산화 강화",
                    categoryType = "음료/차",
                    recipeIngredients = listOf("블루베리", "요거트", "아몬드"),
                    recipeImageRes = R.drawable.png_recipe_sample,
                    isBookmarked = false
                ),
        RecommendationRecipeItem(
            recipeTitle = "견과류 토마토 샐러드",
            categorySlowAging = "항산화 강화",
            categoryType = "샐러드",
            recipeIngredients = listOf("방울토마토", "호두", "잣", "시금치"),
            recipeImageRes = R.drawable.png_recipe_sample,
            isBookmarked = true
        ),
        RecommendationRecipeItem(
            recipeTitle = "그릭요거트 과일볼",
            categorySlowAging = "항산화 강화",
            categoryType = "간식",
            recipeIngredients = listOf("그릭요거트", "블루베리", "딸기"),
            recipeImageRes = R.drawable.png_recipe_sample,
            isBookmarked = false
        )
        )

        // 어댑터 설정
        recommendationAdapter = RecipeRecommendationListAdapter(sampleRecommendationRecipes)
        recommendationViewPager.adapter = recommendationAdapter
        recommendationViewPager.setPageTransformer { page, position ->
            val absPos = kotlin.math.abs(position)
            page.scaleY = 0.85f + (1 - absPos) * 0.15f
            page.scaleX = 0.85f + (1 - absPos) * 0.15f
            page.translationX = -position * 40  // 간격 조절
        }

        // ViewPager2에 어댑터를 설정한 후에 DotsIndicator를 설정합니다.
        dotsIndicator.setViewPager2(recommendationViewPager)


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



        searchButton.setOnClickListener {
            Log.e("RecipeFragment", "검색 버튼 클릭")
            val transaction = parentFragmentManager.beginTransaction()
            transaction.replace(R.id.searchContainer, SearchFragment())
            transaction.addToBackStack(null)
            transaction.commit()
        }

//        // ViewPager 아이템 클릭 이벤트 처리
//        recommendationViewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
//            override fun onPageSelected(position: Int) {
//                super.onPageSelected(position)
//                val selectedRecipe = sampleRecipes[position]
//                Log.d("RecipeFragment", "선택된 레시피 이름: ${selectedRecipe.recipeTitle}")
//                // 여기에 상세 화면 이동 또는 다른 동작 추가 가능
//            }
//        })

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
