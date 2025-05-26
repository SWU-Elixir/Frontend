package com.example.elixir.recipe.ui

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
import com.example.elixir.R
import com.example.elixir.calendar.ui.MealListIngredientAdapter
import com.example.elixir.databinding.FragmentRecipeSearchBinding
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.flexbox.JustifyContent
import android.content.res.ColorStateList

class SearchFragment : Fragment() {

    private var _binding: FragmentRecipeSearchBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRecipeSearchBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 전달받은 검색어가 있을 경우 EditText에 미리 세팅하고 커서 위치, 버튼 상태도 설정
        val passedKeyword = arguments?.getString("search_keyword")
        if (!passedKeyword.isNullOrBlank()) {
            binding.searchEditText.setText(passedKeyword)
            binding.searchEditText.setSelection(passedKeyword.length)

            // 검색 버튼 색상 오렌지로 변경
            ImageViewCompat.setImageTintList(
                binding.searchButton,
                ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.elixir_orange))
            )

            binding.clearButton.visibility = View.VISIBLE

            // 키보드 자동 표시
            binding.searchEditText.requestFocus()
            val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showSoftInput(binding.searchEditText, InputMethodManager.SHOW_IMPLICIT)
        }

        // 텍스트 변경에 따라 검색 버튼 색상 및 삭제 버튼 가시성 제어
        binding.searchEditText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val input = s.toString().trim()
                val context = requireContext()

                if (input.isNotEmpty()) {
                    ImageViewCompat.setImageTintList(
                        binding.searchButton,
                        ColorStateList.valueOf(ContextCompat.getColor(context, R.color.elixir_orange))
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
        }

        // 인기 검색어 리스트 Flexbox 설정
        /*val dummyData = listOf("1", "2", "3", "4", "5")
        binding.popularSearchList.apply {
            layoutManager = FlexboxLayoutManager(context).apply {
                flexDirection = FlexDirection.ROW
                justifyContent = JustifyContent.FLEX_START
            }
            adapter = MealListIngredientAdapter(dummyData)
        }

        // 추천 검색어 리스트 Flexbox 설정
        binding.recommendationSearchList.apply {
            layoutManager = FlexboxLayoutManager(context).apply {
                flexDirection = FlexDirection.ROW
                justifyContent = JustifyContent.FLEX_START
            }
            adapter = FlavoringAdapter(dummyData)
        }*/

        // 뒤로 가기 버튼 클릭 시 이전 프래그먼트로 돌아가기
        binding.backButton.setOnClickListener {
            parentFragmentManager.popBackStack()
        }
    }

    /**
     * 검색 실행 함수
     * 키워드가 비어있지 않으면 SearchListFragment로 이동
     */
    private fun performSearch() {
        val keyword = binding.searchEditText.text.toString().trim()

        if (keyword.isNotEmpty()) {
            Toast.makeText(requireContext(), "검색어: $keyword", Toast.LENGTH_SHORT).show()
            Log.d("SearchFragment", "입력된 검색어: $keyword")

            parentFragmentManager.popBackStack()

            val searchResultFragment = SearchListFragment().apply {
                arguments = Bundle().apply {
                    putString("search_keyword", keyword)
                }
            }

            parentFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, searchResultFragment)
                .addToBackStack(null)
                .commit()
        } else {
            Toast.makeText(requireContext(),
                getString(R.string.search_put_something), Toast.LENGTH_SHORT).show()
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
