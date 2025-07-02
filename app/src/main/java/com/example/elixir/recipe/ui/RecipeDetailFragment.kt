package com.example.elixir.recipe.ui

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
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.elixir.R
import com.example.elixir.RetrofitClient
import com.example.elixir.ToolbarActivity
import com.example.elixir.databinding.FragmentRecipeDetailBinding
import com.example.elixir.dialog.DeleteDialog
import com.example.elixir.ingredient.data.IngredientData
import com.example.elixir.ingredient.network.IngredientDB
import com.example.elixir.ingredient.network.IngredientRepository
import com.example.elixir.ingredient.viewmodel.IngredientViewModel
import com.example.elixir.ingredient.viewmodel.IngredientViewModelFactory
import com.example.elixir.member.data.ProfileEntity
import com.example.elixir.member.network.MemberDB
import com.example.elixir.member.network.MemberRepository
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
import kotlinx.coroutines.launch
import java.util.Locale


/**
 * 레시피 상세 정보를 표시하는 프래그먼트
 * 레시피의 기본 정보, 재료, 조리 순서, 댓글 등을 보여주고 관리
 */
class RecipeDetailFragment : Fragment(), CommentActionListener {
    // ViewBinding 관련 변수
    private var _binding: FragmentRecipeDetailBinding? = null
    private val binding get() = _binding!!

    // Repository
    private lateinit var recipeRepository: RecipeRepository
    private lateinit var memberRepository: MemberRepository
    private lateinit var commentRepository: CommentRepository
    private lateinit var ingredientRepository: IngredientRepository

    private val recipeViewModel: RecipeViewModel by viewModels {
        RecipeViewModelFactory(recipeRepository)
    }

    // 댓글 관련 변수
    private lateinit var commentAdapter: RecipeCommentAdapter
    private var comments = mutableListOf<CommentItem>()
    private var editingCommentId: Int = -1
    private var userNickname: String? = null

    // 댓글 뷰모델
    private val commentViewModel: CommentViewModel by viewModels {
        CommentViewModelFactory(commentRepository)
    }

    // 멤버 뷰모델
    private val memberViewModel: MemberViewModel by viewModels {
        MemberViewModelFactory(memberRepository)
    }

    // 재료 뷰모델
    private val ingredientViewModel: IngredientViewModel by viewModels {
        IngredientViewModelFactory(ingredientRepository)
    }

    // 수정할 시 launcher 띄움
    private lateinit var editRecipeLauncher: ActivityResultLauncher<Intent>
    private var recipeId = -1
    private var ingredientDataMap: Map<Int, IngredientData>? = null

