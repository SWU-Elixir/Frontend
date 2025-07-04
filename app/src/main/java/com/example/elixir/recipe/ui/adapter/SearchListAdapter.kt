package com.example.elixir.recipe.ui.adapter

import android.annotation.SuppressLint
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.AdapterView
import androidx.fragment.app.FragmentManager
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import com.bumptech.glide.Glide
import com.example.elixir.R
import com.example.elixir.databinding.ItemRecipeHeaderRecommendBinding
import com.example.elixir.databinding.ItemRecipeHeaderSearchBinding
import com.example.elixir.databinding.ItemRecipeHeaderSpinnerBinding
import com.example.elixir.databinding.ItemRecipeListBinding
import com.example.elixir.ingredient.data.IngredientData
import com.example.elixir.recipe.data.RecipeItemData
import com.example.elixir.recipe.data.SearchItemData
import com.example.elixir.recipe.ui.fragment.RecipeDetailFragment
import com.example.elixir.recipe.viewmodel.RecipeViewModel
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.flexbox.JustifyContent


// 헤더: 검색창 홀더 정의
class HeaderSearchTextViewHolder(
    private val binding: ItemRecipeHeaderSearchBinding,
    private val onSearch: (String) -> Unit,
    private val onBack: (() -> Unit)?
) : RecipeViewHolder(binding.root) {

    fun bind(currentKeyword: String?) {
        // EditText에 현재 키워드 설정
        binding.etSearch.setText(currentKeyword ?: "")

        // EditText 입력 감지
        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {}
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                onSearch(s.toString())
            }
        })

        // 검색 버튼 클릭
        binding.searchButton.setOnClickListener {
            val keyword = binding.etSearch.text.toString().trim()
            onSearch(keyword)
        }

        // 키보드 검색 버튼
        binding.etSearch.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                val keyword = binding.etSearch.text.toString().trim()
                onSearch(keyword)
                true
            } else false
        }
        binding.btnBack.setOnClickListener { onBack?.invoke() }
    }
}

class HeaderSearchSpinnerViewHolder(private val binding: ItemRecipeHeaderSpinnerBinding,
                              private val onTypeSelected: (String?) -> Unit, private val onMethodSelected: (String?) -> Unit,
                              private val onResetClicked: () -> Unit) : RecipeViewHolder(binding.root) {

    fun bind(selectedType: String?, selectedMethod: String?, typeItems: List<String>, methodItems: List<String>) {
        // 레시피 종류 스피너
        val typeAdapter = RecipeListSpinnerAdapter(binding.root.context, typeItems)
        binding.spinnerType.adapter = typeAdapter
        binding.spinnerType.setSelection(typeItems.indexOf(selectedType ?: typeItems.first()))
        binding.spinnerType.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                val value = if (position == 0) null
                else {
                    if(parent.getItemAtPosition(position).toString() == "음료/차")
                        "음료_차"
                    else if(parent.getItemAtPosition(position).toString() == "양념/소스/잼")
                        "양념_소스_잼"
                    else
                        parent.getItemAtPosition(position).toString()
                }
                onTypeSelected(value)
            }
            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        // 저속노화 스피너
        val methodAdapter = RecipeListSpinnerAdapter(binding.root.context, methodItems)
        binding.spinnerDifficulty.adapter = methodAdapter
        binding.spinnerDifficulty.setSelection(methodItems.indexOf(selectedMethod ?: methodItems.first()))
        binding.spinnerDifficulty.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                val value = if (position == 0) null else parent.getItemAtPosition(position).toString()
                onMethodSelected(value)
            }
            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        // 리셋 버튼: 스피너 둘다 null이나 empty가 아닐 때만 버튼 보여주기
        val isMethodSelected = !selectedMethod.isNullOrEmpty()
        val isTypeSelected = !selectedType.isNullOrEmpty()
        binding.resetButton.visibility = if (isMethodSelected || isTypeSelected) View.VISIBLE else View.GONE

        // 리셋 버튼 클릭 리스너
        binding.resetButton.setOnClickListener { onResetClicked() }
    }
}

