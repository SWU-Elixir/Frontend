package com.example.elixir.recipe

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.elixir.R
import com.example.elixir.databinding.FragmentRecipeDetailBinding
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.flexbox.JustifyContent
import java.math.BigInteger

class RecipeDetailFragment : Fragment() {

    private var _binding: FragmentRecipeDetailBinding? = null
    private val binding get() = _binding!!

    // 상단 뒤로가기 버튼
    private lateinit var backButton: ImageButton

    // 유저 정보
    private lateinit var profileImage: ImageView
    private lateinit var memberTitle: TextView
    private lateinit var memberNickname: TextView
    private lateinit var followButton: Button

    // 레시피 정보
    private lateinit var recipeTitle: TextView
    private lateinit var categorySlowAging: TextView
    private lateinit var categoryType: TextView
    private lateinit var tagList: RecyclerView
    private lateinit var bookmarkButton: ImageButton
    private lateinit var heartButton: ImageButton
    private lateinit var menuButton: ImageButton
    private lateinit var heartCount: TextView
    private lateinit var recipeLevel: TextView
    private lateinit var recipeTimeHour: TextView
    private lateinit var recipeTimeMin: TextView
    private lateinit var ingredientsList: RecyclerView
    private lateinit var seasoningList: RecyclerView
    private lateinit var stepList:RecyclerView
    private lateinit var tipText: TextView
    private lateinit var commentList: RecyclerView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentRecipeDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // ------------------------ 1. 뷰 초기화 ------------------------
        backButton = binding.backButton
        profileImage = binding.profileImage
        memberTitle = binding.memberTitle
        memberNickname = binding.memberNickname
        followButton = binding.followButton

        recipeTitle = binding.recipeNameText
        categorySlowAging = binding.categorySlowAging
        categoryType = binding.categoryType
        tagList = binding.tagList
        bookmarkButton = binding.bookmarkButton
        heartButton = binding.heartButton
        menuButton = binding.menuButton
        heartCount = binding.heartCount
        recipeLevel = binding.recipeLevel
        recipeTimeHour = binding.recipeTimeHour
        recipeTimeMin = binding.recipeTimeMin
        stepList = binding.stepList
        ingredientsList = binding.ingredientsList
        seasoningList = binding.seasoningList
        tipText = binding.tipText
        commentList = binding.commentList

        // ------------------------ 2. 더미 데이터 적용 ------------------------
        val dummyRecipe = RecipeData(
            id = BigInteger.valueOf(1),
            memberId = BigInteger.valueOf(1001),
            title = "케일 항염 그린 스무디",
            imageUrl = R.drawable.png_recipe_sample,
            categorySlowAging = "염증 감소",
            categoryType = "음료/차",
            difficulty = "쉬움",
            timeHours = 0,
            timeMinutes = 3,
            ingredients = listOf("케일", "바나나", "아몬드밀크", "아몬드밀크", "아몬드밀크", "아몬드밀크"),
            seasoning = listOf("꿀", "얼음", "얼음", "얼음", "얼음", "얼음", "얼음"),
            recipeOrder = listOf("재료를 믹서에 넣는다", "곱게 갈아 컵에 담는다"),
            tips = "단맛이 부족하면 꿀 대신 대추즙도 좋아요.",
            createdAt = "2025-04-25",
            updateAt = "2025-04-25",
            isBookmarked = true,
            isLiked = true,
            likeCount = 125
        )

        // ------------------------ 3. 데이터 바인딩 ------------------------
        recipeTitle.text = dummyRecipe.title
        categorySlowAging.text = dummyRecipe.categorySlowAging
        categoryType.text = dummyRecipe.categoryType
        recipeLevel.text = dummyRecipe.difficulty
        recipeTimeHour.text = if (dummyRecipe.timeHours > 0) "${dummyRecipe.timeHours}시간" else ""
        recipeTimeMin.text = "${dummyRecipe.timeMinutes}분"
        tipText.text = dummyRecipe.tips

