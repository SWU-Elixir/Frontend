package com.example.elixir.Ingredient

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
import com.example.elixir.signup.RetrofitClient

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

        // Room DB, Retrofit, Repository, Service, ViewModel 연결
        val db = IngredientDB.getInstance(requireContext())
        val api = RetrofitClient.instanceIngredientApi
        val repository = IngredientRepository(api, db.ingredientDao())
        val service = IngredientService(repository)
        viewModel = IngredientViewModel(service)

        Adapter = IngredientSearchListAdapter(emptyList()) { selectedItem ->
            // 선택된 식재료를 이전 화면으로 전달
            val result = Bundle().apply {
                putInt("ingredientId", selectedItem.id)
                putString("ingredientName", selectedItem.name)
                putString("ingredientType", selectedItem.type)
            }
            parentFragmentManager.setFragmentResult("ingredient_selection", result)
            parentFragmentManager.popBackStack()
        }
        binding.searchList.adapter = Adapter
        binding.searchList.layoutManager = LinearLayoutManager(requireContext())

        // 데이터 관찰
        viewModel.ingredients.observe(viewLifecycleOwner) { items ->
            allIngredients = items
            Adapter.updateData(items)
        }

        // 에러 처리
        viewModel.error.observe(viewLifecycleOwner) { error ->
            error?.let {
                Log.e("YourTag", "데이터 로드 실패: $it")
                Toast.makeText(requireContext(), "데이터 로드 실패: $it", Toast.LENGTH_SHORT).show()
            }
        }

        // 데이터 로드
        viewModel.loadIngredients()

        // 텍스트 변경에 따라 검색 버튼 색상 및 삭제 버튼 가시성 제어
        binding.searchEditText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val input = s.toString().trim()
                val context = requireContext()

                if (input.isNotEmpty()) {
                    ImageViewCompat.setImageTintList(
                        binding.searchButton,
                        ColorStateList.valueOf(ContextCompat.getColor(context,
                            R.color.elixir_orange
                        ))
                    )
                    binding.clearButton.visibility = View.VISIBLE
                } else {
                    ImageViewCompat.setImageTintList(
                        binding.searchButton,
                        ColorStateList.valueOf(ContextCompat.getColor(context, R.color.elixir_gray))
                    )
                    binding.clearButton.visibility = View.GONE
                }

                Log.d("EditText", "입력된 값: $input")
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        // 검색 버튼 클릭 시 검색 실행
        binding.searchButton.setOnClickListener {
            performSearch()
        }

        // 키보드의 검색 버튼 동작 처리
        binding.searchEditText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                performSearch()
                true
            } else {
                false
            }
        }

        // 삭제 버튼 클릭 시 입력 초기화
        binding.clearButton.setOnClickListener {
            binding.searchEditText.setText("")
            // 전체 리스트 다시 표시
            Adapter.updateData(allIngredients)
            binding.searchList.visibility = View.VISIBLE
            binding.emptyRecipeText.visibility = View.GONE
        }

        // 뒤로 가기 버튼 클릭 시 이전 프래그먼트로 돌아가기
        binding.backButton.setOnClickListener {
            parentFragmentManager.popBackStack()
        }


    }

    private fun performSearch() {
        val keyword = binding.searchEditText.text.toString().trim()
        if (keyword.isNotEmpty()) {
            val filtered = allIngredients.filter {
                (it.name?.contains(keyword, ignoreCase = true) == true) ||
                        (it.type?.contains(keyword, ignoreCase = true) == true)
            }
            Adapter.updateData(filtered)
            // 결과 없을 시 안내 텍스트 표시
            if (filtered.isEmpty()) {
                binding.searchList.visibility = View.GONE
                binding.emptyRecipeText.visibility = View.VISIBLE
            } else {
                binding.searchList.visibility = View.VISIBLE
                binding.emptyRecipeText.visibility = View.GONE
            }
        } else {
            Toast.makeText(requireContext(), getString(R.string.search_put_something), Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * 프래그먼트 재진입 시 자동으로 키보드가 올라오게 처리
     */
    override fun onResume() {
        super.onResume()

        binding.searchEditText.requestFocus()
        val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(binding.searchEditText, InputMethodManager.SHOW_IMPLICIT)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
