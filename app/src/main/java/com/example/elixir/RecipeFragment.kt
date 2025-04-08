package com.example.elixir

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import com.tbuonomo.viewpagerdotsindicator.SpringDotsIndicator

data class RecipeItem(
    val recipeName: String,
    val recipeCategory: List<String>,
    val recipeIngredients: List<String>,
    val recipeImageRes: Int? = null,
    val bookmark: Boolean = false
)

class RecipeListFragment : Fragment() {

    private lateinit var viewPager: ViewPager2
    private lateinit var adapter: RecipeRecommendationListAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_recipe, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val searchButton = view.findViewById<ImageButton>(R.id.searchButton)
        // val recommendationTitle = view.findViewById<TextView>(R.id.recommendationTitle)
        viewPager = view.findViewById(R.id.recommendationList)
        val dotsIndicator = view.findViewById<SpringDotsIndicator>(R.id.indicator)

        // 더미 데이터 추가
        val sampleRecipes = listOf(
            RecipeItem("블루베리 스무디", listOf("음료/차", "항산화 강화"), listOf("블루베리", "요거트", "꿀")),
            RecipeItem("아보카도 토스트", listOf("건강식", "브런치"), listOf("아보카도", "식빵", "올리브 오일")),
            RecipeItem("단호박 수프", listOf("수프", "비타민"), listOf("단호박", "우유", "소금"))
        )

        // 어댑터 설정
        adapter = RecipeRecommendationListAdapter(sampleRecipes)
        viewPager.adapter = adapter

        // Indicator와 연결
        dotsIndicator.setViewPager2(viewPager)

        // 검색 버튼 클릭 이벤트 예제
        searchButton.setOnClickListener {
            Log.e("RecipeFragment", "검색 버튼 클릭")
        }
    }
}