// 레시피 리스트 아이템 홀더: 아이템 정의 및 스크랩, 좋아요 정의
class SearchItemViewHolder(val binding: ItemRecipeListBinding, private val ingredientMap: Map<Int, IngredientData>,
                     private val onBookmarkClick: (RecipeItemData) -> Unit, private val onHeartClick: (RecipeItemData) -> Unit,
                     private val fragmentManager: FragmentManager
) : RecipeViewHolder(binding.root) {

    fun bind(item: RecipeItemData) {
        // 레시피 썸네일
        Glide.with(binding.root.context)
            .load(item.imageUrl)
            .placeholder(R.drawable.img_blank)
            .error(R.drawable.img_blank)
            .into(binding.recipePicture)

        // 카테고리 타입 명칭
        val categoryType = when (item.categoryType) {
            "양념_소스_잼" -> "양념/소스/잼"
            "음료_차" -> "음료/차"
            else -> item.categoryType
        }
        // 차례대로 레시피명, 저속노화, 종류, 난이도, 좋아요 갯수 정의
        binding.recipeNameText.text = item.title
        binding.categorySlowAging.text = item.categorySlowAging
        binding.categoryType.text = categoryType
        binding.recipeLevel.text = item.difficulty
        binding.heartCount.text = formatCount(item.likes)

        // 시간 정의 (시간, 분)
        val timeHours = item.totalTimeMinutes / 60
        if (timeHours > 0)
            binding.recipeTimeHour.text = "${timeHours}시간"
        binding.recipeTimeMin.text = "${item.totalTimeMinutes % 60}분"

        // 식재료 태그 정의
        binding.ingredientList.apply {
            layoutManager = FlexboxLayoutManager(context).apply {
                flexDirection = FlexDirection.ROW
                justifyContent = JustifyContent.FLEX_START
            }
            adapter = IngredientTagChipMapAdapter(
                item.ingredientTagIds ?: emptyList(),
                ingredientMap
            )
            visibility = if (item.ingredientTagIds.isNullOrEmpty()) View.GONE else View.VISIBLE
        }

        // 스크랩 버튼
        binding.bookmarkButton.setBackgroundResource(
            if (item.scrappedByCurrentUser) R.drawable.ic_recipe_bookmark_selected
            else R.drawable.ic_recipe_bookmark_normal
        )
        binding.bookmarkButton.setOnClickListener {
            onBookmarkClick(item)
            // notifyItemChanged는 Adapter에서 처리
        }

        // 좋아요 버튼
        binding.heartButton.setBackgroundResource(
            if (item.likedByCurrentUser) R.drawable.ic_recipe_heart_selected
            else R.drawable.ic_recipe_heart_normal
        )
        binding.heartButton.setOnClickListener {
            onHeartClick(item)
            binding.heartCount.text = formatCount(item.likes)
            // notifyItemChanged는 Adapter에서 처리
        }

        // 아이템 클릭: 상세 페이지로 이동
        binding.root.setOnClickListener {
            val detailFragment = RecipeDetailFragment().apply {
                arguments = Bundle().apply {
                    putInt("recipeId", item.id)
                }
            }
            fragmentManager.beginTransaction()
                .replace(R.id.flContainer, detailFragment)
                .addToBackStack(null)
                .commit()
        }
    }
    // 좋아요 갯수 수치화(k, m)
    @SuppressLint("DefaultLocale")
    private fun formatCount(count: Int): String {
        return when {
            count >= 1_000_000 -> String.format("%.1fM", count / 1_000_000.0)
            count >= 1_000 -> String.format("%.1fk", count / 1_000.0)
            else -> count.toString()
        }.removeSuffix(".0")
    }
}

