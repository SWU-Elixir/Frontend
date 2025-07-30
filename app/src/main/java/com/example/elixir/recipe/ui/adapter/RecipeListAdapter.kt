package com.example.elixir.recipe.ui.adapter

import android.annotation.SuppressLint
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.AdapterView
import androidx.fragment.app.FragmentManager
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.elixir.R
import com.example.elixir.databinding.ItemRecipeHeaderRecommendBinding
import com.example.elixir.databinding.ItemRecipeHeaderSearchBinding
import com.example.elixir.databinding.ItemRecipeHeaderSpinnerBinding
import com.example.elixir.databinding.ItemRecipeListBinding
import com.example.elixir.ingredient.data.IngredientData
import com.example.elixir.recipe.data.RecipeItemData
import com.example.elixir.recipe.data.RecipeListItemData
import com.example.elixir.recipe.ui.fragment.RecipeDetailFragment
import com.example.elixir.recipe.viewmodel.RecipeViewModel
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.flexbox.JustifyContent

/**
 * 레시피 리스트 화면에서 사용되는 RecyclerView 어댑터
 * recipeList: 레시피 목록 데이터
 * onBookmarkClick: 북마크 버튼 클릭 시 동작
 * onHeartClick: 좋아요(하트) 버튼 클릭 시 동작
 */

abstract class RecipeViewHolder(view: View) : RecyclerView.ViewHolder(view)

class HeaderRecommendViewHolder(
    private val binding: ItemRecipeHeaderRecommendBinding,
    private val fragmentManager: FragmentManager,
    private val recipeViewModel: RecipeViewModel,
    private val onSearchClicked: () -> Unit
) : RecipeViewHolder(binding.root) {
    private var recommendAdapter: RecipeRecommendationListAdapter? = null

    // 추천 레시피 데이터와 식재료 맵을 받아서 뷰 갱신
    fun bind(
        recommendList: List<RecipeItemData>?,
        ingredientDataMap: Map<Int, IngredientData>?
    ) {
        // 1. ViewPager2 어댑터 세팅 또는 데이터 갱신
        if (recommendAdapter == null) {
            recommendAdapter = RecipeRecommendationListAdapter(
                recommendList ?: emptyList(),
                fragmentManager,
                recipeViewModel,
                ingredientDataMap
            )
            binding.recommendationList.adapter = recommendAdapter
        } else {
            recommendAdapter?.updateData(recommendList ?: emptyList())
            recommendAdapter?.updateIngredientMap(ingredientDataMap ?: emptyMap())
        }

        // 2. ViewPager2 페이지 전환 애니메이션
        binding.recommendationList.setPageTransformer { page, position ->
            val absPos = kotlin.math.abs(position)
            page.scaleY = 0.85f + (1 - absPos) * 0.15f
            page.scaleX = 0.85f + (1 - absPos) * 0.15f
            page.translationX = -position * 40
        }

        // 3. DotsIndicator 연결
        binding.indicator.attachTo(binding.recommendationList)

        // 4. 검색 버튼 클릭 이벤트
        binding.searchButton.setOnClickListener {
            onSearchClicked()
        }
    }
}

