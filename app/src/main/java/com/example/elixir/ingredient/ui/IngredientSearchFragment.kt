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
import com.example.elixir.ingredient.data.IngredientItem
import com.example.elixir.ingredient.network.IngredientDB
import com.example.elixir.ingredient.network.IngredientRepository
import com.example.elixir.ingredient.viewmodel.IngredientService
import com.example.elixir.ingredient.viewmodel.IngredientViewModel
import com.example.elixir.RetrofitClient

class IngredientSearchFragment : Fragment() {

    private var _binding: FragmentIndeterminateSearchBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: IngredientViewModel
    private lateinit var Adapter: IngredientSearchListAdapter
    private var allIngredients: List<IngredientItem> = emptyList()

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
            // Room DB, Retrofit, Repository, Service, ViewModel 연결
            Log.d("IngredientSearch", "Initializing components...")
            val db = IngredientDB.getInstance(requireContext())
            val api = RetrofitClient.instanceIngredientApi
            Log.d("IngredientSearch", "API instance created: ${api != null}")
            
            val repository = IngredientRepository(api, db.ingredientDao())
            val service = IngredientService(repository)
            viewModel = IngredientViewModel(service)
            Log.d("IngredientSearch", "ViewModel initialized")

            // 초기 데이터 로드
            Log.d("IngredientSearch", "Starting initial data load...")
            viewModel.loadIngredients()

            setupRecyclerView()
            setupSearchListeners()
            setupBackButton()
            
            // 로딩 상태 표시
            binding.progressBar.visibility = View.VISIBLE
            binding.searchList.visibility = View.GONE
            
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
        
        binding.searchList.apply {
            adapter = Adapter
            layoutManager = LinearLayoutManager(requireContext())
        }

        // 데이터 관찰
        viewModel.ingredients.observe(viewLifecycleOwner) { items ->
            Log.d("IngredientSearch", "Received ${items.size} ingredients")
            allIngredients = items
            Adapter.updateData(items)
            
            // 로딩 상태 업데이트
            binding.progressBar.visibility = View.GONE
            if (items.isEmpty()) {
                binding.emptyRecipeText.visibility = View.VISIBLE
                binding.searchList.visibility = View.GONE
            } else {
                binding.emptyRecipeText.visibility = View.GONE
                binding.searchList.visibility = View.VISIBLE
            }
        }

        // 에러 처리
        viewModel.error.observe(viewLifecycleOwner) { error ->
            error?.let {
                Log.e("IngredientSearch", "데이터 로드 실패: $it", Exception(it))
                binding.progressBar.visibility = View.GONE
                binding.emptyRecipeText.visibility = View.VISIBLE
                binding.searchList.visibility = View.GONE
                Toast.makeText(requireContext(), "데이터를 불러오는데 실패했습니다. 다시 시도해주세요.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupSearchListeners() {
        // 텍스트 변경 리스너
        binding.searchEditText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val input = s.toString().trim()
                updateSearchUI(input.isNotEmpty())
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        // 검색 버튼 클릭
        binding.searchButton.setOnClickListener {
            hideKeyboard()
            performSearch()
        }

        // 키보드 검색 버튼
        binding.searchEditText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                hideKeyboard()
                performSearch()
                true
            } else {
                false
            }
        }

        // 삭제 버튼
        binding.clearButton.setOnClickListener {
            binding.searchEditText.setText("")
            hideKeyboard()
            Adapter.updateData(allIngredients)
            updateSearchUI(false)
            binding.searchList.visibility = View.VISIBLE
            binding.emptyRecipeText.visibility = View.GONE
        }
    }

    private fun setupBackButton() {
        binding.backButton.setOnClickListener {
            hideKeyboard()
            parentFragmentManager.popBackStack()
        }
    }

    private fun updateSearchUI(isSearching: Boolean) {
        val context = requireContext()
        ImageViewCompat.setImageTintList(
            binding.searchButton,
            ColorStateList.valueOf(
                ContextCompat.getColor(
                    context,
                    if (isSearching) R.color.elixir_orange else R.color.elixir_gray
                )
            )
        )
        binding.clearButton.visibility = if (isSearching) View.VISIBLE else View.GONE
    }

    private fun hideKeyboard() {
        val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(binding.searchEditText.windowToken, 0)
    }

    private fun performSearch() {
        val keyword = binding.searchEditText.text.toString().trim()
        if (keyword.isNotEmpty()) {
            try {
                Log.d("IngredientSearch", "Performing search for: $keyword")
                binding.progressBar.visibility = View.VISIBLE
                
                val filtered = allIngredients.filter {
                    (it.name?.contains(keyword, ignoreCase = true) == true) ||
                            (it.type?.contains(keyword, ignoreCase = true) == true)
                }
                Log.d("IngredientSearch", "Found ${filtered.size} results")
                
                Adapter.updateData(filtered)
                updateSearchResults(filtered.isEmpty())
            } catch (e: Exception) {
                Log.e("IngredientSearch", "Search error: ${e.message}", e)
                Toast.makeText(requireContext(), "검색 중 오류가 발생했습니다.", Toast.LENGTH_SHORT).show()
            } finally {
                binding.progressBar.visibility = View.GONE
            }
        } else {
            Toast.makeText(requireContext(), getString(R.string.search_put_something), Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateSearchResults(isEmpty: Boolean) {
        binding.searchList.visibility = if (isEmpty) View.GONE else View.VISIBLE
        binding.emptyRecipeText.visibility = if (isEmpty) View.VISIBLE else View.GONE
    }

    override fun onResume() {
        super.onResume()
        // 키보드 자동 표시 제거
        binding.searchEditText.requestFocus()
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
}
