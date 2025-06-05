package com.example.elixir.recipe.ui

import android.app.Activity
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.AdapterView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.widget.ImageViewCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.elixir.R
import com.example.elixir.RetrofitClient
import com.example.elixir.ToolbarActivity
import com.example.elixir.adapter.RecommendRecipeKeywordAdapter
import com.example.elixir.databinding.FragmentRecipeBinding
import com.example.elixir.network.AppDatabase
import com.example.elixir.recipe.viewmodel.RecipeViewModel
import com.example.elixir.recipe.data.RecipeData
import com.example.elixir.recipe.data.RecipeRepository
import com.example.elixir.recipe.viewmodel.RecipeViewModelFactory
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.flexbox.JustifyContent
import com.google.gson.Gson
import kotlinx.coroutines.launch
import org.threeten.bp.LocalDateTime
import java.math.BigInteger

/**
 * 레시피 화면을 표시하는 프래그먼트
 * 추천 레시피와 전체 레시피 목록을 보여주며, 필터링 기능을 제공
 */
class RecipeFragment : Fragment() {

    private var _binding: FragmentRecipeBinding? = null
    private val binding get() = _binding!!

    // 데이터
    private var recipeList: MutableList<RecipeData> = mutableListOf()
    private lateinit var recipeListAdapter: RecipeListAdapter

    private lateinit var recipeRepository: RecipeRepository

    // 뷰모델
    private val recipeViewModel: RecipeViewModel by viewModels {
        RecipeViewModelFactory(recipeRepository)
    }

    private lateinit var recipeRegisterLauncher: ActivityResultLauncher<Intent>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRecipeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // db 초기화
        val recipeDao = AppDatabase.getInstance(requireContext()).recipeDao()
        val recipeApi = RetrofitClient.instanceRecipeApi
        recipeRepository = RecipeRepository(recipeApi, recipeDao)

        recipeViewModel.recipeList.observe(viewLifecycleOwner) { recipes ->
            recipeList.clear()
            recipeList.addAll(recipes)
            recipeListAdapter.updateData(recipeList)
        }

        // 레시피 불러오기
        recipeViewModel.getRecipes(0, 10, "항산화 강화", "한식")

        // FAB 클릭 이벤트 설정
        setupFabClickListener()

        // 스피너 설정
        setupSpinners()

        // 추천 레시피 ViewPager 설정
        setupRecommendationViewPager()
        
        // 추천 레시피 데이터 로드
        loadRecommendRecipe()

        // 레시피 리스트 설정
        setupRecipeList()

        // 검색 버튼 클릭 이벤트 설정
        setupSearchButton()

