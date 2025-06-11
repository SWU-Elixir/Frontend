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
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.elixir.R
import com.example.elixir.RetrofitClient
import com.example.elixir.databinding.FragmentRecipeSearchListBinding
import com.example.elixir.ingredient.network.IngredientDB
import com.example.elixir.ingredient.network.IngredientRepository
import com.example.elixir.ingredient.viewmodel.IngredientService
import com.example.elixir.ingredient.viewmodel.IngredientViewModel
import com.example.elixir.recipe.data.RecipeData
import java.math.BigInteger

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
    private lateinit var sampleRecipes: List<RecipeData>
    private var hasNavigatedToSearch = false // 중복 이동 방지 플래그

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRecipeSearchListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 1. 검색어 초기화 및 설정
        initializeSearchKeyword()

        // 2. 검색 관련 이벤트 설정
        setupSearchEvents()

        // 3. 스피너 설정
        setupSpinners()

        // 4. 리스트 초기화
        initializeRecipeList()

        // 5. 뒤로가기 버튼 설정
        setupBackButton()
    }

    /**
     * 검색어 초기화 및 설정
     */
    private fun initializeSearchKeyword() {
        val keyword = arguments?.getString("search_keyword")?.trim()
        binding.searchEditText.setText(keyword)
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
                if (!hasNavigatedToSearch) {
                    navigateToSearch()
                }
            }
        })

        // 검색 버튼 클릭
        binding.searchButton.setOnClickListener {
            if (!hasNavigatedToSearch) {
                navigateToSearch()
            }
        }

        // 키보드 검색 버튼
        binding.searchEditText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                val keyword = binding.searchEditText.text.toString().trim()
                if (keyword.isNotEmpty()) {
                    filterRecipes(keyword)
                    resetSpinners()
                    updateResetButtonVisibility()
                } else {
                    Toast.makeText(requireContext(), R.string.search_put_something, Toast.LENGTH_SHORT).show()
                }
                true
            } else {
                false
            }
        }
    }

    /**
     * SearchFragment로 이동
     */
    private fun navigateToSearch() {
        hasNavigatedToSearch = true
        val currentKeyword = binding.searchEditText.text.toString().trim()
        val searchFragment = SearchFragment().apply {
            arguments = Bundle().apply {
                putString("search_keyword", currentKeyword)
            }
        }
        parentFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, searchFragment)
            .addToBackStack(null)
            .commit()
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
            filterRecipes(binding.searchEditText.text.toString())
        }
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
                binding.spinnerDifficulty.isSelected = position != 0
                updateResetButtonVisibility()
                filterRecipes(binding.searchEditText.text.toString())
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
                binding.spinnerType.isSelected = position != 0
                updateResetButtonVisibility()
                filterRecipes(binding.searchEditText.text.toString())
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }

    /**
     * 레시피 리스트 초기화
     */
    private fun initializeRecipeList() {
        val ingredientRepository = IngredientRepository(
            RetrofitClient.instanceIngredientApi,
            IngredientDB.getInstance(requireContext()).ingredientDao())
        val ingredientService = IngredientService(ingredientRepository)
        val ingredientViewModel = IngredientViewModel(ingredientService)

        // RecyclerView 설정
        binding.recipeList.layoutManager = LinearLayoutManager(requireContext())

        // 어댑터 초기화
        ingredientViewModel.ingredients.observe(viewLifecycleOwner) { ingredientList ->
            recipeListAdapter = RecipeListAdapter(
                sampleRecipes.toMutableList(),
                ingredientList,
                onBookmarkClick = { recipe ->
                    recipe.scrappedByCurrentUser = !recipe.scrappedByCurrentUser
                    recipeListAdapter.notifyItemChanged(sampleRecipes.indexOf(recipe))
                },
                onHeartClick = { recipe ->
                    recipe.likedByCurrentUser = !recipe.likedByCurrentUser
                    recipeListAdapter.notifyItemChanged(sampleRecipes.indexOf(recipe))
                },
                fragmentManager = parentFragmentManager
            )
            binding.recipeList.adapter = recipeListAdapter
        }

        // 검색어가 있으면 필터 적용
        val keyword = binding.searchEditText.text.toString()
        if (keyword.isNotEmpty()) {
            filterRecipes(keyword)
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
     * 레시피 필터링
     * 검색어, 저속노화 방법, 레시피 종류에 따라 필터링
     */
    private fun filterRecipes(keyword: String) {
        val selectedMethod = binding.spinnerDifficulty.selectedItem?.toString()
        val selectedType = binding.spinnerType.selectedItem?.toString()

        val filtered = sampleRecipes.filter { recipe ->
            val keywordMatch = recipe.title.contains(keyword, ignoreCase = true)
                    || recipe.ingredients.any { it.name.contains(keyword, ignoreCase = true) }
                    || recipe.ingredients.any { it.value.contains(keyword, ignoreCase = true) }
                    || recipe.ingredients.any { it.unit.contains(keyword, ignoreCase = true) }
            val methodMatch = selectedMethod == "저속노화" || recipe.categorySlowAging == selectedMethod
            val typeMatch = selectedType == "종류" || recipe.categoryType == selectedType
            keywordMatch && methodMatch && typeMatch
        }
        // 결과 적용
        recipeListAdapter.updateData(filtered)

        // 결과 없을 시 안내 텍스트 표시
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

