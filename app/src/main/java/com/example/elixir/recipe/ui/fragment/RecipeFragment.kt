package com.example.elixir.recipe.ui.fragment

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
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
import com.example.elixir.ingredient.viewmodel.IngredientViewModel
import com.example.elixir.ingredient.viewmodel.IngredientViewModelFactory
import com.example.elixir.network.AppDatabase
import com.example.elixir.recipe.viewmodel.RecipeViewModel
import com.example.elixir.recipe.data.RecipeItemData
import com.example.elixir.recipe.repository.RecipeRepository
import com.example.elixir.recipe.ui.adapter.RecipeListAdapter
import com.example.elixir.recipe.ui.paging.StickyHeaderItemDecoration
import com.example.elixir.recipe.viewmodel.RecipeViewModelFactory
import com.google.gson.Gson
import kotlinx.coroutines.launch

/**
 * 레시피 화면을 표시하는 프래그먼트
 * 추천 레시피와 전체 레시피 목록을 보여주며, 필터링 기능을 제공
 */
class RecipeFragment : Fragment() {

    private var _binding: FragmentRecipeBinding? = null
    private val binding get() = _binding!!

    private lateinit var recipeListAdapter: RecipeListAdapter
    private lateinit var recipeViewModel: RecipeViewModel
    private lateinit var ingredientViewModel: IngredientViewModel

    private var selectedCategoryType: String? = null
    private var selectedSlowAging: String? = null

    private var ingredientDataMap: Map<Int, IngredientData>? = null
    private var recommendRecipeList: List<RecipeItemData> = emptyList()

    private lateinit var recipeRegisterLauncher: ActivityResultLauncher<Intent>

    private var currentStickyHeader: View? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRecipeBinding.inflate(inflater, container, false)

        parentFragmentManager.setFragmentResultListener("refresh_recipes", this) { _, _ ->
            refreshRecipes()
        }

