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
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
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
import com.example.elixir.recipe.data.GetRecipeData
import com.example.elixir.recipe.viewmodel.RecipeViewModel
import com.example.elixir.recipe.data.RecipeData
import com.example.elixir.recipe.data.RecipeRepository
import com.example.elixir.recipe.viewmodel.RecipeViewModelFactory
import com.google.gson.Gson
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
    private var recommendRecipeList: List<GetRecipeData> = emptyList()
    private lateinit var recipeListAdapter: RecipeListAdapter

    private lateinit var recipeRepository: RecipeRepository
    private lateinit var ingredientViewModel: IngredientViewModel

    private var selectedCategoryType: String? = null
    private var selectedSlowAging: String? = null

    // 뷰모델
    private val recipeViewModel: RecipeViewModel by viewModels {
        RecipeViewModelFactory(recipeRepository)
    }

    private lateinit var recipeRegisterLauncher: ActivityResultLauncher<Intent>

    // 데이터 상태 관리
    private var latestRecipeList: List<RecipeData>? = null
    private var ingredientDataMap: Map<Int, IngredientData>? = null
    private var isDataInitialized = false

    // 페이징 함수
    private var isLoading = false
    private var currentPage = 0
    private val pageSize = 10

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRecipeBinding.inflate(inflater, container, false)
        parentFragmentManager.setFragmentResultListener("refresh_recipes", this) { _, _ ->
            Log.d("RecipeFragment", "recipeViewModel.getRecipes 갱신")
            currentPage = 0
            recipeViewModel.getRecipes(currentPage, pageSize, selectedCategoryType, selectedSlowAging)
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initializeData()
        setupUI()
        // 어댑터는 setupUI에서 한 번만 초기화
    }

    private fun initializeData() {
        if (isDataInitialized) {
            setupObservers()
            if (latestRecipeList != null) {
                tryInitAdapter()
            }
            return
        }

        // db 및 repository 초기화
        val recipeDao = AppDatabase.getInstance(requireContext()).recipeDao()
        val recipeApi = RetrofitClient.instanceRecipeApi
        recipeRepository = RecipeRepository(recipeApi, recipeDao)

        val ingredientRepository = IngredientRepository(
            RetrofitClient.instanceIngredientApi,
            IngredientDB.getInstance(requireContext()).ingredientDao()
        )
        val ingredientService = IngredientService(ingredientRepository)
        ingredientViewModel = ViewModelProvider(this,
            IngredientViewModelFactory(ingredientService))[IngredientViewModel::class.java]

        setupObservers()
        Log.d("RecipeFragment", "$selectedSlowAging/$selectedCategoryType")
        recipeViewModel.getRecipes(currentPage, pageSize, selectedCategoryType, selectedSlowAging)
        isDataInitialized = true
    }

    private fun setupUI() {
        // 레이아웃 매니저 설정
        binding.recipeList.layoutManager = LinearLayoutManager(requireContext())

        // 어댑터 초기화 (여기서 한 번만!)
        recipeListAdapter = RecipeListAdapter(
            emptyList(),
            emptyMap(),
            onBookmarkClick = { recipe ->
                if (recipe.scrappedByCurrentUser) {
                    recipeViewModel.deleteScrap(recipe.id)
                } else {
                    recipeViewModel.addScrap(recipe.id)
                }
            },
            onHeartClick = { recipe ->
                if (recipe.likedByCurrentUser) {
                    recipeViewModel.deleteLike(recipe.id)
                } else {
                    recipeViewModel.addLike(recipe.id)
                }
            },
            fragmentManager = parentFragmentManager
        )
        binding.recipeList.adapter = recipeListAdapter

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
        paging()
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
                    recipeListAdapter.updateData(recipeList)
                    recipeViewModel.getRecipes(currentPage, pageSize, selectedCategoryType, selectedSlowAging)
                }
            }
        }
    }

    // 두 데이터가 모두 준비됐을 때만 어댑터 업데이트
    private fun tryInitAdapter() {
        val recipes = latestRecipeList
        val ingredientMap = ingredientDataMap

        Log.d("RecipeFragment", "tryInitAdapter called - recipes: ${recipes?.size}, ingredients: ${ingredientMap?.size}")

        // 레시피 데이터만 있어도 일단 어댑터 업데이트 (식재료는 나중에 업데이트)
        if (recipes != null) {
            val safeIngredientMap = ingredientMap ?: emptyMap()
            if (recipes.isNotEmpty()) {
                // 어댑터는 이미 초기화되어 있으므로, 데이터만 업데이트
                recipeListAdapter.updateData(recipes)
                // 식재료 데이터도 업데이트 (메서드가 있다면)
                try {
                    val updateMethod = recipeListAdapter.javaClass.getMethod("updateIngredientMap", Map::class.java)
                    updateMethod.invoke(recipeListAdapter, safeIngredientMap)
                } catch (e: Exception) {
                    Log.w("Recipe2Fragment", "updateIngredientMap method not found, calling notifyDataSetChanged()")
                    recipeListAdapter.notifyDataSetChanged()
                }
                binding.recipeList.visibility = View.VISIBLE
                binding.emptyRecipeText.visibility = View.GONE
            } else {
                binding.recipeList.visibility = View.GONE
                binding.emptyRecipeText.visibility = View.VISIBLE
            }
        } else {
            Log.d("RecipeFragment", "Recipes data is null - waiting for data")
        }
    }

    private fun setupObservers() {
        // observe: 레시피 데이터
        recipeViewModel.recipeList.observe(viewLifecycleOwner) { recipes ->
            Log.d("RecipeFragment", "observe: recipes.size = ${recipes?.size ?: 0}")
            // 빈 리스트는 무시하고, 실제 데이터만 반영
            if (recipes != null && recipes.isNotEmpty()) {
                latestRecipeList = recipes
                tryInitAdapter()
                recipeListAdapter.updateData(recipes)
                isLoading = false
            }
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
            } else {
                Log.d("RecipeFragment", "Ingredient list is null or empty")
            }
        }
    }

    // 페이징: 스크롤 시 다음 10개 불러오도록
    private fun paging() {
        binding.recipeList.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val layoutManager = binding.recipeList.layoutManager as LinearLayoutManager
                val visibleItemCount = layoutManager.childCount
                val totalItemCount = layoutManager.itemCount
                val firstVisibleItem = layoutManager.findFirstVisibleItemPosition()

                if (!isLoading && (visibleItemCount + firstVisibleItem) >= totalItemCount && firstVisibleItem >= 0) {
                    loadMore()
                }
            }
        })
    }

    private fun loadMore() {
        isLoading = true
        currentPage++
        recipeViewModel.getRecipes(currentPage, pageSize, selectedCategoryType, selectedSlowAging)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    // FAB 클릭 이벤트
    private fun setupFabClickListener() {
        binding.fab.setOnClickListener {
            val intent = Intent(requireContext(), ToolbarActivity::class.java).apply {
                putExtra("mode", 9) // 레시피 등록 모드
            }
            recipeRegisterLauncher.launch(intent)
            currentPage = 0
            recipeViewModel.getRecipes(currentPage, 0, selectedCategoryType, selectedSlowAging)
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
        setupMethodSpinner()
        setupTypeSpinner()
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
                        selectedSlowAging = null
                        return
                    }
                    selectedSlowAging = parent.getItemAtPosition(position).toString()
                    currentPage = 0
                    recipeViewModel.getRecipes(currentPage, pageSize, selectedCategoryType, selectedSlowAging)
                    filterRecipes()
                }
                override fun onNothingSelected(parent: AdapterView<*>) {}
            }
    }

    // 종류 스피너
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
                if (position == 0) {
                    selectedCategoryType = null
                } else {
                    selectedCategoryType = parent.getItemAtPosition(position).toString()

                    // 텍스트 변환
                    if(selectedCategoryType == "음료/차")
                        selectedCategoryType = "음료_차"
                    else if(selectedCategoryType == "양념/소스/잼")
                        selectedCategoryType = "양념_소스_잼"
                }
                currentPage = 0
                recipeViewModel.getRecipes(currentPage, pageSize, selectedCategoryType, selectedSlowAging)
                filterRecipes()
            }
            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }

    private fun setupResetButton() {
        binding.resetButton.setOnClickListener {
            resetSpinners()
            updateResetButtonVisibility()
        }
    }

    private fun setupRecommendationViewPager() {
        val recommendationAdapter = RecipeRecommendationListAdapter(recommendRecipeList, fragmentManager = parentFragmentManager, recipeViewModel)
        binding.recommendationList.adapter = recommendationAdapter
        binding.recommendationList.setPageTransformer { page, position ->
            val absPos = kotlin.math.abs(position)
            page.scaleY = 0.85f + (1 - absPos) * 0.15f
            page.scaleX = 0.85f + (1 - absPos) * 0.15f
            page.translationX = -position * 40
        }
        binding.indicator.attachTo(binding.recommendationList)
    }

    private fun setupSearchButton() {
        binding.searchButton.setOnClickListener {
            val transaction = parentFragmentManager.beginTransaction()
            transaction.replace(R.id.fullscreenContainer, SearchFragment())
            transaction.addToBackStack(null)
            transaction.commit()
        }
    }

    private fun filterRecipes() {
        val selectedMethod = binding.spinnerDifficulty.selectedItem?.toString()
        val selectedType = binding.spinnerType.selectedItem?.toString()
        val filtered = recipeList.filter { recipe ->
            val methodMatch = selectedMethod == "저속노화" || recipe.categorySlowAging == selectedMethod
            val typeMatch = selectedType == "종류" || recipe.categoryType == selectedType
            methodMatch && typeMatch
        }
        recipeListAdapter.updateData(filtered)
        updateFilteredUI(filtered)
    }

    private fun updateFilteredUI(filtered: List<RecipeData>) {
        if (filtered.isEmpty()) {
            binding.recipeList.visibility = View.GONE
            binding.emptyRecipeText.visibility = View.VISIBLE
        } else {
            binding.recipeList.visibility = View.VISIBLE
            binding.emptyRecipeText.visibility = View.GONE
        }
    }

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
                        (binding.recommendationList.adapter as? RecipeRecommendationListAdapter)?.updateData(recommendList)
                            ?: run {
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
