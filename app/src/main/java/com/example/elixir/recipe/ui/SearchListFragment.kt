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
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.elixir.R
import com.example.elixir.RetrofitClient
import com.example.elixir.databinding.FragmentRecipeSearchListBinding
import com.example.elixir.ingredient.data.IngredientData
import com.example.elixir.ingredient.network.IngredientDB
import com.example.elixir.ingredient.network.IngredientRepository
import com.example.elixir.ingredient.viewmodel.IngredientService
import com.example.elixir.ingredient.viewmodel.IngredientViewModel
import com.example.elixir.ingredient.viewmodel.IngredientViewModelFactory
import com.example.elixir.network.AppDatabase
import com.example.elixir.recipe.data.RecipeData
import com.example.elixir.recipe.data.RecipeRepository
import com.example.elixir.recipe.viewmodel.RecipeViewModel
import com.example.elixir.recipe.viewmodel.RecipeViewModelFactory

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

    // Ingredient 데이터를 Map으로 저장하여 효율적으로 사용
    private var ingredientDataMap: Map<Int, IngredientData>? = null

    // 검색 결과를 저장할 변수
    private var latestRecipeSearchResults: List<RecipeData>? = null

    // Repository 및 ViewModel
    private lateinit var recipeRepository: RecipeRepository
    private val recipeViewModel: RecipeViewModel by viewModels {
        RecipeViewModelFactory(recipeRepository)
    }

    private lateinit var ingredientRepository: IngredientRepository
    private lateinit var ingredientService: IngredientService
    private val ingredientViewModel: IngredientViewModel by viewModels {
        IngredientViewModelFactory(ingredientService)
    }

    // 현재 선택된 필터 조건
    private var selectedCategoryType: String? = null
    private var selectedSlowAging: String? = null
    private var currentSearchKeyword: String = "" // 현재 검색 키워드

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRecipeSearchListBinding.inflate(inflater, container, false)

        // Repository 초기화 (onCreateView에서 하는 것이 좋음)
        ingredientRepository = IngredientRepository(
            RetrofitClient.instanceIngredientApi,
            IngredientDB.getInstance(requireContext()).ingredientDao()
        )
        ingredientService = IngredientService(ingredientRepository)

        recipeRepository = RecipeRepository(
            RetrofitClient.instanceRecipeApi,
            AppDatabase.getInstance(requireContext()).recipeDao()
        )
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // RecyclerView 설정
        binding.recipeList.layoutManager = LinearLayoutManager(requireContext())

        // ViewModel에서 데이터 관찰
        setupObservers()

        // 1. 검색어 초기화 및 설정
        initializeSearchKeyword()

        // 2. 검색 관련 이벤트 설정
        setupSearchEvents()

        // 3. 스피너 설정
        setupSpinners()

        // 4. 뒤로가기 버튼 설정
        setupBackButton()

        // 재료 데이터 로드 (필수)
        ingredientViewModel.loadIngredients()
    }

    // ViewModel 데이터 관찰 설정
    private fun setupObservers() {
        // 식재료 데이터 관찰
        ingredientViewModel.ingredients.observe(viewLifecycleOwner) { ingredientList ->
            // List를 Map으로 변환하여 저장
            ingredientDataMap = ingredientList.associateBy { it.id }
            Log.d("SearchListFragment", "Ingredient Map size: ${ingredientDataMap?.size}")
            tryInitOrUpdateAdapter()
        }

        // 레시피 검색 결과 데이터 관찰
        recipeViewModel.recipeList.observe(viewLifecycleOwner) { recipes ->
            latestRecipeSearchResults = recipes
            Log.d("SearchListFragment", "Recipe search results size: ${recipes.size}")
            tryInitOrUpdateAdapter()
        }
    }

    /**
     * 레시피 어댑터 초기화 또는 업데이트 시도
     * 레시피와 식재료 데이터가 모두 준비되었을 때만 어댑터를 설정하거나 업데이트합니다.
     */
    private fun tryInitOrUpdateAdapter() {
        val recipes = latestRecipeSearchResults
        val ingredientMap = ingredientDataMap

        Log.d("SearchListFragment", "tryInitOrUpdateAdapter called - recipes: ${recipes?.size}, ingredients: ${ingredientMap?.size}")

        // 레시피 데이터와 식재료 맵이 모두 준비되었을 때만 처리
        if (recipes != null && ingredientMap != null) {
            // 어댑터가 초기화되지 않았다면 새로 생성
            if (!::recipeListAdapter.isInitialized) {
                Log.d("SearchListFragment", "Creating new adapter with ${recipes.size} recipes")
                recipeListAdapter = RecipeListAdapter(
                    recipes,
                    ingredientMap,
                    onBookmarkClick = { recipe -> // RecipeData 객체 전체를 전달
                        if (recipe.scrappedByCurrentUser) {
                            recipeViewModel.deleteScrap(recipe.id)
                        } else {
                            recipeViewModel.addScrap(recipe.id)
                        }
                    },
                    onHeartClick = { recipe -> // RecipeData 객체 전체를 전달
                        if (recipe.likedByCurrentUser) {
                            recipeViewModel.deleteLike(recipe.id)
                        } else {
                            recipeViewModel.addLike(recipe.id)
                        }
                    },
                    fragmentManager = parentFragmentManager
                )
                binding.recipeList.adapter = recipeListAdapter
            } else {
                // 이미 초기화되었다면 데이터만 업데이트
                Log.d("SearchListFragment", "Updating existing adapter with ${recipes.size} recipes and ${ingredientMap.size} ingredients")
                recipeListAdapter.updateData(recipes)

                // 식재료 데이터가 있다면 어댑터에 업데이트 (RecipeListAdapter에 updateIngredientMap 메서드가 있다고 가정)
                try {
                    val updateMethod = recipeListAdapter.javaClass.getMethod("updateIngredientMap", Map::class.java)
                    updateMethod.invoke(recipeListAdapter, ingredientMap)
                    Log.d("SearchListFragment", "Successfully updated ingredient map in adapter")
                } catch (e: Exception) {
                    Log.w("SearchListFragment", "updateIngredientMap method not found or failed, calling notifyDataSetChanged()")
                    // 리플렉션이 실패하면 전체 데이터를 다시 설정 (강제로 어댑터 재생성)
                    // 이 부분은 RecipeFragment와 유사하게, 어댑터가 이미 초기화되어 있다면 데이터만 업데이트하고,
                    // 식재료 맵이 변경되었을 경우를 고려하여 `updateIngredientMap` 메서드를 호출하는 것이 좋습니다.
                    // 만약 `RecipeListAdapter`에 `updateIngredientMap` 메서드가 없다면,
                    // 데이터 업데이트 후 `notifyDataSetChanged()`를 호출하거나,
                    // 새로운 어댑터를 생성하여 할당하는 방식으로 구현해야 합니다.
                    recipeListAdapter = RecipeListAdapter(
                        recipes,
                        ingredientMap,
                        onBookmarkClick = { recipe -> // RecipeData 객체 전체를 전달
                            if (recipe.scrappedByCurrentUser) {
                                recipeViewModel.deleteScrap(recipe.id)
                            } else {
                                recipeViewModel.addScrap(recipe.id)
                            }
                        },
                        onHeartClick = { recipe -> // RecipeData 객체 전체를 전달
                            if (recipe.likedByCurrentUser) {
                                recipeViewModel.deleteLike(recipe.id)
                            } else {
                                recipeViewModel.addLike(recipe.id)
                            }
                        },
                        fragmentManager = parentFragmentManager
                    )
                    binding.recipeList.adapter = recipeListAdapter
                }
            }

            // 검색 결과에 따른 UI 표시 (레시피 리스트 / 빈 텍스트)
            if (recipes.isEmpty()) {
                binding.recipeList.visibility = View.GONE
                binding.emptyRecipeText.visibility = View.VISIBLE
                Log.d("SearchListFragment", "No recipes - showing empty text")
            } else {
                binding.recipeList.visibility = View.VISIBLE
                binding.emptyRecipeText.visibility = View.GONE
                Log.d("SearchListFragment", "RecipeList is now VISIBLE")
            }
        } else {
            Log.d("SearchListFragment", "Recipes or Ingredients data is null - waiting for data")
            // 데이터 로딩 중이므로 리스트를 숨기고 로딩 상태 표시 등을 고려할 수 있음
            binding.recipeList.visibility = View.GONE
            binding.emptyRecipeText.visibility = View.GONE // 혹은 로딩 인디케이터
        }
    }


    /**
     * 검색어 초기화 및 설정
     */
    private fun initializeSearchKeyword() {
        val keyword = arguments?.getString("search_keyword")?.trim() ?: ""
        binding.searchEditText.setText(keyword)
        currentSearchKeyword = keyword // 현재 검색 키워드 업데이트

        // 초기 검색 실행
        // 스피너의 초기 선택이 "저속노화"와 "종류"이므로, null 대신 기본값을 전달
        // ViewModel에서 API를 호출할 때 "저속노화"와 "종류"는 필터링하지 않음을 의미해야 합니다.
        // 예를 들어, API 파라미터가 null 또는 특정 기본값이면 모든 카테고리를 포함하도록 처리해야 합니다.
        performSearchWithFilters()
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
                currentSearchKeyword = s.toString().trim()
                // 텍스트 변경 즉시 검색을 시작하는 대신, 사용자가 타이핑을 멈췄을 때 또는
                // 검색 버튼을 눌렀을 때만 검색하도록 설정할 수 있습니다.
                // 여기서는 변경될 때마다 호출되도록 유지합니다.
                performSearchWithFilters()
            }
        })

        // 검색 버튼 클릭
        binding.searchButton.setOnClickListener {
            performSearchWithFilters()
        }

        // 키보드 검색 버튼
        binding.searchEditText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                performSearchWithFilters()
                // 검색 후 스피너 초기화는 필요에 따라 주석 처리 또는 유지
                resetSpinners() // 검색 후 스피너 초기화
                updateResetButtonVisibility()
                true
            } else {
                false
            }
        }
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
            performSearchWithFilters() // 스피너 리셋 후 검색 다시 수행
        }
    }

    /**
     * 저속노화 방법 스피너 설정
     */
    private fun setupMethodSpinner() {
        val methodItems = resources.getStringArray(R.array.method_list).toList()
        val methodAdapter = RecipeListSpinnerAdapter(requireContext(), methodItems)
        binding.spinnerDifficulty.adapter = methodAdapter
        binding.spinnerDifficulty.setSelection(0) // 기본 "저속노화" 선택

        binding.spinnerDifficulty.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                selectedSlowAging = parent.getItemAtPosition(position).toString()
                updateResetButtonVisibility()
                performSearchWithFilters() // 스피너 선택 변경 시 검색 다시 수행
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
        binding.spinnerType.setSelection(0) // 기본 "종류" 선택

        binding.spinnerType.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                selectedCategoryType = parent.getItemAtPosition(position).toString()
                // API 호출을 위한 문자열 변환
                if (selectedCategoryType == "음료/차")
                    selectedCategoryType = "음료_차"
                else if (selectedCategoryType == "양념/소스/잼")
                    selectedCategoryType = "양념_소스_잼"

                updateResetButtonVisibility()
                performSearchWithFilters() // 스피너 선택 변경 시 검색 다시 수행
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
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
     * 현재 검색 키워드와 필터 조건으로 ViewModel에 검색 요청
     */
    private fun performSearchWithFilters() {
        val keyword = binding.searchEditText.text.toString().trim()
        // 스피너의 0번째 인덱스는 "저속노화" 또는 "종류"이므로, 이들을 선택했을 때는 필터링하지 않음을 의미합니다.
        // ViewModel의 searchRecipes 메서드가 null을 받으면 모든 카테고리를 검색하도록 처리되어야 합니다.
        val method = if (binding.spinnerDifficulty.selectedItemPosition == 0) null else selectedSlowAging
        val type = if (binding.spinnerType.selectedItemPosition == 0) null else selectedCategoryType

        Log.d("SearchListFragment", "Performing search with keyword: '$keyword', method: '$method', type: '$type'")
        // ViewModel의 searchRecipes 호출
        recipeViewModel.searchRecipes(keyword, 0, 10, type, method)
    }

    /**
     * 스피너 초기화
     */
    private fun resetSpinners() {
        binding.spinnerDifficulty.setSelection(0)
        binding.spinnerType.setSelection(0)
        selectedSlowAging = null // 스피너 초기화 시 필터 값도 초기화
        selectedCategoryType = null // 스피너 초기화 시 필터 값도 초기화
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}