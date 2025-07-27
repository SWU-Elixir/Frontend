package com.example.elixir.recipe.ui.fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
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
import com.example.elixir.recipe.ui.adapter.RecipeListAdapter
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

    // Repository 및 ViewModel
    private lateinit var recipeRepository: RecipeRepository
    private lateinit var recipeViewModel: RecipeViewModel
    private lateinit var ingredientViewModel: IngredientViewModel
    private lateinit var ingredientRepository: IngredientRepository

    // 현재 선택된 필터 조건
    private var selectedCategoryType: String? = null
    private var selectedSlowAging: String? = null
    private var currentSearchKeyword: String = "" // 현재 검색 키워드

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRecipeSearchListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 레포지토리 초기화
        val recipeRepo = RecipeRepository(RetrofitClient.instanceRecipeApi, AppDatabase.getInstance(requireContext()).recipeDao())
        val ingredientRepo = IngredientRepository(RetrofitClient.instanceIngredientApi, IngredientDB.getInstance(requireContext()).ingredientDao())

        // 뷰모델 초기화
        recipeViewModel = ViewModelProvider(this, RecipeViewModelFactory(recipeRepo))[RecipeViewModel::class.java]
        ingredientViewModel = ViewModelProvider(this, IngredientViewModelFactory(ingredientRepo))[IngredientViewModel::class.java]

        // 레시피 리스트 초기화
        binding.recipeList.layoutManager = LinearLayoutManager(requireContext())

        // ViewModel에서 데이터 관찰
        setupObservers()

        // 재료 데이터 로드 (필수)
        ingredientViewModel.loadIngredients()
        refreshRecipes()

        recipeViewModel.searchResults.observe(viewLifecycleOwner) { pagingData ->
            if (!::searchListAdapter.isInitialized) {
                setupRecipeListAdapter(ingredientDataMap ?: emptyMap())
            }
            searchListAdapter.submitData(lifecycle, pagingData)

            // UI가 갱신되고 나서 itemCount를 로그로 확인
            binding.recipeList.post {
                val count = searchListAdapter.itemCount
                Log.d("PagingDebug", "submitData 이후 adapter에 들어있는 아이템 개수: $count")
            }
        }

    }

    // 레시피 필터 리프래시
    private fun refreshRecipes() {
        recipeViewModel.setSearchCategoryType(selectedCategoryType)
        recipeViewModel.setSearchCategorySlowAging(selectedSlowAging)
    }

    // ViewModel 데이터 관찰 설정
    private fun setupObservers() {
        // 레시피 목록
        recipeViewModel.searchResults.observe(viewLifecycleOwner) { pagingData ->
            if (!::searchListAdapter.isInitialized) {
                setupRecipeListAdapter(ingredientDataMap ?: emptyMap())
            }
            searchListAdapter.submitData(lifecycle, pagingData)
        }

        // 식재료 목록
        ingredientViewModel.ingredients.observe(viewLifecycleOwner) { ingredients ->
            ingredientDataMap = ingredients.associateBy { it.id }
            if (::searchListAdapter.isInitialized) {
                //searchListAdapter.updateIngredientMap(ingredientDataMap!!)
                val updateMethod = searchListAdapter.javaClass.getMethod("updateIngredientMap", Map::class.java)
                updateMethod.invoke(searchListAdapter, ingredientDataMap)
                searchListAdapter.notifyItemRangeChanged(0, searchListAdapter.itemCount)
            }
        }

        // 삭제 후 다시 로드
        recipeViewModel.deleteResult.observe(viewLifecycleOwner) {
            refreshRecipes()
        }
    }

    private fun setupRecipeListAdapter(ingredientMap: Map<Int, IngredientData>) {
        val typeItems = resources.getStringArray(R.array.type_list).toList()
        val methodItems = resources.getStringArray(R.array.method_list).toList()

        searchListAdapter = SearchListAdapter(
            ingredientMap,
            onBookmarkClick = { recipe ->
                recipe.scrappedByCurrentUser = !recipe.scrappedByCurrentUser
                if (recipe.scrappedByCurrentUser) recipeViewModel.addScrap(recipe.id)
                else recipeViewModel.deleteScrap(recipe.id)
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
                refreshRecipes()
            },
            onMethodSelected = { method ->
                selectedSlowAging = method
                recipeViewModel.setSearchCategoryType(selectedCategoryType)
                recipeViewModel.setSearchCategorySlowAging(selectedSlowAging)
                refreshRecipes()
            },
            onResetClicked = {
                selectedCategoryType = null
                selectedSlowAging = null
                refreshRecipes()
            }
        ).apply {
            this.typeItems = typeItems
            this.methodItems = methodItems
        }

        binding.recipeList.adapter = searchListAdapter
    }

    /**
     * 레시피 어댑터 초기화 또는 업데이트 시도
     * 레시피와 식재료 데이터가 모두 준비되었을 때만 어댑터를 설정하거나 업데이트합니다.
     */
    /*
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
    }*/

    private fun onSearchKeywordChanged(keyword: String) {
        // 이미 같은 값이면 변화없음
        if (currentSearchKeyword == keyword) return

        currentSearchKeyword = keyword
        recipeViewModel.setSearchKeyword(keyword)
        searchListAdapter.currentKeyword = keyword

        // Handler로 post 해서 안전하게 notify
        binding.recipeList.post {
            searchListAdapter.notifyItemChanged(0)
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}