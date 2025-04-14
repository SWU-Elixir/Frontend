package com.example.elixir

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
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.flexbox.JustifyContent


class SearchFragment : Fragment() {

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
        searchButton = view.findViewById(R.id.searchButton)
        searchEditText = view.findViewById(R.id.searchEditText)

        popularSearchList = view.findViewById(R.id.popularSearchList)
        recommendationSearchList = view.findViewById(R.id.recommendationSearchList)

        // 입력값 가져오기
        val userInput = searchEditText.text.toString()

        // 텍스트 변경 감지
        // searchButton은 onCreateView 또는 onViewCreated에서 미리 findViewById로 초기화되어 있어야 함
        searchEditText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val input = s.toString().trim()

                if (input.isNotEmpty()) {
                    // 입력이 있으면 검색 아이콘 변경 (예: 활성화된 아이콘)
                    searchButton.setImageResource(R.drawable.ic_delete)
                } else {
                    // 입력이 없으면 기본 아이콘으로 복구
                    searchButton.setImageResource(R.drawable.ic_recipe_search)
                }

                Log.d("EditText", "입력된 값: $input")
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        // 1~5 더미 데이터
        val dummyData = listOf("1", "2", "3", "4", "5")

        popularSearchList.apply {
            layoutManager = FlexboxLayoutManager(context).apply {
                flexDirection = FlexDirection.ROW
                justifyContent = JustifyContent.FLEX_START
            }
            popularSearchList.adapter = DietLogIngredientAdapter(dummyData)
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

        searchButton.setOnClickListener {
            searchEditText.setText("")
        }
        searchEditText.setOnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                val keyword = searchEditText.text.toString().trim()

                if (keyword.isNotEmpty()) {
                    Toast.makeText(requireContext(), "검색어: $keyword", Toast.LENGTH_SHORT).show()
                    Log.d("SearchFragment", "입력된 검색어: $keyword")

                    // 현재 프래그먼트 닫기
                    parentFragmentManager.popBackStack()

                    // 새로운 프래그먼트로 전환
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
                true // 이벤트 소비
            } else {
                false // 다른 키는 처리하지 않음
            }
        }



        return view
    }
}