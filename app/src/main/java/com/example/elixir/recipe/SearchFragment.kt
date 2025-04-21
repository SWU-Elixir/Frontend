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

        backButton = view.findViewById(R.id.backButton)
        clearButton = view.findViewById(R.id.clearButton)
        searchButton = view.findViewById(R.id.searchButton)
        searchEditText = view.findViewById(R.id.searchEditText)

        popularSearchList = view.findViewById(R.id.popularSearchList)
        recommendationSearchList = view.findViewById(R.id.recommendationSearchList)

        // arguments 에서 전달된 keyword 받아서 EditText에 미리 넣기
        val passedKeyword = arguments?.getString("search_keyword")
        if (!passedKeyword.isNullOrBlank()) {
            searchEditText.setText(passedKeyword)
            searchEditText.setSelection(passedKeyword.length) // 커서 맨 끝으로 이동

            ImageViewCompat.setImageTintList(
                searchButton,
                ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.elixir_orange))
            )

            clearButton.visibility = View.VISIBLE

            searchEditText.requestFocus()
            val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showSoftInput(searchEditText, InputMethodManager.SHOW_IMPLICIT)
        }

        searchEditText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val input = s.toString().trim()
                val context = requireContext()

                // 텍스트가 있을 때
                if (input.isNotEmpty()) {
                    // tint 색상을 오렌지로
                    ImageViewCompat.setImageTintList(
                        searchButton,
                        ColorStateList.valueOf(ContextCompat.getColor(context, R.color.elixir_orange))
                    )
                    // clear 버튼 보여주기
                    clearButton.visibility = View.VISIBLE
                } else {
                    // tint 제거 또는 기본색으로
                    ImageViewCompat.setImageTintList(
                        searchButton,
                        ColorStateList.valueOf(ContextCompat.getColor(context, R.color.elixir_gray)) // 기본 색상
                    )
                    clearButton.visibility = View.GONE
                }

                Log.d("EditText", "입력된 값: $input")
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        searchButton.setOnClickListener {
            performSearch()
        }

        searchEditText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                performSearch()
                true
            } else {
                false
            }
        }

        clearButton.setOnClickListener {
            searchEditText.setText("")
        }


        // 1~5 더미 데이터
        val dummyData = listOf("1", "2", "3", "4", "5")

        popularSearchList.apply {
            layoutManager = FlexboxLayoutManager(context).apply {
                flexDirection = FlexDirection.ROW
                justifyContent = JustifyContent.FLEX_START
            }
            popularSearchList.adapter = MealListIngredientAdapter(dummyData)
        }

        recommendationSearchList.apply {
            layoutManager = FlexboxLayoutManager(context).apply {
                flexDirection = FlexDirection.ROW
                justifyContent = JustifyContent.FLEX_START
            }
            recommendationSearchList.adapter = RecipeIngredientAdapter(dummyData)
        }

        // 뒤로가기: 프래그먼트 닫기 (BackStack)
        backButton.setOnClickListener {
            parentFragmentManager.popBackStack()
        }


        return view
    }

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

    override fun onResume() {
        super.onResume()

        // EditText에 포커스 주고 키보드 자동으로 열기
        searchEditText.requestFocus()
        val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(searchEditText, InputMethodManager.SHOW_IMPLICIT)
    }
}