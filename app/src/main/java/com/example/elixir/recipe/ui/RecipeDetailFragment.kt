package com.example.elixir.recipe.ui

import android.R.attr.fragment
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.elixir.R
import com.example.elixir.RetrofitClient
import com.example.elixir.ToolbarActivity
import com.example.elixir.databinding.FragmentRecipeDetailBinding
import com.example.elixir.ingredient.network.IngredientDB
import com.example.elixir.ingredient.network.IngredientRepository
import com.example.elixir.ingredient.viewmodel.IngredientService
import com.example.elixir.ingredient.viewmodel.IngredientViewModel
import com.example.elixir.member.network.MemberDB
import com.example.elixir.member.network.MemberRepository
import com.example.elixir.member.viewmodel.MemberService
import com.example.elixir.member.viewmodel.MemberViewModel
import com.example.elixir.member.viewmodel.MemberViewModelFactory
import com.example.elixir.network.AppDatabase
import com.example.elixir.recipe.data.CommentItem
import com.example.elixir.recipe.data.CommentRepository
import com.example.elixir.recipe.data.FlavoringItem
import com.example.elixir.recipe.data.RecipeRepository
import com.example.elixir.recipe.data.RecipeStepData
import com.example.elixir.recipe.viewmodel.CommentViewModel
import com.example.elixir.recipe.viewmodel.CommentViewModelFactory
import com.example.elixir.recipe.viewmodel.RecipeViewModel
import com.example.elixir.recipe.viewmodel.RecipeViewModelFactory
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.flexbox.JustifyContent
import java.util.Locale


/**
 * 레시피 상세 정보를 표시하는 프래그먼트
 * 레시피의 기본 정보, 재료, 조리 순서, 댓글 등을 보여주고 관리
 */
class RecipeDetailFragment : Fragment(), CommentActionListener {
    // ViewBinding 관련 변수
    private var _binding: FragmentRecipeDetailBinding? = null
    private val binding get() = _binding!!

    private lateinit var recipeRepository: RecipeRepository
    private lateinit var memberRepository: MemberRepository
    private lateinit var commentRepository: CommentRepository

    // 댓글 관련 변수
    private lateinit var commentAdapter: RecipeCommentAdapter
    private var comments = mutableListOf<CommentItem>()
    private var editingCommentId: Int = -1
    private var userNickname: String? = null


    private val commentViewModel: CommentViewModel by viewModels {
        CommentViewModelFactory(commentRepository)
    }

