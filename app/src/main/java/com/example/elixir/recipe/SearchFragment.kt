package com.example.elixir.recipe

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.example.elixir.R
import com.example.elixir.calendar.MealListIngredientAdapter
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.flexbox.JustifyContent
import androidx.core.content.ContextCompat
import androidx.core.widget.ImageViewCompat
import android.content.res.ColorStateList
import android.view.inputmethod.InputMethodManager

class SearchFragment : Fragment() {

    // UI 요소 선언
    private lateinit var clearButton: ImageButton
    private lateinit var searchButton: ImageButton
    private lateinit var backButton: ImageButton
    private lateinit var searchEditText: EditText
    private lateinit var popularSearchList: RecyclerView
    private lateinit var recommendationSearchList: RecyclerView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_recipe_search, container, false)

        // 뷰 초기화
        backButton = view.findViewById(R.id.backButton)
        clearButton = view.findViewById(R.id.clearButton)
        searchButton = view.findViewById(R.id.searchButton)
        searchEditText = view.findViewById(R.id.searchEditText)
        popularSearchList = view.findViewById(R.id.popularSearchList)
        recommendationSearchList = view.findViewById(R.id.recommendationSearchList)

        // 전달받은 검색어가 있을 경우 EditText에 미리 세팅하고 커서 위치, 버튼 상태도 설정
        val passedKeyword = arguments?.getString("search_keyword")
        if (!passedKeyword.isNullOrBlank()) {
            searchEditText.setText(passedKeyword)
            searchEditText.setSelection(passedKeyword.length)

            // 검색 버튼 색상 오렌지로 변경
            ImageViewCompat.setImageTintList(
                searchButton,
                ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.elixir_orange))
            )

            clearButton.visibility = View.VISIBLE

            // 키보드 자동 표시
            searchEditText.requestFocus()
            val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showSoftInput(searchEditText, InputMethodManager.SHOW_IMPLICIT)
        }

        // 텍스트 변경에 따라 검색 버튼 색상 및 삭제 버튼 가시성 제어
        searchEditText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val input = s.toString().trim()
                val context = requireContext()

                if (input.isNotEmpty()) {
                    ImageViewCompat.setImageTintList(
                        searchButton,
                        ColorStateList.valueOf(ContextCompat.getColor(context, R.color.elixir_orange))
                    )
                    clearButton.visibility = View.VISIBLE
                } else {
                    ImageViewCompat.setImageTintList(
                        searchButton,
                        ColorStateList.valueOf(ContextCompat.getColor(context, R.color.elixir_gray))
                    )
                    clearButton.visibility = View.GONE
                }

                Log.d("EditText", "입력된 값: $input")
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        // 검색 버튼 클릭 시 검색 실행
        searchButton.setOnClickListener {
            performSearch()
        }

        // 키보드의 검색 버튼 동작 처리
        searchEditText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                performSearch()
                true
            } else {
                false
            }
        }

        // 삭제 버튼 클릭 시 입력 초기화
        clearButton.setOnClickListener {
            searchEditText.setText("")
        }

        // 인기 검색어 리스트 Flexbox 설정
        val dummyData = listOf("1", "2", "3", "4", "5")
        popularSearchList.apply {
            layoutManager = FlexboxLayoutManager(context).apply {
                flexDirection = FlexDirection.ROW
                justifyContent = JustifyContent.FLEX_START
            }
            adapter = MealListIngredientAdapter(dummyData)
        }

        // 추천 검색어 리스트 Flexbox 설정
        recommendationSearchList.apply {
            layoutManager = FlexboxLayoutManager(context).apply {
                flexDirection = FlexDirection.ROW
                justifyContent = JustifyContent.FLEX_START
            }
            adapter = RecipeIngredientAdapter(dummyData)
        }

        // 뒤로 가기 버튼 클릭 시 이전 프래그먼트로 돌아가기
        backButton.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        return view
    }

    /**
     * 검색 실행 함수
     * 키워드가 비어있지 않으면 SearchListFragment로 이동
     */
    private fun performSearch() {
        val keyword = searchEditText.text.toString().trim()

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
            Toast.makeText(requireContext(), "검색어를 입력하세요", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * 프래그먼트 재진입 시 자동으로 키보드가 올라오게 처리
     */
    override fun onResume() {
        super.onResume()

        searchEditText.requestFocus()
        val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(searchEditText, InputMethodManager.SHOW_IMPLICIT)
    }
}
