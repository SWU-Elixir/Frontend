package com.example.elixir.recipe.ui

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.elixir.R
import com.example.elixir.ToolbarActivity
import com.example.elixir.databinding.FragmentRecipeDetailBinding
import com.example.elixir.recipe.data.CommentData
import com.example.elixir.recipe.data.FlavoringData
import com.example.elixir.recipe.data.RecipeData
import com.example.elixir.recipe.data.RecipeStepData
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.flexbox.JustifyContent
import com.google.gson.Gson
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

/**
 * 레시피 상세 정보를 표시하는 프래그먼트
 * 레시피의 기본 정보, 재료, 조리 순서, 댓글 등을 보여주고 관리
 */
class RecipeDetailFragment : Fragment(), CommentActionListener {

    // ViewBinding 관련 변수
    private var _binding: FragmentRecipeDetailBinding? = null
    private val binding get() = _binding!!

    // 댓글 관련 변수
    private lateinit var commentAdapter: RecipeCommentAdapter
    private val comments = mutableListOf<CommentData>()
    private var editingCommentId: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRecipeDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // ------------------------ 데이터 적용 ------------------------
        // arguments에서 JSON 문자열 꺼내서 객체로 변환
        val recipeDataJson = arguments?.getString("recipeDataJson")
        val recipeData = recipeDataJson?.let { Gson().fromJson(it, RecipeData::class.java) }

        // ------------------------ 데이터 바인딩 ------------------------
        // 레시피 기본 정보 설정
        binding.recipeNameText.text = recipeData?.title
        binding.categorySlowAging.text = recipeData?.categorySlowAging
        binding.categoryType.text = recipeData?.categoryType
        binding.recipeLevel.text = recipeData?.difficulty
        binding.recipeImage.setImageURI(Uri.parse(recipeData?.imageUrl))

        // 순서
        // 조리순서 데이터 변환
        val stepList = recipeData!!.stepDescriptions.zip(recipeData.stepImageUrls) { desc, imgUrl ->
            RecipeStepData(stepDescription = desc, stepImg = imgUrl)
        }

        // 조리순서 RecyclerView에 어댑터 연결
        binding.stepList.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        binding.stepList.adapter = RecipeStepAdapter(stepList)

        // 조리 시간 설정 (시간이 있는 경우에만 표시)
        binding.recipeTimeHour.text = if (recipeData.timeHours > 0) getString(R.string.recipe_time_hour_format, recipeData.timeHours) else ""
        binding.recipeTimeMin.text = getString(R.string.recipe_time_min_format, recipeData.timeMinutes)

        // 팁과 좋아요 수 설정
        binding.tipText.text = recipeData.tips
        binding.heartCount.text = formatCount(recipeData.likes)

        // 북마크/좋아요 버튼 초기 상태 설정
        binding.bookmarkButton.setBackgroundResource(
            if(recipeData.scrappedByCurrentUser) R.drawable.ic_recipe_bookmark_selected
            else R.drawable.ic_recipe_bookmark_normal
        )

        binding.heartButton.setBackgroundResource(
            if(recipeData.likedByCurrentUser) R.drawable.ic_recipe_heart_selected
            else R.drawable.ic_recipe_heart_normal
        )

        // 북마크 버튼 클릭 이벤트 처리
        binding.bookmarkButton.setOnClickListener{
            recipeData.scrappedByCurrentUser = !recipeData.scrappedByCurrentUser
            binding.bookmarkButton.setBackgroundResource(
                if(recipeData.scrappedByCurrentUser) R.drawable.ic_recipe_bookmark_selected
                else R.drawable.ic_recipe_bookmark_normal
            )
        }

        // 좋아요 버튼 클릭 이벤트 처리
        binding.heartButton.setOnClickListener{
            recipeData.likedByCurrentUser = !recipeData.likedByCurrentUser
            // 좋아요 상태에 따라 카운트 증가/감소
            if (recipeData.likedByCurrentUser) {
                recipeData.likes++
            } else {
                recipeData.likes--
            }
            // 버튼 이미지와 카운트 업데이트
            binding.heartButton.setBackgroundResource(
                if(recipeData.likedByCurrentUser) R.drawable.ic_recipe_heart_selected
                else R.drawable.ic_recipe_heart_normal
            )
            binding.heartCount.text = formatCount(recipeData.likes)
        }