    private lateinit var editRecipeLauncher: ActivityResultLauncher<Intent>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRecipeDetailBinding.inflate(inflater, container, false)
        // 수정 런처 설정
        editRecipeLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                Toast.makeText(requireContext(), "수정 완료", Toast.LENGTH_SHORT).show()
            }
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // DB, API 초기화
        val recipeDao = AppDatabase.getInstance(requireContext()).recipeDao()
        val recipeApi = RetrofitClient.instanceRecipeApi
        recipeRepository = RecipeRepository(recipeApi, recipeDao)

        val memberDao = MemberDB.getInstance(requireContext()).memberDao()
        val memberApi = RetrofitClient.instanceMemberApi
        memberRepository = MemberRepository(memberApi, memberDao)

        val memberService = MemberService(memberRepository) // 필요 파라미터 전달

        val memberViewModel: MemberViewModel by viewModels {
            MemberViewModelFactory(memberService)
        }

        // 댓글 api, dao
        val commentDao = AppDatabase.getInstance(requireContext()).commentDao()
        val commentApi = RetrofitClient.instanceCommentApi
        commentRepository = CommentRepository(commentApi, commentDao)

        // ------------------------ 데이터 적용 ------------------------
        // arguments에서 JSON 문자열 꺼내서 객체로 변환
        val recipeId = arguments?.getInt("recipeId") ?: return
        val recipeViewModel: RecipeViewModel by viewModels {
            RecipeViewModelFactory(recipeRepository)
        }

        recipeViewModel.getRecipeById(recipeId)

        // 레시피 기본 정보 설정
        recipeViewModel.recipeDetail.observe(viewLifecycleOwner) { recipeData ->
            if (recipeData != null) {
                // recipeData에 저장된 값으로 UI 초기화
                // 유저
                binding.memberTitle.text = if(recipeData.authorTitle.isNullOrBlank()) "일반" else recipeData.authorTitle
                binding.memberNickname.text = recipeData.authorNickname

                // 레시피
                binding.recipeNameText.text = recipeData.title
                binding.categorySlowAging.text = recipeData.categorySlowAging
                binding.categoryType.text = recipeData.categoryType
                binding.recipeLevel.text = recipeData.difficulty
                Glide.with(requireContext())
                    .load(recipeData.imageUrl)
                    .placeholder(R.drawable.img_blank)
                    .error(R.drawable.img_blank)
                    .into(binding.recipeImage)

                // 순서
                // 조리순서 데이터 변환
                val stepList = recipeData.stepDescriptions.zip(recipeData.stepImageUrls) { desc, imgUrl ->
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

                memberViewModel.loadMemberProfile()

                memberViewModel.profile.observe(viewLifecycleOwner) { profile ->
                    profile?.let {
                        userNickname = profile.nickname
                        // 사용자가 아니면 수정 못하게
                        if(recipeData.authorNickname != profile.nickname)
                            binding.menuButton.visibility = View.GONE

                        binding.commentNickname.text = profile.nickname
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
                                // 레시피 아이디랑 실행 모드 전달
                                val intent = Intent(requireContext(), ToolbarActivity::class.java).apply {
                                    putExtra("mode", 9)
                                    putExtra("recipeId", recipeId)
                                }
                                editRecipeLauncher.launch(intent)
                                true
                            }
                            R.id.menu_delete -> {
                                AlertDialog.Builder(requireContext())
                                    .setTitle("레시피 삭제")
                                    .setMessage("레시피를 삭제하시겠습니까?")
                                    .setPositiveButton("삭제") { _, _ ->
                                        // 1. ViewModel의 삭제 함수 호출
                                        recipeViewModel.deleteRecipe(recipeId)
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

                    val ingredientRepository = IngredientRepository(
                        RetrofitClient.instanceIngredientApi,
                        IngredientDB.getInstance(requireContext()).ingredientDao())
                    val ingredientService = IngredientService(ingredientRepository)
                    val ingredientViewModel = IngredientViewModel(ingredientService)

                    ingredientViewModel.ingredients.observe(viewLifecycleOwner) { ingredientList ->
                        adapter = IngredientTagChipAdapter(recipeData.ingredientTagIds, ingredientList)
                    }
                }

                recipeViewModel.deleteResult.observe(viewLifecycleOwner) { result ->
                    result.onSuccess { deleted ->
                        if (deleted) {
                            Toast.makeText(context, "레시피가 삭제되었습니다", Toast.LENGTH_SHORT).show()
                            parentFragmentManager.popBackStack()
                        } else {
                            Toast.makeText(context, "삭제에 실패했습니다.", Toast.LENGTH_SHORT).show()
                        }
                    }
                    result.onFailure {
                        Toast.makeText(context, "삭제 중 오류 발생", Toast.LENGTH_SHORT).show()
                    }
                }


                // 재료 리스트 설정
                binding.ingredientsList.apply {
                    layoutManager = FlexboxLayoutManager(context).apply {
                        flexDirection = FlexDirection.ROW
                        justifyContent = JustifyContent.FLEX_START
                    }

                    adapter = FlavoringAdapter(recipeData.ingredients.map { FlavoringItem(it.name, it.value, it.unit) })
                }

                // 양념 리스트 설정
                binding.seasoningList.apply {
                    layoutManager = FlexboxLayoutManager(context).apply {
                        flexDirection = FlexDirection.ROW
                        justifyContent = JustifyContent.FLEX_START
                    }
                    adapter = FlavoringAdapter(recipeData.seasonings.map { FlavoringItem(it.name, it.value, it.unit) })
                }

                // 댓글 리스트 설정
                comments = recipeData.comments!!.toMutableList()
                Log.d("RecipeDetailFragment", "$comments")

                binding.commentList.layoutManager = LinearLayoutManager(requireContext())
                commentAdapter = RecipeCommentAdapter(requireContext(), comments, this)
                binding.commentList.adapter = commentAdapter

                // ------------------------ 댓글 어댑터 연결 ------------------------
                commentViewModel.comments.observe(viewLifecycleOwner) { newComments ->
                    comments.clear()
                    comments.addAll(newComments)
                    commentAdapter.notifyDataSetChanged()
                    Log.d("RecipeDetailFragment", "리스트: $newComments")
                }

            } else {
                // 에러 처리 (예: Toast 등)
                Toast.makeText(requireContext(), "레시피 정보를 불러올 수 없습니다.", Toast.LENGTH_SHORT).show()
            }
        }

        // 댓글 작성/수정 버튼 클릭 이벤트 처리
        binding.commentButton.setOnClickListener {
            // 입력된 텍스트 가져오기
            val commentText = binding.editComment.text.toString()
            // 입력창이 비어있지 않을 때 실행
            if (commentText.isNotEmpty()) {
                val commentIndex = comments.indexOfFirst { it.commentId == editingCommentId }
                // 댓글 수정
                if (commentIndex != -1) {
                    // 뷰모델에 수정 요청
                    commentViewModel.updateComment(recipeId, editingCommentId, commentText)
                    Toast.makeText(context, "댓글이 수정되었습니다.", Toast.LENGTH_SHORT).show()
                } else {
                    // 댓글 등록
                    commentViewModel.uploadComment(recipeId, commentText)
                    Toast.makeText(context, "댓글이 등록되었습니다.", Toast.LENGTH_SHORT).show()
                }
                // 수정 모드 초기화
                editingCommentId = -1
                binding.commentButton.text = getString(R.string.check)
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

    override fun onEditComment(commentId: Int, commentText: String) {
        // 수정할 댓글 ID 저장
        editingCommentId = commentId
        // EditText에 기존 댓글 내용 설정
        binding.editComment.setText(commentText)
        // 버튼 텍스트 변경
        binding.commentButton.text = "수정"
    }

    override fun onDeleteComment(recipeId: Int, commentId: Int) {
        AlertDialog.Builder(requireContext())
            .setTitle("댓글 삭제")
            .setMessage("댓글을 삭제하시겠습니까?")
            .setPositiveButton("삭제") { _, _ ->
                // 뷰모델에 삭제 요청
                commentViewModel.deleteComment(recipeId, commentId)
                Toast.makeText(context, "댓글이 삭제되었습니다.", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("취소", null)
            .show()
    }
}
