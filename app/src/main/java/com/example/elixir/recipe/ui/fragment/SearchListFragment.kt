package com.example.elixir.recipe.ui.fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.paging.PagingData
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.elixir.R
import com.example.elixir.RetrofitClient
import com.example.elixir.databinding.FragmentRecipeSearchListBinding
import com.example.elixir.ingredient.data.IngredientData
import com.example.elixir.ingredient.network.IngredientDB
import com.example.elixir.ingredient.network.IngredientRepository
import com.example.elixir.ingredient.viewmodel.IngredientViewModel
import com.example.elixir.ingredient.viewmodel.IngredientViewModelFactory
import com.example.elixir.network.AppDatabase
import com.example.elixir.recipe.data.RecipeData
import com.example.elixir.recipe.data.RecipeListItemData
import com.example.elixir.recipe.data.SearchItemData
import com.example.elixir.recipe.repository.RecipeRepository
import com.example.elixir.recipe.ui.adapter.SearchListAdapter
import com.example.elixir.recipe.viewmodel.RecipeViewModel
import com.example.elixir.recipe.viewmodel.RecipeViewModelFactory
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/**
 * 레시피 검색 결과를 표시하는 프래그먼트
 * 검색어와 필터 조건에 따라 레시피를 필터링하고 표시
 */
class SearchListFragment : Fragment() {

    // ViewBinding
    private var _binding: FragmentRecipeSearchListBinding? = null
    private val binding get() = _binding!!

    // 어댑터 및 데이터
    private lateinit var searchListAdapter: SearchListAdapter

    // Ingredient 데이터를 Map으로 저장하여 효율적으로 사용
    private var ingredientDataMap: Map<Int, IngredientData>? = null

    // 검색 결과를 저장할 변수
    private var latestPagingData: PagingData<SearchItemData>? = null

    // Repository 및 ViewModel
    private lateinit var recipeRepository: RecipeRepository
    private val recipeViewModel: RecipeViewModel by viewModels {
        RecipeViewModelFactory(recipeRepository)
    }

    private lateinit var ingredientRepository: IngredientRepository
    private val ingredientViewModel: IngredientViewModel by viewModels {
        IngredientViewModelFactory(ingredientRepository)
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
/*
        // 1. 검색어 초기화 및 설정
        initializeSearchKeyword()

        // 2. 검색 관련 이벤트 설정
        setupSearchEvents()

        // 3. 스피너 설정
        setupSpinners()

        // 4. 뒤로가기 버튼 설정
        setupBackButton()*/

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
        recipeViewModel.searchResults.observe(viewLifecycleOwner) { pagingData ->
            Log.d("SearchListFragment", "PagingData received from ViewModel")
            if (::searchListAdapter.isInitialized) {
                searchListAdapter.submitData(viewLifecycleOwner.lifecycle, pagingData)
            } else {
                latestPagingData = pagingData // 아래에서 넘기기 위해 잠깐 저장
                tryInitOrUpdateAdapter()
            }
        }
    }

    /**
     * 레시피 어댑터 초기화 또는 업데이트 시도
     * 레시피와 식재료 데이터가 모두 준비되었을 때만 어댑터를 설정하거나 업데이트합니다.
     */
    private fun tryInitOrUpdateAdapter() {
        val ingredientMap = ingredientDataMap

        if (ingredientMap != null) {
            // 어댑터가 초기화되지 않았다면 새로 생성
            if (!::searchListAdapter.isInitialized) {
                setUpRecipeListAdapter(ingredientMap)
            } else {
                // 어댑터가 이미 있다면 ingredientMap만 업데이트
                searchListAdapter.updateIngredientMap(ingredientMap)
            }
        } else {
            Log.d("SearchListFragment", "Ingredients data is null - waiting for data")
            /*// 식재료 데이터가 있다면 어댑터에 업데이트 (RecipeListAdapter에 updateIngredientMap 메서드가 있다고 가정)
            try {
                val updateMethod = recipeListAdapter.javaClass.getMethod("updateIngredientMap", Map::class.java)
                updateMethod.invoke(recipeListAdapter, ingredientMap)
                Log.d("SearchListFragment", "Successfully updated ingredient map in adapter")
            } catch (e: Exception) {
                Log.w("SearchListFragment", "updateIngredientMap method not found or failed, calling notifyDataSetChanged()")

                recipeListAdapter = RecipeListAdapter(
                    recipes.toMutableList(),
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
            binding.etSearch.visibility = View.VISIBLE
            Log.d("SearchListFragment", "No recipes - showing empty text")
        } else {
            binding.recipeList.visibility = View.VISIBLE
            binding.etSearch.visibility = View.GONE
            Log.d("SearchListFragment", "RecipeList is now VISIBLE")
        }
    } else {
        Log.d("SearchListFragment", "Recipes or Ingredients data is null - waiting for data")
        // 데이터 로딩 중이므로 리스트를 숨기고 로딩 상태 표시 등을 고려할 수 있음
        binding.recipeList.visibility = View.GONE
        binding.etSearch.visibility = View.GONE // 혹은 로딩 인디케이터*/
        }
    }