    // 회원 정보
    private lateinit var member: ProfileEntity

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // 바인딩에 프래그먼트 할당
        _binding = FragmentRecipeDetailBinding.inflate(inflater, container, false)
        // 수정 페이지 런처 설정 -> 레시피 등록 페이지에서 수정 후 돌아올 때
        editRecipeLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                // 수정한 레시피로 상세 페이지 정보 불러오기
                recipeId = result.data?.getIntExtra("recipeId", -1) ?: -1
                recipeViewModel.getRecipeById(recipeId)
            }
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // --------------------- Repository 초기화 ---------------------
        // 레시피
        recipeRepository = RecipeRepository(RetrofitClient.instanceRecipeApi,
            AppDatabase.getInstance(requireContext()).recipeDao())

        // 회원
        memberRepository = MemberRepository(RetrofitClient.instanceMemberApi,
            MemberDB.getInstance(requireContext()).memberDao())

        // 댓글
        commentRepository = CommentRepository(RetrofitClient.instanceCommentApi,
            AppDatabase.getInstance(requireContext()).commentDao())

        // 재료 (초기화 위치 변경)
        ingredientRepository = IngredientRepository(RetrofitClient.instanceIngredientApi,
            IngredientDB.getInstance(requireContext()).ingredientDao())

        // ingredientViewModel 데이터 로드 (onViewCreated 초반에 한 번만 호출)
        ingredientViewModel.loadIngredients()

        // ------------------------ 데이터 적용 ------------------------
        // arguments에서 레시피 아이디 꺼내서 서버로부터 레시피 불러오기
        recipeId = arguments?.getInt("recipeId") ?: return
        recipeViewModel.getRecipeById(recipeId)

        // 레시피 기본 정보 설정
        recipeViewModel.recipeDetail.observe(viewLifecycleOwner) { recipeData ->
            if (recipeData != null) {
                // recipeData에 저장된 값으로 UI 초기화
                Log.d("RecipeDetailFragment", "$recipeData")
                // 유저
                lifecycleScope.launch {
                    // 유저
                    val memberApi = RetrofitClient.instanceMemberApi
                    val member = memberApi.getProfile(recipeData.authorId).data

                    binding.tvMember.text = if(member.title.isNullOrBlank()) "일반" else member.title
                    binding.tvNickname.text = member.nickname

                    val profileImg = if(member.profileUrl.isNullOrBlank()) R.drawable.ic_profile else member.profileUrl
                    Glide.with(requireContext())
                        .load(profileImg)
                        .placeholder(R.drawable.ic_profile)
                        .error(R.drawable.ic_profile)
                        .into(binding.imgProfile)

                    // 팔로우 버튼 상태 초기화
                    if(recipeData.authorFollowByCurrentUser) {
                        binding.btnFollow.text = context?.getString(R.string.following)
                        binding.btnFollow.setBackgroundResource(R.drawable.bg_rect_outline_gray)
                        context?.getColor(R.color.black)?.let { binding.btnFollow.setTextColor(it) }
                    } else {
                        binding.btnFollow.text = context?.getString(R.string.follow)
                        binding.btnFollow.setBackgroundResource(R.drawable.bg_rect_filled_orange)
                        context?.getColor(R.color.white)?.let { binding.btnFollow.setTextColor(it) }
                    }

                    // 팔로우 버튼 클릭 이벤트 처리
                    binding.btnFollow.setOnClickListener {
                        val isFollowing = binding.btnFollow.text == getString(R.string.following)

                        // 코루틴으로 감싸기!
                        lifecycleScope.launch {
                            if (isFollowing) {
                                memberApi.unfollow(recipeData.authorId)
                            } else {
                                memberApi.follow(recipeData.authorId)
                            }

                            // UI 업데이트는 메인스레드에서 안전하게
                            binding.btnFollow.text = if (isFollowing) getString(R.string.follow) else getString(R.string.following)
                            binding.btnFollow.setBackgroundResource(
                                if (isFollowing) R.drawable.bg_rect_filled_orange
                                else R.drawable.bg_rect_outline_gray
                            )
                            binding.btnFollow.setTextColor(
                                resources.getColor(
                                    if (isFollowing) R.color.white
                                    else R.color.black,
                                    null
                                )
                            )
                        }
                    }
                }


                // 레시피
                binding.recipeNameText.text = recipeData.title
                binding.categorySlowAging.text = recipeData.categorySlowAging
                binding.categoryType.text = recipeData.categoryType
                binding.recipeLevel.text = recipeData.difficulty
                Glide.with(requireContext())
                    .load(recipeData.imageUrl)
                    .placeholder(R.drawable.ic_recipe_white)
                    .error(R.drawable.ic_recipe_white)
                    .into(binding.imgRecipe)


                // 순서
                // 조리순서 데이터 변환
                val stepList = recipeData.stepDescriptions.zip(recipeData.stepImageUrls) { desc, imgUrl ->
                    RecipeStepData(stepDescription = desc, stepImg = imgUrl)
                }

                // 조리순서 RecyclerView에 어댑터 연결
                binding.stepList.layoutManager = LinearLayoutManager(requireContext(),
                    LinearLayoutManager.VERTICAL, false)
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

                // 스크랩 버튼 클릭 이벤트 처리
                binding.bookmarkButton.setOnClickListener{
                    recipeData.scrappedByCurrentUser = !recipeData.scrappedByCurrentUser
                    if (recipeData.scrappedByCurrentUser) {
                        recipeViewModel.addScrap(recipeId)
                    } else {
                        recipeViewModel.deleteScrap(recipeId)
                    }
                    // 스크랩 상태에 따라 카운트 증가/감소
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
                        recipeViewModel.addLike(recipeId)
                    } else {
                        recipeData.likes--
                        recipeViewModel.deleteLike(recipeId)
                    }
                    // 버튼 이미지와 카운트 업데이트
                    binding.heartButton.setBackgroundResource(
                        if(recipeData.likedByCurrentUser) R.drawable.ic_recipe_heart_selected
                        else R.drawable.ic_recipe_heart_normal
                    )
                    binding.heartCount.text = formatCount(recipeData.likes)
                }

                // 회원 정보 불러오기
                memberViewModel.loadProfile()
                memberViewModel.profile.observe(viewLifecycleOwner) { profile ->
                    profile?.let {
                        userNickname = profile.nickname
                        if(member.nickname == userNickname)
                            binding.btnFollow.visibility = View.GONE
                        else
                            binding.btnFollow.visibility = View.VISIBLE

                        // 사용자가 아니면 수정 못하게
                        if(member.nickname != profile.nickname)
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
                            // 레시피 수정 시 레시피 아이디랑 실행 모드 전달
                            R.id.menu_edit -> {
                                val intent = Intent(requireContext(), ToolbarActivity::class.java).apply {
                                    putExtra("mode", 9)
                                    putExtra("recipeId", recipeId)
                                }
                                editRecipeLauncher.launch(intent)
                                true
                            }
                            // 레시피 삭제
                            R.id.menu_delete -> {
                                DeleteDialog(requireActivity()) {
                                    recipeViewModel.deleteRecipe(recipeId)
                                }.show()
                                true
                            }
                            else -> false
                        }
                    }
                    popupMenu.show()
                }

                // ------------------------ 3. 리스트 설정 ------------------------
                // 태그 리스트 설정 (FlexboxLayoutManager 사용)
                // ingredientViewModel 데이터 로드는 onViewCreated 초반에 이미 했으므로 여기서는 observer로 데이터 변경만 감지
                ingredientViewModel.ingredients.observe(viewLifecycleOwner) { ingredientList ->
                    Log.d("Debug", "ingredientList size: ${ingredientList?.size}")

                    if (ingredientList != null && ingredientList.isNotEmpty()) {
                        ingredientDataMap = ingredientList.associateBy { it.id }

                        // 태그 리스트 설정 (FlexboxLayoutManager 사용)
                        val tagIds = recipeData.ingredientTagIds
                        if (tagIds != null && tagIds.isNotEmpty() && ingredientDataMap != null) {
                            binding.listRepresentIngredient.apply {
                                layoutManager = FlexboxLayoutManager(context).apply {
                                    flexDirection = FlexDirection.ROW
                                    justifyContent = JustifyContent.FLEX_START
                                }
                                adapter = IngredientTagChipMapAdapter(tagIds, ingredientDataMap!!)
                                visibility = View.VISIBLE
                            }
                        } else {
                            Log.w("RecipeDetailFragment", "ingredientTagIds is null or empty")
                            binding.listRepresentIngredient.visibility = View.GONE
                        }
                    } else {
                        Log.w("RecipeDetailFragment", "ingredientList is null or empty")
                        binding.listRepresentIngredient.visibility = View.GONE
                    }
                }

                recipeViewModel.deleteResult.observe(viewLifecycleOwner) { result ->
                    result.onSuccess { deleted ->
                        if (deleted) {
                            Toast.makeText(context, "레시피가 삭제되었습니다", Toast.LENGTH_SHORT).show()
                            parentFragmentManager.popBackStack()

                        } else {
                            Toast.makeText(context, "레시피를 삭제하지 못했습니다.", Toast.LENGTH_SHORT).show()
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
                    adapter = FlavoringAdapter(recipeData.ingredients!!.map { FlavoringItem(it.name, it.value, it.unit) })
                }

                // 양념 리스트 설정
                binding.seasoningList.apply {
                    layoutManager = FlexboxLayoutManager(context).apply {
                        flexDirection = FlexDirection.ROW
                        justifyContent = JustifyContent.FLEX_START
                    }
                    adapter = FlavoringAdapter(recipeData.seasonings!!.map { FlavoringItem(it.name, it.value, it.unit) })
                }

                // 댓글 리스트 설정
                binding.commentList.layoutManager = LinearLayoutManager(requireContext())
                // ------------------------ 댓글 어댑터 연결 ------------------------
                commentViewModel.comments.observe(viewLifecycleOwner) { newComments ->
                    commentAdapter = RecipeCommentAdapter(requireContext(), newComments!!.toMutableList(), this)
                    binding.commentList.adapter = commentAdapter
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
                // 업데이트
                recipeViewModel.getRecipeById(recipeId)

                // 수정 모드 초기화
                editingCommentId = -1
                binding.commentButton.text = getString(R.string.check)
                binding.editComment.text.clear()
            } else {
                Toast.makeText(context, getString(R.string.please_enter_comment), Toast.LENGTH_SHORT).show()
            }
        }

        // ------------------------ 4. 뒤로 가기 버튼 ------------------------
        binding.btnBack.setOnClickListener {
            parentFragmentManager.setFragmentResult("refresh_recipes", Bundle()) // 결과 전달
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

    // 댓글 삭제
    override fun onDeleteComment(recipeId: Int, commentId: Int) {
        DeleteDialog(requireActivity()) {
            // 뷰모델에 삭제 요청
            Log.d("DeleteComment", "recipeId: $recipeId, commentId: $commentId")
            commentViewModel.deleteComment(recipeId, commentId)
            Toast.makeText(context, "댓글이 삭제되었습니다.", Toast.LENGTH_SHORT).show()
            // 정보 업데이트
            recipeViewModel.getRecipeById(recipeId)
        }.show()
    }
}
