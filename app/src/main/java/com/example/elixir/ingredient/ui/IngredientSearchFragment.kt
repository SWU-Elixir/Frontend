package com.example.elixir.ingredient.ui

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.widget.ImageViewCompat
import androidx.fragment.app.Fragment
import android.content.res.ColorStateList
import com.example.elixir.R
import com.example.elixir.databinding.FragmentIndeterminateSearchBinding
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.elixir.ingredient.network.IngredientDB
import com.example.elixir.ingredient.network.IngredientRepository
import com.example.elixir.ingredient.viewmodel.IngredientViewModel
import com.example.elixir.RetrofitClient
import com.example.elixir.ingredient.data.IngredientData
import androidx.recyclerview.widget.RecyclerView
import com.example.elixir.databinding.ItemRecipeListTagBinding

class IngredientSearchFragment : Fragment() {

    private var _binding: FragmentIndeterminateSearchBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: IngredientViewModel
    private lateinit var Adapter: IngredientSearchListAdapter
    private var allIngredients: List<IngredientData> = emptyList()

    private val ingredientCategories = listOf(
        "전체", "농산물", "수산물", "육류", "난류·유제품", "과자·빵·떡",
        "가공농산물·유지", "음료·차", "조미료·장·절임", "기타"
    )
    private var selectedCategory: String = ingredientCategories[0]

    // 카테고리 어댑터
    private lateinit var categoryAdapter: IngredientCategoryAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentIndeterminateSearchBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        try {
            // Room DB, Retrofit, Repository, ViewModel 연결
            Log.d("IngredientSearch", "Initializing components...")
            val db = IngredientDB.getInstance(requireContext())
            val api = RetrofitClient.instanceIngredientApi
            Log.d("IngredientSearch", "API instance created: ${api != null}")
            
            val repository = IngredientRepository(api, db.ingredientDao())
            viewModel = IngredientViewModel(repository)
            Log.d("IngredientSearch", "ViewModel initialized")

            // 초기 데이터 로드
            Log.d("IngredientSearch", "Starting initial data load...")
            viewModel.loadIngredients()

            setupRecyclerView()
            setupSearchListeners()
            setupBackButton()
            setupCategoryRecyclerView()
            
            // 로딩 상태 표시
            binding.tvProgress.visibility = View.VISIBLE
            binding.listSearch.visibility = View.GONE
            
        } catch (e: Exception) {
            Log.e("IngredientSearch", "Initialization error: ${e.message}", e)
            Toast.makeText(requireContext(), "초기화 중 오류가 발생했습니다: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun setupRecyclerView() {
        Adapter = IngredientSearchListAdapter(emptyList()) { selectedItem ->
            try {
                Log.d("IngredientSearch", "Selected ingredient: ${selectedItem.name} (ID: ${selectedItem.id})")
                // 선택된 식재료를 이전 화면으로 전달
                val result = Bundle().apply {
                    putInt("ingredientId", selectedItem.id)
                    putString("ingredientName", selectedItem.name)
                    putString("ingredientType", selectedItem.type)
                }
                parentFragmentManager.setFragmentResult("ingredient_selection", result)
                parentFragmentManager.popBackStack()
            } catch (e: Exception) {
                Log.e("IngredientSearch", "Error sending result: ${e.message}", e)
                Toast.makeText(requireContext(), "식재료 선택 중 오류가 발생했습니다.", Toast.LENGTH_SHORT).show()
            }
        }
        
        binding.listSearch.apply {
            adapter = Adapter
            layoutManager = LinearLayoutManager(requireContext())
        }

        // 데이터 관찰
        viewModel.ingredients.observe(viewLifecycleOwner) { items ->
            Log.d("IngredientSearch", "Received ${items.size} ingredients")
            allIngredients = items
            Adapter.updateData(items)
            
            // 로딩 상태 업데이트
            binding.tvProgress.visibility = View.GONE
            if (items.isEmpty()) {
                binding.tvNoRecipe.visibility = View.VISIBLE
                binding.listSearch.visibility = View.GONE
            } else {
                binding.tvNoRecipe.visibility = View.GONE
                binding.listSearch.visibility = View.VISIBLE
            }
        }

        // 에러 처리
        viewModel.error.observe(viewLifecycleOwner) { error ->
            error?.let {
                Log.e("IngredientSearch", "데이터 로드 실패: $it", Exception(it))
                binding.tvProgress.visibility = View.GONE
                binding.tvNoRecipe.visibility = View.VISIBLE
                binding.listSearch.visibility = View.GONE
                Toast.makeText(requireContext(), "데이터를 불러오는데 실패했습니다. 다시 시도해주세요.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupSearchListeners() {
        // 텍스트 변경 리스너
        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val input = s.toString().trim()
                updateSearchUI(input.isNotEmpty())
                // 실시간 검색 수행
                filterByCategoryAndSearch()
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })


        // 키보드 검색 버튼
        binding.etSearch.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                hideKeyboard()
                // 이미 실시간으로 검색되므로 별도의 검색 수행 불필요
                true
            } else {
                false
            }
        }

        // 삭제 버튼
        binding.btnClear.setOnClickListener {
            binding.etSearch.setText("")
            hideKeyboard()
            Adapter.updateData(allIngredients)
            updateSearchUI(false)
            binding.listSearch.visibility = View.VISIBLE
            binding.tvNoRecipe.visibility = View.GONE
        }
    }

