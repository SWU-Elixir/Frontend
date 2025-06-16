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
import android.widget.Toast
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

    private lateinit var recipeRepository: RecipeRepository

    // ViewModel 초기화
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
            //tryInitOrUpdateAdapter()
        }

        // 레시피 검색 결과 데이터 관찰
        recipeViewModel.recipeList.observe(viewLifecycleOwner) { recipes ->
            latestRecipeSearchResults = recipes
            Log.d("SearchListFragment", "Recipe search results size: ${recipes.size}")
            //tryInitOrUpdateAdapter()
        }
    }

    // 레시피 어댑터 초기화 또는 업데이트 시도
    // 레시피 어댑터 초기화 또는 업데이트 시도
//    private fun tryInitOrUpdateAdapter() {
//        val recipes = latestRecipeSearchResults
//        val ingredientsMap = ingredientDataMap
//
//        // 두 데이터가 모두 준비되었을 때만 처리
//        if (recipes != null && ingredientsMap != null) {
//            if (!::recipeListAdapter.isInitialized) {
//                // 어댑터가 초기화되지 않았다면 새로 생성
//                recipeListAdapter = RecipeListAdapter(
//                    recipes, // 초기 데이터
//                    ingredientsMap, // Map 전달
//                    onBookmarkClick = { recipeId -> // <-- 여기: Int 타입으로 변경
//                        // ViewModel에 스크랩/스크랩 취소 요청
//                        val currentRecipe = latestRecipeSearchResults?.find { it.id == recipeId }
//                        if (currentRecipe != null) {
//                            if (currentRecipe.scrappedByCurrentUser) {
//                                recipeViewModel.deleteScrap(recipeId)
//                            } else {
//                                recipeViewModel.addScrap(recipeId)
//                            }
//                        }
//                    },
//                    onHeartClick = { recipeId -> // <-- 여기: Int 타입으로 변경
//                        // ViewModel에 좋아요/좋아요 취소 요청
//                        val currentRecipe = latestRecipeSearchResults?.find { it.id == recipeId }
//                        if (currentRecipe != null) {
//                            if (currentRecipe.likedByCurrentUser) {
//                                recipeViewModel.deleteLike(recipeId)
//                            } else {
//                                recipeViewModel.addLike(recipeId)
//                            }
//                        }
//                    },
//                    fragmentManager = parentFragmentManager
//                )
//                binding.recipeList.adapter = recipeListAdapter
//            } else {
//                // 이미 초기화되었다면 데이터만 업데이트
//                recipeListAdapter.updateData(recipes)
//            }
//
//            // 검색 결과에 따른 UI 표시 (레시피 리스트 / 빈 텍스트)
//            if (recipes.isEmpty()) {
//                binding.recipeList.visibility = View.GONE
//                binding.emptyRecipeText.visibility = View.VISIBLE
//            } else {
//                binding.recipeList.visibility = View.VISIBLE
//                binding.emptyRecipeText.visibility = View.GONE
//            }
//        }
//    }

    /**
     * 검색어 초기화 및 설정
     */
    private fun initializeSearchKeyword() {
        val keyword = arguments?.getString("search_keyword")?.trim() ?: ""
        binding.searchEditText.setText(keyword)
        currentSearchKeyword = keyword // 현재 검색 키워드 업데이트
        // 초기 검색 실행 (뷰모델을 통해)
        // 스피너의 초기 선택이 "저속노화"와 "종류"이므로, null 대신 기본값을 전달
        recipeViewModel.searchRecipes(currentSearchKeyword, 0, 10, "저속노화", "종류")
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
                // 사용자가 텍스트를 입력할 때마다 검색 필터링을 다시 수행
                // 이전에 'navigateToSearch'로 이동하는 로직이 있었는데,
                // 여기서는 바로 필터링을 수행하는 것으로 변경
                currentSearchKeyword = s.toString().trim()
                performSearchWithFilters()
            }
        })

        // 검색 버튼 클릭 (현재는 EditText의 TextWatcher와 동일하게 작동)
        binding.searchButton.setOnClickListener {
            // EditText의 TextWatcher가 이미 필터링을 수행하므로, 추가적인 작업이 필요 없을 수 있습니다.
            // 명시적으로 검색 버튼 클릭 시 검색을 재실행하고 싶다면 여기에 로직 추가
            performSearchWithFilters()
        }

        // 키보드 검색 버튼
        binding.searchEditText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                performSearchWithFilters()
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
        val method = if (binding.spinnerDifficulty.selectedItemPosition == 0) null else selectedSlowAging
        val type = if (binding.spinnerType.selectedItemPosition == 0) null else selectedCategoryType

        // ViewModel의 searchRecipes 호출
        recipeViewModel.searchRecipes(keyword, 0, 10, type, method)
    }


    /**
     * 스피너 초기화 (선택을 "저속노화"와 "종류"로 되돌림)
     */
    private fun resetSpinners() {
        binding.spinnerDifficulty.setSelection(0)
        binding.spinnerType.setSelection(0)
        // 스피너 리셋 시 선택된 값도 초기화 (performSearchWithFilters에서 null로 전달될 수 있도록)
        selectedSlowAging = null
        selectedCategoryType = null
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}