        // 레시피 등록 후 현재 프래그먼트로 되돌아왔을 때 refresh
        recipeRegisterLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                result.data?.getStringExtra("recipeData")?.let { json ->
                    refreshRecipes()
                    recipeListAdapter.updateSearchHeader(
                        selectedType = selectedCategoryType,
                        selectedMethod = selectedSlowAging,
                        typeItems = resources.getStringArray(R.array.type_list).toList(),
                        methodItems = resources.getStringArray(R.array.method_list).toList()
                    )
                }
            }
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        // 레포지토리 초기화
        val recipeRepo = RecipeRepository(RetrofitClient.instanceRecipeApi, AppDatabase.getInstance(requireContext()).recipeDao())
        val ingredientRepo = IngredientRepository(RetrofitClient.instanceIngredientApi, IngredientDB.getInstance(requireContext()).ingredientDao())

        // 뷰모델 초기화
        recipeViewModel = ViewModelProvider(requireActivity(), RecipeViewModelFactory(recipeRepo)).get(RecipeViewModel::class.java)
        ingredientViewModel = ViewModelProvider(requireActivity(), IngredientViewModelFactory(ingredientRepo))[IngredientViewModel::class.java]

        binding.recipeList.layoutManager = LinearLayoutManager(requireContext())
        binding.fab.setOnClickListener {
            val intent = Intent(requireContext(), ToolbarActivity::class.java).apply {
                putExtra("mode", 9)
            }
            recipeRegisterLauncher.launch(intent)
        }

        setupObservers()
        //setupRecipeRegisterLauncher()

        ingredientViewModel.loadIngredients()
        refreshRecipes()
        loadRecommendRecipe()
    }

    // 레시피 필터 리프래시
    private fun refreshRecipes() {
        recipeViewModel.setCategoryType(selectedCategoryType)
        recipeViewModel.setCategorySlowAging(selectedSlowAging)
    }

    // 라이브데이터 관찰
    private fun setupObservers() {
        // 레시피 목록
        recipeViewModel.recipes.observe(viewLifecycleOwner) { pagingData ->
            if (!::recipeListAdapter.isInitialized) {
                setupRecipeListAdapter(ingredientDataMap ?: emptyMap())
            }
            recipeListAdapter.submitData(lifecycle, pagingData)
        }

        // 식재료 목록
        ingredientViewModel.ingredients.observe(viewLifecycleOwner) { ingredients ->
            ingredientDataMap = ingredients.associateBy { it.id }

            if (::recipeListAdapter.isInitialized) {
                try {
                    val updateMethod = recipeListAdapter.javaClass.getMethod("updateIngredientMap", Map::class.java)
                    updateMethod.invoke(recipeListAdapter, ingredientDataMap)
                    recipeListAdapter.notifyItemRangeChanged(0, recipeListAdapter.itemCount)
                } catch (e: Exception) {
                    Log.w("RecipeFragment", "updateIngredientMap() reflection failed: ${e.message}")
                }
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

        recipeListAdapter = RecipeListAdapter(
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
            onSearchClicked = {
                parentFragmentManager.beginTransaction()
                    .replace(R.id.fullscreenContainer, SearchFragment())
                    .addToBackStack(null)
                    .commit()
            },
            onTypeSelected = { type ->
                selectedCategoryType = type
                recipeViewModel.setCategoryType(selectedCategoryType)
                refreshRecipes()
            },
            onMethodSelected = { method ->
                selectedSlowAging = method
                recipeViewModel.setCategorySlowAging(selectedSlowAging)
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

        // 레시피 리스트에 어댑터 및 선형 레이아웃 매니저 부착
        binding.recipeList.apply {
            adapter = recipeListAdapter
            layoutManager = LinearLayoutManager(context)
        }

        binding.recipeList.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(rv: RecyclerView, dx: Int, dy: Int) {
                handleStickyHeaderVisibility()
            }
        })
    }

    private fun handleStickyHeaderVisibility() {
        val layoutManager = binding.recipeList.layoutManager as? LinearLayoutManager ?: return
        val firstVisiblePosition = layoutManager.findFirstVisibleItemPosition()

        if (firstVisiblePosition >= 2) {
            if (binding.stickyHeader.childCount == 0) {
                val viewType = recipeListAdapter.getItemViewType(1)
                val vh = recipeListAdapter.onCreateViewHolder(binding.recipeList, viewType)
                recipeListAdapter.onBindViewHolder(vh, 1)
                val headerView = vh.itemView

                measureAndLayoutHeader(headerView)

                binding.stickyHeader.addView(headerView)
                currentStickyHeader = headerView
            }
        } else {
            binding.stickyHeader.removeAllViews()
            currentStickyHeader = null
        }
    }

    private fun measureAndLayoutHeader(headerView: View) {
        val widthSpec = View.MeasureSpec.makeMeasureSpec(binding.recipeList.width, View.MeasureSpec.EXACTLY)
        val heightSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
        headerView.measure(widthSpec, heightSpec)
        headerView.layout(0, 0, headerView.measuredWidth, headerView.measuredHeight)
    }
/*
    // 레시피 등록 후 refresh
    private fun setupRecipeRegisterLauncher() {
        recipeRegisterLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                result.data?.getStringExtra("recipeData")?.let { json ->
                    //val newRecipe = Gson().fromJson(json, RecipeItemData::class.java)
                    recipeViewModel.setCategoryType(selectedCategoryType)
                    recipeViewModel.setCategorySlowAging(selectedSlowAging)
                    recipeListAdapter.updateSearchHeader(
                        selectedType = selectedCategoryType,
                        selectedMethod = selectedSlowAging,
                        typeItems = resources.getStringArray(R.array.type_list).toList(),
                        methodItems = resources.getStringArray(R.array.method_list).toList()
                    )
                }
            }
        }
    }*/

    // 추천 레시피 불러오기
    private fun loadRecommendRecipe() {
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.instanceRecipeApi.getRecipeByRecommend()
                if (response.isSuccessful) {
                    response.body()?.data?.let {
                        recommendRecipeList = it
                        if (::recipeListAdapter.isInitialized) {
                            recipeListAdapter.recommendRecipeList = it
                            recipeListAdapter.notifyItemChanged(0)
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("RecipeFragment", "추천 레시피 로드 실패", e)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