    private fun setupBackButton() {
        binding.btnBack.setOnClickListener {
            hideKeyboard()
            parentFragmentManager.popBackStack()
        }
    }

    private fun updateSearchUI(isSearching: Boolean) {
        val context = requireContext()
        binding.btnClear.visibility = if (isSearching) View.VISIBLE else View.GONE
    }

    private fun hideKeyboard() {
        val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(binding.etSearch.windowToken, 0)
    }

    private fun setupCategoryRecyclerView() {
        categoryAdapter = IngredientCategoryAdapter(ingredientCategories) { category ->
            selectedCategory = category
            filterByCategoryAndSearch()
        }
        binding.rvCategory.adapter = categoryAdapter
        binding.rvCategory.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        // 최초 선택
        categoryAdapter.setSelectedCategory(selectedCategory)
    }

    private fun filterByCategoryAndSearch() {
        val keyword = binding.etSearch.text.toString().trim()
        val filtered = allIngredients.filter {
            (selectedCategory == "전체" || (it.categoryGroup?.trim() == selectedCategory)) &&
            (keyword.isEmpty() || it.name?.contains(keyword, ignoreCase = true) == true || (it.type?.contains(keyword, ignoreCase = true) == true))
        }
        Adapter.updateData(filtered)
        updateSearchResults(filtered.isEmpty())
    }


    private fun updateSearchResults(isEmpty: Boolean) {
        binding.listSearch.visibility = if (isEmpty) View.GONE else View.VISIBLE
        binding.tvNoRecipe.visibility = if (isEmpty) View.VISIBLE else View.GONE
    }

    override fun onResume() {
        super.onResume()
        // 키보드 자동 표시 제거
        binding.etSearch.requestFocus()
    }

    override fun onPause() {
        super.onPause()
        hideKeyboard()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        hideKeyboard()
        _binding = null
    }

    // 카테고리 어댑터 클래스
    class IngredientCategoryAdapter(
        private val categories: List<String>,
        private val onClick: (String) -> Unit
    ) : RecyclerView.Adapter<IngredientCategoryAdapter.ViewHolder>() {
        private var selectedPosition = 0
        inner class ViewHolder(val binding: ItemRecipeListTagBinding) : RecyclerView.ViewHolder(binding.root) {
            fun bind(category: String, isSelected: Boolean) {
                binding.tvIndeterminateName.text = category
                binding.root.isSelected = isSelected
                binding.root.setBackgroundResource(
                    if (isSelected) R.drawable.bg_rect_filled_orange_5 else R.drawable.bg_rect_outline_gray_5
                )
                binding.tvIndeterminateName.setTextColor(
                    binding.root.context.getColor(
                        if (isSelected) R.color.white else R.color.black
                    )
                )
                binding.root.setOnClickListener {
                    val prev = selectedPosition
                    selectedPosition = adapterPosition
                    notifyItemChanged(prev)
                    notifyItemChanged(selectedPosition)
                    onClick(category)
                }
            }
        }
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
            ViewHolder(ItemRecipeListTagBinding.inflate(LayoutInflater.from(parent.context), parent, false))
        override fun onBindViewHolder(holder: ViewHolder, position: Int) =
            holder.bind(categories[position], position == selectedPosition)
        override fun getItemCount() = categories.size
        fun setSelectedCategory(category: String) {
            val idx = categories.indexOf(category)
            if (idx != -1) {
                val prev = selectedPosition
                selectedPosition = idx
                notifyItemChanged(prev)
                notifyItemChanged(selectedPosition)
            }
        }
    }
}