        // 북마크/좋아요 버튼 이미지 설정
        bookmarkButton.setImageResource(
            if (dummyRecipe.isBookmarked) R.drawable.ic_recipe_bookmark_selected
            else R.drawable.ic_recipe_bookmark_normal
        )
        heartButton.setImageResource(
            if (dummyRecipe.isLiked) R.drawable.ic_recipe_heart_selected
            else R.drawable.ic_recipe_heart_normal
        )

        // 좋아요 수 포맷 처리
        heartCount.text = formatCount(dummyRecipe.likeCount)

        // 팔로우 버튼 클릭 이벤트 설정
        binding.followButton.setOnClickListener {
            val isFollowing = binding.followButton.text == "팔로잉"
            binding.followButton.text = if (isFollowing) "팔로우" else "팔로잉"
            binding.followButton.setBackgroundResource(
                if (isFollowing) R.drawable.bg_rect_outline_gray_10
                else R.drawable.bg_rect_filled_orange_5
            )
            binding.followButton.setTextColor(
                resources.getColor(
                    if (isFollowing) R.color.black
                    else R.color.white,
                    null
                )
            )
        }

        // 메뉴 버튼 클릭 시 팝업 메뉴 표시
        menuButton.setOnClickListener {
            val popupMenu = PopupMenu(context, menuButton)
            popupMenu.menuInflater.inflate(R.menu.item_menu_drop, popupMenu.menu)

            popupMenu.setOnMenuItemClickListener { menuItem ->
                when (menuItem.itemId) {
                    R.id.menu_edit -> {
                        Toast.makeText(context, "댓글 수정 클릭됨", Toast.LENGTH_SHORT).show()
                        true
                    }
                    R.id.menu_delete -> {
                        Toast.makeText(context, "댓글 삭제 클릭됨", Toast.LENGTH_SHORT).show()
                        true
                    }
                    else -> false
                }
            }
            popupMenu.show()
        }

        // ------------------------ 4. 리스트 ------------------------
        tagList.apply {
            layoutManager = FlexboxLayoutManager(context).apply {
                flexDirection = FlexDirection.ROW
                justifyContent = JustifyContent.FLEX_START
            }
            adapter = RecipeTagAdapter(dummyRecipe.ingredients)
        }

        ingredientsList.apply {
            layoutManager = FlexboxLayoutManager(context).apply {
                flexDirection = FlexDirection.ROW
                justifyContent = JustifyContent.FLEX_START
            }
            adapter = RecipeSeasoningAdapter(dummyRecipe.ingredients)
        }

        seasoningList.apply {
            layoutManager = FlexboxLayoutManager(context).apply {
                flexDirection = FlexDirection.ROW
                justifyContent = JustifyContent.FLEX_START
            }
            adapter = RecipeSeasoningAdapter(dummyRecipe.seasoning)
        }

        stepList.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        stepList.adapter = RecipeStepAdapter(dummyRecipe.recipeOrder)

        // ------------------------ 더미 댓글 데이터 ------------------------
        val dummyComments = listOf(
            CommentData(
                profileImageResId = R.drawable.ic_profile,
                memberTitle = "건강지기",
                memberNickname = "wellness_lover",
                commentText = "이 레시피 정말 맛있고 건강해요!",
                date = "2025-05-01 12:13"
            ),
            CommentData(
                profileImageResId = R.drawable.ic_profile,
                memberTitle = "슬로우에이징러",
                memberNickname = "슬로우",
                commentText = "아보카도 좋아하는데 해봐야겠어요~",
                date = "2025-05-01 12:13"
            )
        )

        // ------------------------ 댓글 어댑터 연결 ------------------------
        commentList.layoutManager = LinearLayoutManager(requireContext())
        commentList.adapter = RecipeCommentAdapter(requireContext(), dummyComments)

        // ------------------------ 5. 뒤로 가기 버튼 ------------------------
        backButton.setOnClickListener {
            parentFragmentManager.popBackStack()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    /**
     * 좋아요 수를 보기 좋은 포맷으로 변환 (예: 1.2k, 2M)
     */
    private fun formatCount(count: Int): String {
        return when {
            count >= 1_000_000 -> String.format("%.1fM", count / 1_000_000.0)
            count >= 1_000     -> String.format("%.1fk", count / 1_000.0)
            else               -> count.toString()
        }.removeSuffix(".0") // 소수점 0 제거
    }
}
