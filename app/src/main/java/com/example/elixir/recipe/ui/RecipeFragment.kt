package com.example.elixir.recipe.ui

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.elixir.R
import com.example.elixir.RetrofitClient
import com.example.elixir.ToolbarActivity
import com.example.elixir.databinding.FragmentRecipeBinding
import com.example.elixir.ingredient.data.IngredientData
import com.example.elixir.ingredient.network.IngredientDB
import com.example.elixir.ingredient.network.IngredientRepository
import com.example.elixir.ingredient.viewmodel.IngredientService
import com.example.elixir.ingredient.viewmodel.IngredientViewModel
import com.example.elixir.ingredient.viewmodel.IngredientViewModelFactory
import com.example.elixir.network.AppDatabase
import com.example.elixir.recipe.viewmodel.RecipeViewModel
import com.example.elixir.recipe.data.RecipeData
import com.example.elixir.recipe.data.RecipeListItemData
import com.example.elixir.recipe.data.RecipeRepository
import com.example.elixir.recipe.viewmodel.RecipeViewModelFactory
import com.google.gson.Gson
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/**
 * 레시피 화면을 표시하는 프래그먼트
 * 추천 레시피와 전체 레시피 목록을 보여주며, 필터링 기능을 제공
 */
class RecipeFragment : Fragment() {

    companion object {
        private const val TAG = "RecipeFragment"
    }

    private var _binding: FragmentRecipeBinding? = null
    private val binding get() = _binding!!

    // 데이터
    private var recipeList: MutableList<RecipeData> = mutableListOf()
    private var recommendRecipeList: List<RecipeListItemData> = emptyList()
    private lateinit var recipeListAdapter: RecipeListAdapter

    private lateinit var recipeRepository: RecipeRepository
    private lateinit var ingredientViewModel: IngredientViewModel

    private var selectedCategoryType: String? = null
    private var selectedSlowAging: String? = null

    // 페이징 함수
    private var isLoading = false
    private var currentPage = 0
    private val pageSize = 10

    // 뷰모델
    private val recipeViewModel: RecipeViewModel by viewModels {
        RecipeViewModelFactory(recipeRepository)
    }

    private lateinit var recipeRegisterLauncher: ActivityResultLauncher<Intent>

    // 데이터 상태 관리
    private var latestRecipeList: List<RecipeData>? = null
    private var ingredientDataMap: Map<Int, IngredientData>? = null
    private var isDataInitialized = false // 데이터 초기화 상태 추가

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRecipeBinding.inflate(inflater, container, false)
        // 다른 프래그먼트에서 다시 레시피 프래그먼트로 돌아왔을 때 갱신
        parentFragmentManager.setFragmentResultListener("refresh_recipes", this) { _, _ ->
            Log.d("RecipeFragment", "recipeViewModel.getRecipes 갱신")
            recipeViewModel.setCategoryType(selectedCategoryType)
            recipeViewModel.setCategorySlowAging(selectedSlowAging)
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 항상 초기화 진행 (중복 초기화 방지는 내부에서 처리)
        initializeData()
        setupUI()
    }

    private fun initializeData() {
        // 이미 초기화된 경우에도 Observer는 다시 설정
        if (isDataInitialized) {
            setupObservers()
            // 기존 데이터가 있다면 바로 어댑터 업데이트
            if (latestRecipeList != null) {
                tryInitAdapter()
            }
            return
        }

        // db 초기화
        val recipeDao = AppDatabase.getInstance(requireContext()).recipeDao()
        val recipeApi = RetrofitClient.instanceRecipeApi
        recipeRepository = RecipeRepository(recipeApi, recipeDao)

        // 식재료 ViewModel 초기화
        val ingredientRepository = IngredientRepository(
            RetrofitClient.instanceIngredientApi,
            IngredientDB.getInstance(requireContext()).ingredientDao()
        )
        val ingredientService = IngredientService(ingredientRepository)
        ingredientViewModel = ViewModelProvider(this,
            IngredientViewModelFactory(ingredientService))[IngredientViewModel::class.java]

        // Observer 설정 (항상 실행)
        setupObservers()

        // 데이터 로딩
        Log.d("RecipeFragment", "Loading ingredient data...")
        ingredientViewModel.loadIngredients()
        Log.d("RecipeFragment", "Loading recipe data...")
        recipeViewModel.setCategoryType(selectedCategoryType)
        recipeViewModel.setCategorySlowAging(selectedSlowAging)

        isDataInitialized = true
    }

