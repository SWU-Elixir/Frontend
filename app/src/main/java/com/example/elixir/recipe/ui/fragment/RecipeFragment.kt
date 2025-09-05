package com.example.elixir.recipe.ui.fragment

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.paging.LoadState
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.elixir.R
import com.example.elixir.RetrofitClient
import com.example.elixir.ToolbarActivity
import com.example.elixir.databinding.FragmentRecipeBinding
import com.example.elixir.ingredient.data.IngredientEntity
import com.example.elixir.ingredient.network.IngredientRepository
import com.example.elixir.ingredient.viewmodel.IngredientViewModel
import com.example.elixir.ingredient.viewmodel.IngredientViewModelFactory
import com.example.elixir.network.AppDatabase
import com.example.elixir.recipe.viewmodel.RecipeViewModel
import com.example.elixir.recipe.data.RecipeItemData
import com.example.elixir.recipe.repository.RecipeRepository
import com.example.elixir.recipe.ui.adapter.RecipeListAdapter
import com.example.elixir.recipe.viewmodel.RecipeViewModelFactory
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

    private var ingredientDataMap: Map<Int, IngredientEntity>? = null
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
                result.data?.getStringExtra("recipeData")?.let {
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
        val appDB = AppDatabase.getInstance(requireContext())
        val recipeRepo = RecipeRepository(RetrofitClient.instanceRecipeApi, appDB.recipeDao())
        val ingredientRepo = IngredientRepository(RetrofitClient.instanceIngredientApi, appDB.ingredientDao())

        // 뷰모델 초기화
        recipeViewModel = ViewModelProvider(requireActivity(), RecipeViewModelFactory(recipeRepo))[RecipeViewModel::class.java]
        ingredientViewModel = ViewModelProvider(requireActivity(), IngredientViewModelFactory(ingredientRepo))[IngredientViewModel::class.java]

        binding.recipeList.layoutManager = LinearLayoutManager(requireContext())
        binding.fab.setOnClickListener {
            val intent = Intent(requireContext(), ToolbarActivity::class.java).apply {
                putExtra("mode", 9)
            }
            recipeRegisterLauncher.launch(intent)
        }

        binding.recipeList.layoutManager = LinearLayoutManager(requireContext())

        // 어댑터 초기화 (빈 식재료 데이터 전달)
        setupRecipeListAdapter(ingredientDataMap ?: emptyMap())

        // 레시피 리스트 옵저버 등록
        recipeViewModel.recipes.observe(viewLifecycleOwner) { pagingData ->
            recipeListAdapter.submitData(lifecycle, pagingData)
        }

        // 레시피 아이템 삭제 시 즉각 조회하여 리스트에 반영
        recipeViewModel.deleteResult.observe(viewLifecycleOwner) {
            refreshRecipes()
        }

        // 저장된 레시피 리스트 아이템에 반영
        recipeViewModel.recipes.value?.let {
            recipeListAdapter.submitData(lifecycle, it)
        }

        // 식재료 옵저버 등록
        ingredientViewModel.ingredients.observe(viewLifecycleOwner) { ingredients ->
            ingredientDataMap = ingredients.associateBy { it.id }
            recipeListAdapter.updateIngredientMap(ingredientDataMap ?: emptyMap())
        }

        // 식재료 로드
        ingredientViewModel.loadIngredients()

        // 추천 레시피
        refreshRecipes()
        loadRecommendRecipe()

        // 추천 레시피
        setupRecipeRegisterLauncher()
    }

    // 레시피 필터 리프래시
    private fun refreshRecipes() {
        // 저속노화 및 종류 조건대로 뷰 모델에 값 요청
        recipeViewModel.setCategoryType(selectedCategoryType)
        recipeViewModel.setCategorySlowAging(selectedSlowAging)

        // 재설정 버튼 표시 여부: 둘다 null이 아니라면 보이도록 설정
        val isResetVisible = !(selectedCategoryType == null && selectedSlowAging == null)
        recipeListAdapter.shouldShowResetButton = isResetVisible
        recipeListAdapter.notifyItemChanged(1)              // 검색 스피너 헤더 갱신
    }

    // 레시피 리스트 어댑터 설정
    private fun setupRecipeListAdapter(ingredientMap: Map<Int, IngredientEntity>) {
        val typeItems = resources.getStringArray(R.array.type_list).toList()
        val methodItems = resources.getStringArray(R.array.method_list).toList()

        recipeListAdapter = RecipeListAdapter(
            ingredientMap,
            // 스크랩: 스크랩 버튼 터치 시 만약에 스크랩 중이 아니라면 스크랩에 추가, 스크랩 중이라면 스크랩에서 제외
            onBookmarkClick = { recipe ->
                recipe.scrappedByCurrentUser = !recipe.scrappedByCurrentUser
                if (recipe.scrappedByCurrentUser) recipeViewModel.addScrap(recipe.id)
                else recipeViewModel.deleteScrap(recipe.id)
            },
            // 좋아요: 좋아요 버튼 터치 시 만약에 좋아요 중이 아니라면 좋아요 +1 및 갯수 갱신, 이미 누른 상태면 -1
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
            // 검색 버튼을 누르면 검색 창으로 넘어가기
            onSearchClicked = {
                parentFragmentManager.beginTransaction()
                    .replace(R.id.fullscreenContainer, SearchFragment())
                    .addToBackStack(null)
                    .commit()
            },
            // 레시피 종류 선택 시, 해당 레시피가 보이게 리사이클러뷰를 처음부터 아이템 갯수만큼 갱신
            onTypeSelected = { type ->
                selectedCategoryType = type
                recipeListAdapter.selectedType = selectedCategoryType
                recipeListAdapter.notifyItemRangeChanged(0, recipeListAdapter.itemCount)
                recipeViewModel.setCategoryType(selectedCategoryType)
                refreshRecipes()
            },
            // 레시피 저속노화 선택 시, 해당 레시피가 보이게 리사이클러뷰를 처음부터 아이템 갯수만큼 갱신
            onMethodSelected = { method ->
                selectedSlowAging = method
                recipeListAdapter.selectedSlowAging = selectedSlowAging
                recipeListAdapter.notifyItemRangeChanged(0, recipeListAdapter.itemCount)
                recipeViewModel.setCategorySlowAging(method)
                refreshRecipes()
            },
            // 검색 재설정 버튼을 누르면 전체 레시피 보여주기(저속노화와 종류 모두 null 값)
            onResetClicked = {
                selectedCategoryType = null
                selectedSlowAging = null
                recipeListAdapter.selectedType = selectedCategoryType
                recipeListAdapter.selectedSlowAging = selectedSlowAging
                recipeListAdapter.notifyItemRangeChanged(0, recipeListAdapter.itemCount)
                recipeViewModel.setCategoryType(selectedCategoryType)
                recipeViewModel.setCategorySlowAging(selectedSlowAging)
                refreshRecipes()
            }
        ).apply {
            this.typeItems = typeItems
            this.methodItems = methodItems
        }

        // 레시피 리스트에 어댑터 및 레이아웃 매니저 부착
        binding.recipeList.apply {
            adapter = recipeListAdapter
            layoutManager = LinearLayoutManager(context)
        }

        // 페이징으로부터 로딩 상태를 감지하여 결과가 없다는 내용을 보여줄 지 말지 결정
        recipeListAdapter.addLoadStateListener { loadStates ->
            Log.d(
                "RecipeFragment",
                "LoadState: ${loadStates.source.refresh}, itemCount: ${recipeListAdapter.itemCount}"
            )
            val realRecipeCount = recipeListAdapter.snapshot().items
                .filterIsInstance<RecipeItemData>() // ← 레시피 데이터 클래스 이름
                .size

            val isListEmpty =
                loadStates.source.refresh is LoadState.NotLoading &&
                        loadStates.append.endOfPaginationReached &&
                        realRecipeCount == 0

            Log.d(
                "RecipeFragment",
                "LoadState: ${loadStates.source.refresh}, " +
                        "endOfPaginationReached=${loadStates.append.endOfPaginationReached}, " +
                        "snapshotItems=${recipeListAdapter.snapshot().items.size}, " +
                        "isListEmpty: $isListEmpty"
            )

            // tvNoRecipe를 수정
            binding.tvNoRecipe.visibility = if (isListEmpty) View.VISIBLE else View.GONE
            binding.recipeList.visibility = View.VISIBLE
        }

        // 리사이클러뷰 스크롤 시, 검색 헤더가 안보일만큼 스크롤하면 항상 상단에 보이게 설정
        binding.recipeList.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(rv: RecyclerView, dx: Int, dy: Int) {
                handleStickyHeaderVisibility()
            }
        })
    }

    // 상단 스티키 헤더 표시 여부
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
    }

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

    override fun onResume() {
        super.onResume()
        // 프래그먼트 재실행 시, 기존 검색했던 조건대로 실행
        recipeViewModel.setCategoryType(selectedCategoryType)
        recipeViewModel.setCategorySlowAging(selectedSlowAging)
        recipeViewModel.loadRecipes()

        // 해당 프래그먼트를 떠나기 전 선택했던 값대로 스피너에 반영
        if (::recipeListAdapter.isInitialized) {
            recipeListAdapter.updateSearchHeader(
                selectedType = selectedCategoryType,
                selectedMethod = selectedSlowAging,
                typeItems = resources.getStringArray(R.array.type_list).toList(),
                methodItems = resources.getStringArray(R.array.method_list).toList()
            )
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
