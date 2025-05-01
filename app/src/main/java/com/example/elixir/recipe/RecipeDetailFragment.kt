package com.example.elixir.recipe

import android.app.Dialog
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
import java.text.SimpleDateFormat
import java.util.*

class RecipeDetailFragment : Fragment() {

    private var _binding: FragmentRecipeDetailBinding? = null
    private val binding get() = _binding!!
    private lateinit var commentAdapter: RecipeCommentAdapter
    private val comments = mutableListOf<CommentData>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentRecipeDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // ------------------------ 1. 더미 데이터 적용 ------------------------
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
            likeCount = 42
        )

        // ------------------------ 2. 데이터 바인딩 ------------------------
        binding.recipeNameText.text = dummyRecipe.title
        binding.categorySlowAging.text = dummyRecipe.categorySlowAging
        binding.categoryType.text = dummyRecipe.categoryType
        binding.recipeLevel.text = dummyRecipe.difficulty
        binding.recipeTimeHour.text = if (dummyRecipe.timeHours > 0) "${dummyRecipe.timeHours}시간" else ""
        binding.recipeTimeMin.text = "${dummyRecipe.timeMinutes}분"
        binding.tipText.text = dummyRecipe.tips
        binding.heartCount.text = formatCount(dummyRecipe.likeCount)

        // 초기 버튼 상태 설정
        binding.bookmarkButton.setBackgroundResource(
            if(dummyRecipe.isBookmarked) R.drawable.ic_recipe_bookmark_selected
            else R.drawable.ic_recipe_bookmark_normal
        )
        binding.heartButton.setBackgroundResource(
            if(dummyRecipe.isLiked) R.drawable.ic_recipe_heart_selected
            else R.drawable.ic_recipe_heart_normal
        )

        // 북마크/좋아요 버튼 이미지 설정
        binding.bookmarkButton.setOnClickListener{
            dummyRecipe.isBookmarked = !dummyRecipe.isBookmarked
            binding.bookmarkButton.setBackgroundResource(
                if(dummyRecipe.isBookmarked) R.drawable.ic_recipe_bookmark_selected
                else R.drawable.ic_recipe_bookmark_normal
            )
        }

        binding.heartButton.setOnClickListener{
            dummyRecipe.isLiked = !dummyRecipe.isLiked
            if (dummyRecipe.isLiked) {
                dummyRecipe.likeCount++
            } else {
                dummyRecipe.likeCount--
            }
            binding.heartButton.setBackgroundResource(
                if(dummyRecipe.isLiked) R.drawable.ic_recipe_heart_selected
                else R.drawable.ic_recipe_heart_normal
            )
            binding.heartCount.text = formatCount(dummyRecipe.likeCount)
        }

        // 팔로우 버튼 클릭 이벤트 설정
        binding.followButton.setOnClickListener {
            val isFollowing = binding.followButton.text == "팔로잉"
            binding.followButton.text = if (isFollowing) "팔로우" else "팔로잉"
            binding.followButton.setBackgroundResource(
                if (isFollowing) R.drawable.bg_rect_filled_orange
                else R.drawable.bg_rect_outline_gray
            )
            binding.followButton.setTextColor(
                resources.getColor(
                    if (isFollowing) R.color.white
                    else R.color.black,
                    null
                )
            )
        }

        // 메뉴 버튼 클릭 시 팝업 메뉴 표시
        binding.menuButton.setOnClickListener {
            val popupMenu = PopupMenu(context, binding.menuButton)
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

        // ------------------------ 3. 리스트 ------------------------
        binding.tagList.apply {
            layoutManager = FlexboxLayoutManager(context).apply {
                flexDirection = FlexDirection.ROW
                justifyContent = JustifyContent.FLEX_START
            }
            adapter = RecipeTagAdapter(dummyRecipe.ingredients)
        }

        binding.ingredientsList.apply {
            layoutManager = FlexboxLayoutManager(context).apply {
                flexDirection = FlexDirection.ROW
                justifyContent = JustifyContent.FLEX_START
            }
            adapter = RecipeSeasoningAdapter(dummyRecipe.ingredients)
        }

        binding.seasoningList.apply {
            layoutManager = FlexboxLayoutManager(context).apply {
                flexDirection = FlexDirection.ROW
                justifyContent = JustifyContent.FLEX_START
            }
            adapter = RecipeSeasoningAdapter(dummyRecipe.seasoning)
        }

        binding.stepList.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        binding.stepList.adapter = RecipeStepAdapter(dummyRecipe.recipeOrder)

        // ------------------------ 더미 댓글 데이터 ------------------------
        comments.addAll(listOf(
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
        ))

        // ------------------------ 댓글 어댑터 연결 ------------------------
        binding.commentList.layoutManager = LinearLayoutManager(requireContext())
        commentAdapter = RecipeCommentAdapter(requireContext(), comments)
        binding.commentList.adapter = commentAdapter

        // 댓글 작성 버튼 클릭 이벤트
        binding.commentButton.setOnClickListener {
            val commentText = binding.editComment.text.toString()
            if (commentText.isNotEmpty()) {
                val currentTime = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date())
                val newComment = CommentData(
                    profileImageResId = R.drawable.ic_profile,
                    memberTitle = "칭호",
                    memberNickname = "user123",
                    commentText = commentText,
                    date = currentTime
                )
                comments.add(newComment)
                commentAdapter.notifyItemInserted(comments.size - 1)
                binding.commentList.smoothScrollToPosition(comments.size - 1)
                binding.editComment.text.clear()
            } else {
                Toast.makeText(context, "댓글을 입력해주세요", Toast.LENGTH_SHORT).show()
            }
        }

        // ------------------------ 4. 뒤로 가기 버튼 ------------------------
        binding.backButton.setOnClickListener {
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
