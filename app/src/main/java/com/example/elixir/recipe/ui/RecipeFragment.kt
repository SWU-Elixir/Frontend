package com.example.elixir.recipe.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.elixir.R
import com.example.elixir.ToolbarActivity
import com.example.elixir.databinding.FragmentRecipeBinding
import com.example.elixir.recipe.viewmodel.RecipeViewModel
import com.example.elixir.recipe.data.RecipeData
import java.math.BigInteger

/**
 * 레시피 화면을 표시하는 프래그먼트
 * 추천 레시피와 전체 레시피 목록을 보여주며, 필터링 기능을 제공
 */
class RecipeFragment : Fragment() {

    private var _binding: FragmentRecipeBinding? = null
    private val binding get() = _binding!!

    // 데이터
    private lateinit var sampleRecipes: List<RecipeData>
    private lateinit var recipeListAdapter: RecipeListAdapter

    // 뷰모델
    private lateinit var recipeViewModel: RecipeViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRecipeBinding.inflate(inflater, container, false)
        
        // FAB 클릭 이벤트 설정
        setupFabClickListener()

        // 스피너 설정
        setupSpinners()

        // 더미 데이터 초기화 및 어댑터 설정
        initializeDataAndAdapters()

        // 검색 버튼 클릭 이벤트 설정
        setupSearchButton()
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    /**
     * FAB 클릭 이벤트 설정
     */
    private fun setupFabClickListener() {
        binding.fab.setOnClickListener {
            val intent = Intent(requireContext(), ToolbarActivity::class.java)
            intent.putExtra("mode", 9) // 레시피 등록 모드
            startActivity(intent)
        }
    }

    /**
     * 리셋 버튼 표시 여부 업데이트
     * spinner 2개 중 하나라도 선택되어 있을 시 리셋 버튼 표시
     */
    private fun updateResetButtonVisibility() {
        val isMethodSelected = binding.spinnerDifficulty.selectedItemPosition != 0
        val isTypeSelected = binding.spinnerType.selectedItemPosition != 0
        binding.resetButton.visibility = if (isMethodSelected || isTypeSelected) View.VISIBLE else View.GONE
    }

    /**
     * 스피너 설정 및 이벤트 처리
     */
    private fun setupSpinners() {
        // 저속노화 방법 스피너 설정
        setupMethodSpinner()
        
        // 레시피 종류 스피너 설정
        setupTypeSpinner()
        
        // 리셋 버튼 클릭 이벤트 설정
        setupResetButton()
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
                // 첫 번째 항목(기본값)이 아닌 경우에만 선택된 상태로 표시
                binding.spinnerDifficulty.isSelected = position != 0
                // 리셋 버튼 표시 여부 업데이트
                updateResetButtonVisibility()
                // 선택된 필터 조건에 따라 레시피 목록 필터링
                filterRecipes()
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
                // 첫 번째 항목(기본값)이 아닌 경우에만 선택된 상태로 표시
                binding.spinnerType.isSelected = position != 0
                // 리셋 버튼 표시 여부 업데이트
                updateResetButtonVisibility()
                // 선택된 필터 조건에 따라 레시피 목록 필터링
                filterRecipes()
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }

    /**
     * 리셋 버튼 클릭 이벤트 설정
     */
    private fun setupResetButton() {
        binding.resetButton.setOnClickListener {
            resetSpinners()
            updateResetButtonVisibility()
        }
    }

    /**
     * 데이터 초기화 및 어댑터 설정
     */
    private fun initializeDataAndAdapters() {
        // 더미 데이터 초기화
        sampleRecipes = getDummyRecipeData()

        // 추천 레시피 ViewPager 설정
        setupRecommendationViewPager()

        // 레시피 리스트 설정
        setupRecipeList()
    }

    /**
     * 추천 레시피 ViewPager 설정
     */
    private fun setupRecommendationViewPager() {
        val recommendationAdapter = RecipeRecommendationListAdapter(sampleRecipes)
        binding.recommendationList.adapter = recommendationAdapter
        
        // 페이지 전환 애니메이션 설정
        binding.recommendationList.setPageTransformer { page, position ->
            val absPos = kotlin.math.abs(position)
            page.scaleY = 0.85f + (1 - absPos) * 0.15f
            page.scaleX = 0.85f + (1 - absPos) * 0.15f
            page.translationX = -position * 40
        }

        // DotsIndicator를 ViewPager2에 연결
        binding.indicator.attachTo(binding.recommendationList)
    }