class SearchListAdapter(
    private var ingredientMap: Map<Int, IngredientData>,
    private val onBookmarkClick: (RecipeItemData) -> Unit,
    private val onHeartClick: (RecipeItemData) -> Unit,
    private val fragmentManager: FragmentManager,
    private val recipeViewModel: RecipeViewModel,
    private val onSearchKeywordChanged: (String) -> Unit,
    private val onTypeSelected: (String?) -> Unit,
    private val onMethodSelected: (String?) -> Unit,
    private val onResetClicked: () -> Unit,
    private val onBack: (() -> Unit)? = null
) : PagingDataAdapter<SearchItemData, RecipeViewHolder>(SearchDiffCallback()) {

    enum class SearchViewType(val value: Int) {
        SEARCH_TEXT(0),     // 검색창 헤더
        SEARCH_SPINNER(1),  // 검색 스피너 헤더
        ITEM(2)             // 레시피 아이템
    }

    // 스피너 값, 검색어 등
    private var selectedType: String? = null
    private var selectedSlowAging: String? = null
    var typeItems: List<String> = emptyList()
    var methodItems: List<String> = emptyList()
    var currentKeyword: String? = null

    override fun getItemViewType(position: Int): Int = when (getItem(position)) {
        is SearchItemData.SearchTextHeader -> SearchViewType.SEARCH_TEXT.value
        is SearchItemData.SearchSpinnerHeader -> SearchViewType.SEARCH_SPINNER.value
        is SearchItemData.SearchItem -> SearchViewType.ITEM.value
        else -> SearchViewType.ITEM.value
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecipeViewHolder {
        return when (viewType) {
            SearchViewType.SEARCH_TEXT.value -> {
                val binding = ItemRecipeHeaderSearchBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                HeaderSearchTextViewHolder(binding, onSearchKeywordChanged, onBack)
            }
            SearchViewType.SEARCH_SPINNER.value -> {
                val binding = ItemRecipeHeaderSpinnerBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                HeaderSearchSpinnerViewHolder(binding, onTypeSelected, onMethodSelected, onResetClicked)
            }
            else -> {
                val binding = ItemRecipeListBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                SearchItemViewHolder(binding, ingredientMap, onBookmarkClick, onHeartClick, fragmentManager)
            }
        }
    }

    override fun onBindViewHolder(holder: RecipeViewHolder, position: Int) {
        val item = getItem(position) ?: return
        when (item) {
            is SearchItemData.SearchTextHeader -> (holder as HeaderSearchTextViewHolder).bind(currentKeyword)
            is SearchItemData.SearchSpinnerHeader -> (holder as HeaderSearchSpinnerViewHolder).bind(
                selectedType, selectedSlowAging, typeItems, methodItems
            )
            is SearchItemData.SearchItem -> (holder as SearchItemViewHolder).bind(item.data)
        }
    }

    fun updateSearchHeader(
        selectedType: String?, selectedMethod: String?,
        typeItems: List<String>, methodItems: List<String>, keyword: String?
    ) {
        this.selectedType = selectedType
        this.selectedSlowAging = selectedMethod
        this.typeItems = typeItems
        this.methodItems = methodItems
        this.currentKeyword = keyword
        notifyItemChanged(0) // SearchTextHeader 위치
        notifyItemChanged(1) // SearchSpinnerHeader 위치
    }

    fun updateIngredientMap(newIngredientMap: Map<Int, IngredientData>) {
        ingredientMap = newIngredientMap
        notifyDataSetChanged()
    }
}


class SearchDiffCallback : DiffUtil.ItemCallback<SearchItemData>() {
    override fun areItemsTheSame(oldItem: SearchItemData, newItem: SearchItemData): Boolean {
        if (oldItem::class != newItem::class) return false
        return when (oldItem) {
            is SearchItemData.SearchTextHeader -> true
            is SearchItemData.SearchSpinnerHeader -> true
            is SearchItemData.SearchItem ->
                newItem is SearchItemData.SearchItem && oldItem.data.id == newItem.data.id
        }
    }
    override fun areContentsTheSame(oldItem: SearchItemData, newItem: SearchItemData): Boolean {
        if (oldItem::class != newItem::class) return false
        return when (oldItem) {
            is SearchItemData.SearchTextHeader -> true
            is SearchItemData.SearchSpinnerHeader -> true
            is SearchItemData.SearchItem ->
                newItem is SearchItemData.SearchItem && oldItem.data == newItem.data
        }
    }
}