        // 팔로우 버튼 클릭 이벤트 처리
        binding.followButton.setOnClickListener {
            val isFollowing = binding.followButton.text == getString(R.string.following)
            // 팔로우 상태 토글 및 UI 업데이트
            binding.followButton.text = if (isFollowing) getString(R.string.follow) else getString(R.string.following)
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

        // 수정 런처 설정
        val editRecipeLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val recipeDataJson = result.data?.getStringExtra("recipeData")
                val recipeData = recipeDataJson?.let { Gson().fromJson(it, RecipeData::class.java) }
                if (recipeData != null) {
                    // 레시피 데이터 업데이트
                    binding.memberTitle.text = recipeData.authorTitle
                    binding.memberNickname.text = recipeData.authorNickname
                    binding.recipeNameText.text = recipeData.title
                    val imagePath = recipeData.imageUrl ?: ""
                    if (imagePath.startsWith("/")) {
                        binding.recipeImage.setImageURI(Uri.fromFile(File(imagePath)))
                    } else {
                        binding.recipeImage.setImageURI(Uri.parse(imagePath))
                    }
                    binding.categorySlowAging.text = recipeData.categorySlowAging
                    binding.categoryType.text = recipeData.categoryType
                    binding.recipeLevel.text = recipeData.difficulty
                    binding.recipeTimeHour.text =
                        if (recipeData.timeHours > 0)
                            getString(R.string.recipe_time_hour_format, recipeData.timeHours)
                        else
                            ""
                    binding.recipeTimeMin.text =
                        if(recipeData.timeMinutes > 0)
                            getString(R.string.recipe_time_min_format, recipeData.timeMinutes)
                        else
                            ""
                    binding.tipText.text = recipeData.tips
                    binding.heartCount.text = formatCount(recipeData.likes)
                    binding.bookmarkButton.setBackgroundResource(
                        if (recipeData.scrappedByCurrentUser) R.drawable.ic_recipe_bookmark_selected
                        else R.drawable.ic_recipe_bookmark_normal
                    )
                    binding.heartButton.setBackgroundResource(
                        if (recipeData.likedByCurrentUser) R.drawable.ic_recipe_heart_selected
                        else R.drawable.ic_recipe_heart_normal
                    )
                }
            }
        }

        // 메뉴 버튼 클릭 시 팝업 메뉴 표시
        binding.menuButton.setOnClickListener {
            val popupMenu = PopupMenu(context, binding.menuButton)
            popupMenu.menuInflater.inflate(R.menu.item_menu_drop, popupMenu.menu)

            // 메뉴 아이템 클릭 이벤트 처리
            popupMenu.setOnMenuItemClickListener { menuItem ->
                when (menuItem.itemId) {
                    R.id.menu_edit -> {
                        // arguments에서 JSON 문자열 꺼내서 객체로 변환
                        val recipeDataJson = Gson().toJson(recipeData)
                        val intent = Intent(requireContext(), ToolbarActivity::class.java).apply {
                            putExtra("mode", 9)
                            putExtra("recipeData", recipeDataJson)
                        }
                        editRecipeLauncher.launch(intent)
                        true
                    }
                    R.id.menu_delete -> {
                        // 레시피 삭제 확인 다이얼로그 표시
                        AlertDialog.Builder(requireContext())
                            .setTitle("레시피 삭제")
                            .setMessage("레시피를 삭제하시겠습니까?")
                            .setPositiveButton("삭제") { _, _ ->
                                Toast.makeText(context, "레시피가 삭제되었습니다", Toast.LENGTH_SHORT).show()
                                parentFragmentManager.popBackStack()
                            }
                            .setNegativeButton("취소", null)
                            .show()
                        true
                    }
                    else -> false
                }
            }
            popupMenu.show()
        }

        // ------------------------ 3. 리스트 설정 ------------------------
        // 태그 리스트 설정 (FlexboxLayoutManager 사용)
        binding.tagList.apply {
            layoutManager = FlexboxLayoutManager(context).apply {
                flexDirection = FlexDirection.ROW
                justifyContent = JustifyContent.FLEX_START
            }
            adapter = IngredientTagChipAdapter(recipeData.ingredientTagIds)
        }

        // 재료 리스트 설정
        binding.ingredientsList.apply {
            layoutManager = FlexboxLayoutManager(context).apply {
                flexDirection = FlexDirection.ROW
                justifyContent = JustifyContent.FLEX_START
            }

            adapter = FlavoringAdapter(recipeData.ingredients.map { FlavoringData(it.key, it.value) })
        }

        // 양념 리스트 설정
        binding.seasoningList.apply {
            layoutManager = FlexboxLayoutManager(context).apply {
                flexDirection = FlexDirection.ROW
                justifyContent = JustifyContent.FLEX_START
            }
            adapter = FlavoringAdapter(recipeData.seasoning.map { FlavoringData(it.key, it.value) })
        }

        // ------------------------ 더미 댓글 데이터 ------------------------
        comments.addAll(listOf(
            CommentData(
                commentId = "1",
                profileImageResId = R.drawable.ic_profile,
                memberTitle = "건강지기",
                memberNickname = "wellness_lover",
                commentText = "이 레시피 정말 맛있고 건강해요!",
                date = "2025-05-01 12:13"
            ),
            CommentData(
                commentId = "2",
                profileImageResId = R.drawable.ic_profile,
                memberTitle = "슬로우에이징러",
                memberNickname = "슬로우",
                commentText = "아보카도 좋아하는데 해봐야겠어요~",
                date = "2025-05-01 12:13"
            )
        ))

        // ------------------------ 댓글 어댑터 연결 ------------------------
        binding.commentList.layoutManager = LinearLayoutManager(requireContext())
        commentAdapter = RecipeCommentAdapter(requireContext(), comments, this)
        binding.commentList.adapter = commentAdapter

        // 댓글 작성/수정 버튼 클릭 이벤트 처리
        binding.commentButton.setOnClickListener {
            val commentText = binding.editComment.text.toString()
            if (commentText.isNotEmpty()) {
                if (editingCommentId != null) {
                    // 댓글 수정
                    val commentIndex = comments.indexOfFirst { it.commentId == editingCommentId }
                    if (commentIndex != -1) {
                        val updatedComment = comments[commentIndex].copy(commentText = commentText)
                        comments[commentIndex] = updatedComment
                        commentAdapter.notifyItemChanged(commentIndex)
                    }
                    // 수정 모드 초기화
                    editingCommentId = null
                    binding.commentButton.text = getString(R.string.check)
                } else {
                    // 새 댓글 작성
                    val currentTime = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date())
                    val newComment = CommentData(
                        commentId = UUID.randomUUID().toString(),
                        profileImageResId = R.drawable.ic_profile,
                        memberTitle = "userTitle",
                        memberNickname = "userNickname",
                        commentText = commentText,
                        date = currentTime
                    )
                    comments.add(newComment)
                    commentAdapter.notifyItemInserted(comments.size - 1)
                    binding.commentList.smoothScrollToPosition(comments.size - 1)
                }
                binding.editComment.text.clear()
            } else {
                Toast.makeText(context, getString(R.string.please_enter_comment), Toast.LENGTH_SHORT).show()
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
     * 좋아요 수를 보기 좋은 포맷으로 변환
     * @param count 변환할 숫자
     * @return 포맷팅된 문자열 (예: 1.2k, 2M)
     */
    private fun formatCount(count: Int): String {
        return when {
            count >= 1_000_000 -> String.format(Locale.KOREA,"%.1fM", count / 1_000_000.0)
            count >= 1_000     -> String.format(Locale.KOREA,"%.1fk", count / 1_000.0)
            else               -> count.toString()
        }.removeSuffix(".0") // 소수점 0 제거
    }

    override fun onEditComment(commentId: String, commentText: String) {
        // 수정할 댓글 ID 저장
        editingCommentId = commentId
        // EditText에 기존 댓글 내용 설정
        binding.editComment.setText(commentText)
        // 버튼 텍스트 변경
        binding.commentButton.text = "수정"
    }

    override fun onDeleteComment(commentId: String) {
        // 댓글 삭제 확인 다이얼로그 표시
        AlertDialog.Builder(requireContext())
            .setTitle("댓글 삭제")
            .setMessage("댓글을 삭제하시겠습니까?")
            .setPositiveButton("삭제") { _, _ ->
                // 댓글 삭제
                val commentIndex = comments.indexOfFirst { it.commentId == commentId }
                if (commentIndex != -1) {
                    comments.removeAt(commentIndex)
                    commentAdapter.notifyItemRemoved(commentIndex)
                }
            }
            .setNegativeButton("취소", null)
            .show()
    }
}