    // 레시피 리스트 어댑터 설정
    private fun setUpRecipeListAdapter(safeIngredientMap: Map<Int, IngredientData>) {
        val typeItems = resources.getStringArray(R.array.type_list).toList()
        val methodItems = resources.getStringArray(R.array.method_list).toList()

        searchListAdapter = SearchListAdapter(
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
            fragmentManager = parentFragmentManager,
            recipeViewModel = recipeViewModel,
            onSearchKeywordChanged = { keyword -> onSearchKeywordChanged(keyword) },
            onTypeSelected = { type ->
                selectedCategoryType = type
                recipeViewModel.setSearchCategoryType(selectedCategoryType)
                recipeViewModel.setSearchCategorySlowAging(selectedSlowAging)
                searchListAdapter.notifyItemChanged(1)                  // 검색 스피너 헤더만 업데이트(모드 번호: 1)
            },
            onMethodSelected = { method ->
                selectedSlowAging = method
                recipeViewModel.setSearchCategoryType(selectedCategoryType)
                recipeViewModel.setSearchCategorySlowAging(selectedSlowAging)
                searchListAdapter.notifyItemChanged(1)                  // 검색 스피너 헤더만 업데이트(모드 번호: 1)
            },
            onResetClicked = {
                selectedCategoryType = null
                selectedSlowAging = null
                recipeViewModel.setSearchCategoryType(null)
                recipeViewModel.setSearchCategorySlowAging(null)
                searchListAdapter.notifyItemChanged(1)                  // 검색 스피너 헤더만 업데이트(모드 번호: 1)
            }
        )
        searchListAdapter.typeItems = typeItems
        searchListAdapter.methodItems = methodItems
        binding.recipeList.adapter = searchListAdapter

        observePagingData()
    }

    private fun observePagingData() {
        recipeViewModel.searchResults.observe(viewLifecycleOwner) { pagingData ->
            searchListAdapter.submitData(lifecycle, pagingData)
        }
    }

    private fun onSearchKeywordChanged(keyword: String) {
        currentSearchKeyword = keyword
        recipeViewModel.setSearchKeyword(keyword)
        searchListAdapter.currentKeyword = keyword
        searchListAdapter.notifyItemChanged(0) // SearchTextHeader 위치
    }

/*
    /**
     * 검색어 초기화 및 설정
     */
    private fun initializeSearchKeyword() {
        val keyword = arguments?.getString("search_keyword")?.trim() ?: ""
        binding.etSearch.setText(keyword)
        currentSearchKeyword = keyword // 현재 검색 키워드 업데이트

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
        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {}
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                currentSearchKeyword = s.toString().trim()
                performSearchWithFilters()
            }
        })

        // 검색 버튼 클릭
        binding.searchButton.setOnClickListener {
            performSearchWithFilters()
        }

        // 키보드 검색 버튼
        binding.etSearch.setOnEditorActionListener { _, actionId, _ ->
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
        binding.btnBack.setOnClickListener {
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
        val keyword = binding.etSearch.text.toString().trim()
        // 스피너의 0번째 인덱스는 "저속노화" 또는 "종류"이므로, 이들을 선택했을 때는 필터링하지 않음을 의미합니다.
        // ViewModel의 searchRecipes 메서드가 null을 받으면 모든 카테고리를 검색하도록 처리되어야 합니다.
        val method = if (binding.spinnerDifficulty.selectedItemPosition == 0) null else selectedSlowAging
        val type = if (binding.spinnerType.selectedItemPosition == 0) null else selectedCategoryType

        Log.d("SearchListFragment", "Performing search with keyword: '$keyword', method: '$method', type: '$type'")
        // ViewModel의 searchRecipes 호출
        lifecycleScope.launch {
            recipeViewModel.searchResults.collectLatest { pagingData ->
                recipeListAdapter.submitData(pagingData)
            }
        }
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
    }*/
}