    /**
     * 레시피 리스트 설정
     * RecyclerView와 어댑터를 초기화하고 북마크, 좋아요 기능을 설정
     */
    private fun setupRecipeList() {
        // RecyclerView에 LinearLayoutManager 설정
        // 세로 방향으로 아이템을 배치하고 스크롤 가능하도록 함
        binding.recipeList.layoutManager = LinearLayoutManager(requireContext())

        // RecipeListAdapter 초기화
        recipeListAdapter = RecipeListAdapter(
            // 전체 레시피 데이터 전달
            sampleRecipes,
            // 북마크 버튼 클릭 이벤트 처리
            onBookmarkClick = { recipe ->
                // 북마크 상태 토글
                recipe.isBookmarked = !recipe.isBookmarked
                // 변경된 상태를 리스트에 반영
                recipeListAdapter.notifyItemChanged(sampleRecipes.indexOf(recipe))
            },
            // 좋아요 버튼 클릭 이벤트 처리
            onHeartClick = { recipe ->
                // 좋아요 상태 토글
                recipe.isLiked = !recipe.isLiked
                // 변경된 상태를 리스트에 반영
                recipeListAdapter.notifyItemChanged(sampleRecipes.indexOf(recipe))
            },
            // 프래그먼트 전환을 위한 FragmentManager 전달
            fragmentManager = parentFragmentManager
        )
        // RecyclerView에 어댑터 설정
        binding.recipeList.adapter = recipeListAdapter
    }

    /**
     * 검색 버튼 클릭 이벤트 설정
     * 전체 화면으로 SearchFragment 전환
     */
    private fun setupSearchButton() {
        binding.searchButton.setOnClickListener {
            val transaction = parentFragmentManager.beginTransaction()
            transaction.replace(R.id.fullscreenContainer, SearchFragment())
            transaction.addToBackStack(null)
            transaction.commit()
        }
    }

    /**
     * 필터 조건에 따라 레시피 리스트 필터링
     * 선택된 저속노화 방법과 레시피 종류에 따라 레시피 목록을 필터링하고 UI를 업데이트
     */
    private fun filterRecipes() {
        // 스피너에서 선택된 저속노화 방법과 레시피 종류 가져오기
        val selectedMethod = binding.spinnerDifficulty.selectedItem?.toString()
        val selectedType = binding.spinnerType.selectedItem?.toString()

        // 선택된 조건에 따라 레시피 필터링
        val filtered = sampleRecipes.filter { recipe ->
            // 저속노화 방법 필터링 조건
            // "저속노화"가 선택된 경우 모든 레시피 포함, 그 외에는 선택된 방법과 일치하는 레시피만 포함
            val methodMatch = selectedMethod == "저속노화" || recipe.categorySlowAging == selectedMethod
            // 레시피 종류 필터링 조건
            // "종류"가 선택된 경우 모든 레시피 포함, 그 외에는 선택된 종류와 일치하는 레시피만 포함
            val typeMatch = selectedType == "종류" || recipe.categoryType == selectedType
            // 두 조건이 모두 만족하는 레시피만 필터링
            methodMatch && typeMatch
        }

        // 필터링된 레시피 목록으로 어댑터 데이터 업데이트
        (binding.recipeList.adapter as RecipeListAdapter).updateData(filtered)

        // 필터링 결과에 따른 UI 업데이트
        // 결과가 없는 경우 빈 화면 표시, 있는 경우 레시피 목록 표시
        updateFilteredUI(filtered)
    }

    /**
     * 필터링 결과에 따른 UI 업데이트
     */
    private fun updateFilteredUI(filtered: List<RecipeData>) {
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

    /**
     * 더미 레시피 데이터 생성
     */
    private fun getDummyRecipeData(): List<RecipeData> =
        listOf(
            RecipeData(
                id = BigInteger.valueOf(1),
                memberId = BigInteger.valueOf(1001),
                title = "블루베리 항산화 스무디",
                imageUrl = "android.resource://com.example.elixir.recipe/${R.drawable.img_blank}",
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
                likeCount = 42234
            ),
            RecipeData(
                id = BigInteger.valueOf(2),
                memberId = BigInteger.valueOf(1001),
                title = "아보카도 혈당 조절 샐러드",
                imageUrl = "android.resource://com.example.elixir.recipe/${R.drawable.img_blank}",
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
                likeCount = 1293
            ),
            RecipeData(
                id = BigInteger.valueOf(3),
                memberId = BigInteger.valueOf(1001),
                title = "토마토 올리브 항염 마리네이드",
                imageUrl = "android.resource://com.example.elixir.recipe/${R.drawable.img_blank}",
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
                likeCount = 42234
            ),
            RecipeData(
                id = BigInteger.valueOf(4),
                memberId = BigInteger.valueOf(1001),
                title = "케일 항염 그린 스무디",
                imageUrl = "android.resource://com.example.elixir.recipe/${R.drawable.img_blank}",
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
                imageUrl = "android.resource://com.example.elixir.recipe/${R.drawable.img_blank}",
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
                imageUrl = "android.resource://com.example.elixir.recipe/${R.drawable.img_blank}",
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