class HeaderSpinnerViewHolder(
    private val binding: ItemRecipeHeaderSpinnerBinding, private val onTypeSelected: (String?) -> Unit,
    private val onMethodSelected: (String?) -> Unit, private val onResetClicked: () -> Unit
) : RecipeViewHolder(binding.root) {

    fun bind(selectedType: String?, selectedSlowAging: String?, typeItems: List<String>, slowAgingItems: List<String>) {
        // 레시피 종류 스피너
        val typeAdapter = RecipeListSpinnerAdapter(binding.root.context, typeItems)
        binding.spinnerType.adapter = typeAdapter

        val typeIndex = typeItems.indexOf(selectedType)
        val validTypeIndex = if (typeIndex >= 0) typeIndex else 0

        typeAdapter.setSelectedPosition(validTypeIndex)
        binding.spinnerType.setSelection(validTypeIndex, false)

        binding.spinnerType.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                val value =
                    if(parent.getItemAtPosition(position).toString() == "음료/차")
                        "음료_차"
                    else if(parent.getItemAtPosition(position).toString() == "양념/소스/잼")
                        "양념_소스_잼"
                    else if(parent.getItemAtPosition(position).toString() == "종류")
                        null
                    else
                        parent.getItemAtPosition(position).toString()
                onTypeSelected(value)
                updateResetButtonVisibility()
            }
            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        // 저속노화 스피너
        val slowAgingAdapter = RecipeListSpinnerAdapter(binding.root.context, slowAgingItems)
        binding.spinnerDifficulty.adapter = slowAgingAdapter

        val slowAgingIndex = slowAgingItems.indexOf(selectedSlowAging)
        val validSlowAgingIndex = if (slowAgingIndex >= 0) slowAgingIndex else 0

        slowAgingAdapter.setSelectedPosition(validSlowAgingIndex)
        binding.spinnerDifficulty.setSelection(validSlowAgingIndex, false)

        binding.spinnerDifficulty.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                val value =
                    if (parent.getItemAtPosition(position).toString() == "저속노화")
                        null
                    else
                        parent.getItemAtPosition(position).toString()
                onMethodSelected(value)
                updateResetButtonVisibility()
            }
            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        // 리셋 버튼 클릭 리스너
        binding.resetButton.setOnClickListener {
            // UI 초기화
            binding.spinnerType.setSelection(0, false) // "종류"
            binding.spinnerDifficulty.setSelection(0, false) // "저속노화"
            onResetClicked()

            // 리셋 버튼 숨기기
            binding.resetButton.visibility = View.GONE
        }
    }

    private fun updateResetButtonVisibility() {
        val typeSelected = binding.spinnerType.selectedItem?.toString()?.let {
            it != "종류" && it.isNotBlank()
        } ?: false

        val slowAgingSelected = binding.spinnerDifficulty.selectedItem?.toString()?.let {
            it != "저속노화" && it.isNotBlank()
        } ?: false

        binding.resetButton.visibility = if (typeSelected || slowAgingSelected) View.VISIBLE else View.GONE
    }
}

