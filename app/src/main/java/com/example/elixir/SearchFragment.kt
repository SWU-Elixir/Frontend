package com.example.elixir

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
        searchEditText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                Log.d("EditText", "입력된 값: ${s.toString()}")
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

        // 검색 버튼 → 프래그먼트 닫고 다른 곳에 새 프래그먼트 띄우기
        searchButton.setOnClickListener {
            val keyword = searchEditText.text.toString().trim()

            if (keyword.isNotEmpty()) {
                Log.d("SearchFragment", "검색어: $keyword")

                // 새로운 프래그먼트 생성 + Bundle 전달
                val searchListFragment = SearchListFragment()
                val bundle = Bundle().apply {
                    putString("search_keyword", keyword)
                }
                searchListFragment.arguments = bundle

                // 현재 프래그먼트 닫기
                parentFragmentManager.popBackStack()

                // 새로운 프래그먼트로 전환
                val transaction = parentFragmentManager.beginTransaction()
                transaction.replace(R.id.fragmentContainer, searchListFragment)
                transaction.addToBackStack(null)
                transaction.commit()

            } else {
                Toast.makeText(requireContext(), "검색어를 입력하세요", Toast.LENGTH_SHORT).show()
            }
        }



        return view
    }
}