    private fun setupObservers() {
        // observe: 레시피 데이터
        recipeViewModel.recipeList.observe(viewLifecycleOwner) { recipes ->
            Log.d("RecipeFragment", "Recipe data received: ${recipes?.size ?: 0} items")
            latestRecipeList = recipes
            tryInitAdapter()
        }

        // 2. 페이징 Flow Collector (페이징 적용 시)
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                recipeViewModel.recipes.collectLatest { pagingData ->
                    recipeListAdapter.submitData(pagingData)
                }
            }
        }

        recipeViewModel.deleteResult.observe(viewLifecycleOwner) {
            recipeViewModel.setCategoryType(selectedCategoryType)
            recipeViewModel.setCategorySlowAging(selectedSlowAging)
        }

        // observe: 식재료 데이터 (항상 업데이트)
        ingredientViewModel.ingredients.observe(viewLifecycleOwner) { ingredientList ->
            Log.d("RecipeFragment", "Ingredient data received: ${ingredientList?.size ?: 0} items")
            if (ingredientList != null && ingredientList.isNotEmpty()) {
                ingredientDataMap = ingredientList.associateBy { it.id }
                Log.d("RecipeFragment", "Ingredient map created with ${ingredientDataMap?.size} items")
                // 샘플 로그 추가
                ingredientList.take(3).forEach { ingredient ->
                    Log.d("RecipeFragment", "Sample ingredient: ${ingredient.name} (ID: ${ingredient.id})")
                }
                tryInitAdapter()
            } else {
                Log.d("RecipeFragment", "Ingredient list is null or empty")
            }
        }
    }

    /*private fun loadMore() {
        Log.d("RecipeFragment", "loadMore called")
        currentPage++
        recipeViewModel.getRecipes(currentPage, pageSize, selectedCategoryType, selectedSlowAging)
    }*/

    private fun setupUI() {
        // 레이아웃 매니저 설정
        binding.recipeList.layoutManager = LinearLayoutManager(requireContext())

        // FAB, 스피너, 검색 등 UI 설정
        setupFabClickListener()
        setupSpinners()
        setupRecommendationViewPager()
        setupSearchButton()

        // 추천 레시피 데이터 로드 (한 번만)
        if (recommendRecipeList.isEmpty()) {
            loadRecommendRecipe()
        }


        setupRecipeRegisterLauncher()
    }

    private fun setupRecipeRegisterLauncher() {
        recipeRegisterLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data = result.data?.getStringExtra("recipeData")
                if (data != null) {
                    val newRecipe = Gson().fromJson(data, RecipeData::class.java)
                    recipeList.add(newRecipe)
                    recipeViewModel.setCategoryType(selectedCategoryType)
                    recipeViewModel.setCategorySlowAging(selectedSlowAging)
                }
            }
        }
    }

    // 두 데이터가 모두 준비됐을 때만 어댑터 초기화
    private fun tryInitAdapter() {
        val recipes = latestRecipeList
        val ingredientMap = ingredientDataMap

        Log.d("RecipeFragment", "tryInitAdapter called - recipes: ${recipes?.size}, ingredients: ${ingredientMap?.size}")

        // 레시피 데이터만 있어도 일단 어댑터 초기화 (식재료는 나중에 업데이트)
        if (recipes != null) {
            // 식재료 맵이 없으면 빈 맵으로 초기화
            val safeIngredientMap = ingredientMap ?: emptyMap()

            Log.d("RecipeFragment", "Using ingredient map size: ${safeIngredientMap.size}")

            if (recipes.isNotEmpty()) {
                if (!::recipeListAdapter.isInitialized) {
                    recipeList = recipes.toMutableList()
                    recipeListAdapter = RecipeListAdapter( safeIngredientMap,
                        onBookmarkClick = { recipe ->
                            recipe.scrappedByCurrentUser = !recipe.scrappedByCurrentUser
                            if (recipe.scrappedByCurrentUser) {
                                recipeViewModel.addScrap(recipe.id)
                            } else {
                                recipeViewModel.deleteScrap(recipe.id)
                            }
                        },
                        onHeartClick = { recipe ->
                            recipe.likedByCurrentUser = !recipe.likedByCurrentUser
                            if (recipe.likedByCurrentUser) {
                                recipe.likes++
                                recipeViewModel.addLike(recipe.id)
                            } else {
                                recipe.likes--
                                recipeViewModel.deleteLike(recipe.id)
                            }
                        },
                        fragmentManager = parentFragmentManager
                    )

                    binding.recipeList.adapter = recipeListAdapter
                } else {
                    // 이미 초기화된 어댑터라면 데이터 업데이트
                    recipeViewModel.setCategoryType(selectedCategoryType)
                    recipeViewModel.setCategorySlowAging(selectedSlowAging)

                    // 식재료 데이터가 있다면 어댑터에 업데이트
                    if (safeIngredientMap.isNotEmpty()) {
                        // RecipeListAdapter에 updateIngredientMap 메서드가 있는지 확인 필요
                        try {
                            val updateMethod = recipeListAdapter.javaClass.getMethod("updateIngredientMap", Map::class.java)
                            updateMethod.invoke(recipeListAdapter, safeIngredientMap)
                            Log.d("RecipeFragment", "Successfully updated ingredient map in adapter")
                        } catch (e: Exception) {
                            Log.w("RecipeFragment", "updateIngredientMap method not found, calling notifyDataSetChanged()")
                            // 리플렉션이 실패하면 전체 데이터를 다시 설정
                            recipeListAdapter = RecipeListAdapter(
                                safeIngredientMap,
                                onBookmarkClick = { recipe ->
                                    recipe.scrappedByCurrentUser = !recipe.scrappedByCurrentUser
                                    if (recipe.scrappedByCurrentUser) {
                                        recipeViewModel.addScrap(recipe.id)
                                    } else {
                                        recipeViewModel.deleteScrap(recipe.id)
                                    }
                                },
                                onHeartClick = { recipe ->
                                    recipe.likedByCurrentUser = !recipe.likedByCurrentUser
                                    if (recipe.likedByCurrentUser) {
                                        recipe.likes++
                                        recipeViewModel.addLike(recipe.id)
                                    } else {
                                        recipe.likes--
                                        recipeViewModel.deleteLike(recipe.id)
                                    }
                                },
                                fragmentManager = parentFragmentManager
                            )
                            binding.recipeList.adapter = recipeListAdapter
                        }
                    }
                }
                binding.recipeList.visibility = View.VISIBLE
                binding.emptyRecipeText.visibility = View.GONE
                Log.d("RecipeFragment", "RecipeList is now VISIBLE")
            } else {
                binding.recipeList.visibility = View.GONE
                binding.emptyRecipeText.visibility = View.VISIBLE
                Log.d("RecipeFragment", "No recipes - showing empty text")
            }
        } else {
            Log.d("RecipeFragment", "Recipes data is null - waiting for data")
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    // 등록 설정
    private fun setupFabClickListener() {
        binding.fab.setOnClickListener {
            val intent = Intent(requireContext(), ToolbarActivity::class.java).apply {
                putExtra("mode", 9) // 레시피 등록 모드
            }
            recipeRegisterLauncher.launch(intent)
            // 데이터 갱신
            recipeViewModel.setCategoryType(selectedCategoryType)
            recipeViewModel.setCategorySlowAging(selectedSlowAging)
        }
    }

    // 검색 스피너 리셋 버튼 활성화
    private fun updateResetButtonVisibility() {
        val isMethodSelected = binding.spinnerDifficulty.selectedItemPosition != 0
        val isTypeSelected = binding.spinnerType.selectedItemPosition != 0
        binding.resetButton.visibility =
            if (isMethodSelected || isTypeSelected) View.VISIBLE else View.GONE
    }

    // 스피너 설정 및 이벤트 처리
    private fun setupSpinners() {
        // 저속노화 방법 스피너 설정
        setupMethodSpinner()

        // 레시피 종류 스피너 설정
        setupTypeSpinner()

        // 리셋 버튼 클릭 이벤트 설정
        setupResetButton()
    }

    // 저속노화 스피너
    private fun setupMethodSpinner() {
        val methodItems = resources.getStringArray(R.array.method_list).toList()
        val methodAdapter = RecipeListSpinnerAdapter(requireContext(), methodItems)

        binding.spinnerDifficulty.adapter = methodAdapter
        binding.spinnerDifficulty.setSelection(0)
        binding.spinnerDifficulty.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    if (position == 0 && parent.count > 1) {
                        // 첫 번째 항목 선택 시 두 번째로 이동
                        selectedSlowAging = null
                        return
                    }
                    selectedSlowAging = parent.getItemAtPosition(position).toString()

                    // ViewModel 함수 호출
                    recipeViewModel.setCategoryType(selectedCategoryType)
                    recipeViewModel.setCategorySlowAging(selectedSlowAging)
                    Log.d("RecipeFragment", "Method selected: $selectedSlowAging")
                }

                override fun onNothingSelected(parent: AdapterView<*>) {}
            }
    }

    // 레시피 종류 스피너
    private fun setupTypeSpinner() {
        val typeItems = resources.getStringArray(R.array.type_list).toList()
        val typeAdapter = RecipeListSpinnerAdapter(requireContext(), typeItems)
        binding.spinnerType.adapter = typeAdapter
        binding.spinnerType.setSelection(0)
        binding.spinnerType.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View?,
                position: Int,
                id: Long
            ) {
                if (position == 0 && parent.count > 1) {
                    // 첫 번째 항목 선택 시 두 번째로 자동 이동
                    selectedCategoryType = null
                    return
                }

                selectedCategoryType = parent.getItemAtPosition(position).toString()
                if(selectedCategoryType == "음료/차")
                    selectedCategoryType = "음료_차"
                else if(selectedCategoryType == "양념/소스/잼")
                    selectedCategoryType = "양념_소스_잼"

                // ViewModel 함수 호출
                recipeViewModel.setCategoryType(selectedCategoryType)
                recipeViewModel.setCategorySlowAging(selectedSlowAging)
                Log.d("RecipeFragment", "Type selected: $selectedCategoryType")
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }

    // 리셋 버튼 클릭 이벤트
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
        val recommendationAdapter = RecipeRecommendationListAdapter(recommendRecipeList, fragmentManager = parentFragmentManager, recipeViewModel)
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
        //(binding.recipeList.adapter as RecipeListAdapter).updateData(filtered)

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
                        recommendRecipeList = recommendList
                        // ViewPager2 어댑터 업데이트
                        (binding.recommendationList.adapter as? RecipeRecommendationListAdapter)?.updateData(recommendList)
                            ?: run {
                                // 어댑터가 없는 경우 새로 생성
                                binding.recommendationList.adapter = RecipeRecommendationListAdapter(recommendList, fragmentManager = parentFragmentManager, recipeViewModel)
                            }
                    }
                } else {
                    Log.e(TAG, "추천 레시피 로드 실패: ${response.code()}, ${response.message()}")
                }
            } catch (e: Exception) {
                Log.e(TAG, "추천 레시피 로드 실패", e)
            }
        }
    }
}