// 레시피 리스트 아이템 홀더: 아이템 정의 및 스크랩, 좋아요 정의
class ItemViewHolder(val binding: ItemRecipeListBinding,
    private val onBookmarkClick: (RecipeItemData) -> Unit, private val onHeartClick: (RecipeItemData) -> Unit,
    private val fragmentManager: FragmentManager) : RecipeViewHolder(binding.root) {

    fun bind(item: RecipeItemData, ingredientMap: Map<Int, IngredientData>) {
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
        else
            binding.recipeTimeHour.visibility = View.GONE
        binding.recipeTimeMin.text = "${item.totalTimeMinutes % 60}분"

        // 식재료 태그 정의
        binding.ingredientList.apply {
            layoutManager = FlexboxLayoutManager(context).apply {
                flexDirection = FlexDirection.ROW
                justifyContent = JustifyContent.FLEX_START
            }
            binding.ingredientList.adapter = IngredientTagChipMapAdapter(
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

class RecipeListAdapter(
    private var ingredientMap: Map<Int, IngredientData>,
    private val onBookmarkClick: (RecipeItemData) -> Unit,
    private val onHeartClick: (RecipeItemData) -> Unit,
    private val fragmentManager: FragmentManager,
    private val recipeViewModel: RecipeViewModel,
    private val onSearchClicked: () -> Unit,
    private val onTypeSelected: (String?) -> Unit,
    private val onMethodSelected: (String?) -> Unit,
    private val onResetClicked: () -> Unit
) : PagingDataAdapter<RecipeListItemData, RecipeViewHolder>(RecipeDiffCallback()) {
    // 리사이클러뷰 모드 지정
    enum class RecipeViewType(val value: Int) {
        RECOMMEND(0),       // 추천 레시피 헤더 (스크롤 시 사라지게)
        SEARCH_SPINNER(1),  // 검색 스피너 헤더 (스크롤 시 상단에 붙어있게)
        ITEM(2)             // 레시피 아이템
    }

    override fun getItemViewType(position: Int): Int {
        val item = getItem(position) ?: return RecipeViewType.ITEM.value

        return when (item) {
            is RecipeListItemData.RecommendHeader -> RecipeViewType.RECOMMEND.value
            is RecipeListItemData.SearchSpinnerHeader -> RecipeViewType.SEARCH_SPINNER.value
            is RecipeListItemData.RecipeItem -> RecipeViewType.ITEM.value
        }
    }

    // 추천, 스티키헤더, 아이템 데이터
    var recommendRecipeList: List<RecipeItemData>? = null
    private var ingredientDataMap: Map<Int, IngredientData>? = null

    // 검색 스피너 값 정의
    private var selectedType: String? = null
    private var selectedSlowAging: String? = null
    var typeItems: List<String> = emptyList()
    var methodItems: List<String> = emptyList()

    // 검색 헤더 정보 갱신
    fun updateSearchHeader(selectedType: String?, selectedMethod: String?,
        typeItems: List<String>, methodItems: List<String> ) {
        this.selectedType = selectedType
        this.selectedSlowAging = selectedMethod
        this.typeItems = typeItems
        this.methodItems = methodItems
        notifyItemChanged(1) // SearchHeader가 1번 인덱스에 있다고 가정
    }

    // 바인딩 정의
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecipeViewHolder {
        return when (viewType) {
            // 헤더: 추천 레시피 바인딩
            RecipeViewType.RECOMMEND.value -> {
                val binding = ItemRecipeHeaderRecommendBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                HeaderRecommendViewHolder(binding, fragmentManager, recipeViewModel, onSearchClicked)
            }
            // 헤더: 검색 스피너 바인딩
            RecipeViewType.SEARCH_SPINNER.value -> {
                val binding = ItemRecipeHeaderSpinnerBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                HeaderSpinnerViewHolder(binding, onTypeSelected, onMethodSelected, onResetClicked)
            }
            // 바디: 레시피 리스트 아이템 바인딩
            else -> {
                val binding = ItemRecipeListBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                ItemViewHolder(binding, onBookmarkClick, onHeartClick, fragmentManager)
            }
        }
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: RecipeViewHolder, position: Int) {
        val item = getItem(position) ?: return
        Log.d("RecipeListAdapter", "Binding position: $position, holder: ${holder::class.simpleName}, item: ${item.let { it::class.simpleName }}")

        // ViewHolder 타입과 아이템 타입을 모두 확인하여 안전하게 캐스팅
        when {
            holder is HeaderRecommendViewHolder && item is RecipeListItemData.RecommendHeader -> {
                holder.bind(recommendRecipeList, ingredientDataMap)
            }
            holder is HeaderSpinnerViewHolder && item is RecipeListItemData.SearchSpinnerHeader -> {
                holder.bind(selectedType, selectedSlowAging, typeItems, methodItems)
            }
            holder is ItemViewHolder && item is RecipeListItemData.RecipeItem -> {
                holder.bind(item.data, ingredientMap)
            }
            else -> {
                // 예상치 못한 조합일 경우 로그를 출력하고 처리하지 않음
                Log.e("RecipeListAdapter", "Mismatched ViewHolder and Item types: " +
                        "holder=${holder::class.simpleName}, item=${item::class.simpleName}, position=$position")
            }
        }
    }

    fun updateIngredientMap(newIngredientMap: Map<Int, IngredientData>) {
        ingredientMap = newIngredientMap
        // 모든 아이템에 영향을 줄 수 있으니, 전체 갱신
        notifyDataSetChanged() // 단순화를 위해, 실제로는 DiffUtil이 알아서 처리하므로 필요 없을 수 있음
    }
}

class RecipeDiffCallback : DiffUtil.ItemCallback<RecipeListItemData>() {
    override fun areItemsTheSame(oldItem: RecipeListItemData, newItem: RecipeListItemData): Boolean {
        if (oldItem::class != newItem::class) return false
        return when (oldItem) {
            is RecipeListItemData.RecommendHeader -> true
            is RecipeListItemData.SearchSpinnerHeader -> true
            is RecipeListItemData.RecipeItem ->
                newItem is RecipeListItemData.RecipeItem && oldItem.data.id == newItem.data.id
        }
    }
    override fun areContentsTheSame(oldItem: RecipeListItemData, newItem: RecipeListItemData): Boolean {
        if (oldItem::class != newItem::class) return false
        return when (oldItem) {
            is RecipeListItemData.RecommendHeader -> true
            is RecipeListItemData.SearchSpinnerHeader -> true
            is RecipeListItemData.RecipeItem ->
                newItem is RecipeListItemData.RecipeItem && oldItem.data == newItem.data
        }
    }
}