        // 레시피 등록을 위한 ActivityResultLauncher 설정
        recipeRegisterLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data = result.data?.getStringExtra("recipeData")
                if (data != null) {
                    val newRecipe = Gson().fromJson(data, RecipeData::class.java)
                    recipeList.add(newRecipe)
                    recipeListAdapter.updateData(recipeList)
                }
            }
        }
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
            val intent = Intent(requireContext(), ToolbarActivity::class.java).apply {
                putExtra("mode", 9) // 레시피 등록 모드
            }
            recipeRegisterLauncher.launch(intent)
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
     * 추천 레시피 ViewPager 설정
     */
    private fun setupRecommendationViewPager() {
        val recommendationAdapter = RecipeRecommendationListAdapter(recipeList)
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
            recipeList,
            // 북마크 버튼 클릭 이벤트 처리
            onBookmarkClick = { recipe ->
                // 북마크 상태 토글
                recipe.scrappedByCurrentUser = !recipe.scrappedByCurrentUser
                // 변경된 상태를 리스트에 반영
                recipeListAdapter.notifyItemChanged(recipeList.indexOf(recipe))
            },
            // 좋아요 버튼 클릭 이벤트 처리
            onHeartClick = { recipe ->
                // 좋아요 상태 토글
                recipe.likedByCurrentUser = !recipe.likedByCurrentUser
                // 변경된 상태를 리스트에 반영
                recipeListAdapter.notifyItemChanged(recipeList.indexOf(recipe))
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
        val filtered = recipeList.filter { recipe ->
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

    private fun loadRecommendRecipe() {
        lifecycleScope.launch {
            try {
                val api = RetrofitClient.instanceRecipeApi
                val response = api.getRecipeByRecommend()
                if (response.isSuccessful) {
                    response.body()?.data?.let { recommendList ->
                        val recipeList = recommendList.map { recommend ->
                            RecipeData(
                                email = "recommend.id",
                                title = recommend.title,
                                imageUrl = recommend.imageUrl ?: "android.resource://com.example.elixir/${R.drawable.ic_recipe_white}",
                                categorySlowAging = recommend.categorySlowAging,
                                categoryType = recommend.categoryType,
                                ingredients = mapOf(),
                                scrappedByCurrentUser = recommend.scrappedByCurrentUser,
                                // 기본값 설정
                                description = "",
                                difficulty = "",
                                timeHours = 0,
                                timeMinutes = 0,
                                ingredientTagIds = recommend.ingredientTagIds,
                                seasoning = mapOf(),
                                stepDescriptions = listOf(),
                                stepImageUrls = listOf(),
                                tips = "",
                                allergies = listOf(),
                                authorFollowByCurrentUser = false,
                                likedByCurrentUser = false,
                                authorNickname = "",
                                authorTitle = "",
                                likes = 0,
                                scraps = 0,
                                createdAt = LocalDateTime.now(),
                                updatedAt = LocalDateTime.now()
                            )
                        }
                        
                        // ViewPager2 어댑터 업데이트
                        (binding.recommendationList.adapter as? RecipeRecommendationListAdapter)?.let { adapter ->
                            adapter.updateData(recipeList)
                        } ?: run {
                            // 어댑터가 없는 경우 새로 생성
                            binding.recommendationList.adapter = RecipeRecommendationListAdapter(recipeList)
                        }
                    }
                } else {
                    Log.e(TAG, "추천 레시피 로드 실패: ${response.message()}")
                }
            } catch (e: Exception) {
                Log.e(TAG, "추천 레시피 로드 실패", e)
            }
        }
    }

    /**
     * 더미 레시피 데이터 생성
     */
//    private fun getDummyRecipeData(): List<RecipeData> {
//        return listOf(
//            RecipeData(
//            title = "블루베리 항산화 스무디",
//            description = "블루베리와 그릭요거트를 활용한 항산화 스무디 레시피입니다.",
//            categorySlowAging = "항산화 강화",
//            categoryType = "음료/차",
//            difficulty = "쉬움",
//            timeHours = 0,
//            timeMinutes = 5,
//            ingredientTagIds = listOf(1, 2), // 예시 태그 ID
//            ingredients = mapOf(
//                "블루베리" to "100g",
//                "그릭요거트" to "150g"
//            ),
//            seasoning = mapOf(
//                "얼음" to "적당량",
//                "시나몬 파우더" to "1작은술"
//            ),
//            stepDescriptions = listOf(
//                "모든 재료를 믹서에 넣는다",
//                "곱게 갈아 컵에 담는다"
//            ),
//            stepImageUrls = listOf(
//                "android.resource://com.example.elixir.recipe/${R.drawable.img_blank}",
//                "android.resource://com.example.elixir.recipe/${R.drawable.img_blank}"
//            ),
//            tips = "시나몬을 추가하면 향과 항산화 성분이 강화됩니다.",
//            allergies = listOf("우유"),
//            imageUrl = "android.resource://com.example.elixir.recipe/${R.drawable.img_blank}",
//            authorFollowByCurrentUser = false,
//            likedByCurrentUser = false,
//            scrappedByCurrentUser = false,
//            authorNickname = "헬시마스터",
//            authorTitle = "영양사",
//            likes = 42234,
//            scraps = 1234,
//            createdAt = LocalDateTime.of(2025, 4, 22, 0, 0),
//            updatedAt = LocalDateTime.of(2025, 4, 22, 0, 0)
//        ))
//    }
}
