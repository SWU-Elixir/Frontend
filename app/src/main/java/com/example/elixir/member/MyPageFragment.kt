package com.example.elixir.member

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.*
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.elixir.R
import com.example.elixir.RetrofitClient
import com.example.elixir.ToolbarActivity
import com.example.elixir.databinding.FragmentMypageBinding
import com.example.elixir.dialog.LogoutDialog
import com.example.elixir.member.data.MemberEntity
import com.example.elixir.member.network.MemberDB
import com.example.elixir.member.network.MemberRepository
import com.example.elixir.member.viewmodel.MemberService
import com.example.elixir.member.viewmodel.MemberViewModel
import kotlinx.coroutines.launch

class MyPageFragment : Fragment() {

    private var myPageBinding: FragmentMypageBinding? = null
    private val binding get() = myPageBinding!!

    private lateinit var viewModel: MemberViewModel

    private val spanCount = 3 // 한 줄에 3개
    private val spacing = 16 // dp → px로 변환 필요

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        myPageBinding = FragmentMypageBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        try {
            val db = MemberDB.getInstance(requireContext())
            val api = RetrofitClient.instanceMemberApi
            val repository = MemberRepository(api, db.memberDao())
            val service = MemberService(repository)
            viewModel = MemberViewModel(service)

            viewModel.loadMember()

        } catch (e: Exception) {
            Log.e("MyPage", "Initialization error: ${e.message}", e)
            Toast.makeText(requireContext(), "초기화 중 오류가 발생했습니다: ${e.message}", Toast.LENGTH_LONG).show()
        }

        viewModel.member.observe(viewLifecycleOwner) { member ->
            member?.let {
                setProfile(it)
            }
        }

        // ------------------------------------ RecyclerView 설정 ------------------------------------ //
        binding.mypageBadgeGrid.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        binding.mypageRecipeGrid.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        binding.mypageScrapGrid.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)

        // ViewModel에서 top3 데이터 로드
        viewModel.loadTop3Achievements()
        viewModel.loadTop3Recipes()
        viewModel.loadTop3Scraps()

        viewModel.top3Achievements.observe(viewLifecycleOwner) { achievements ->
            val badgeList = if (achievements.isNotEmpty()) {
                achievements.map { Uri.parse(it.achievementImageUrl) }
            } else {
                listOf(
                    Uri.parse("android.resource://${context?.packageName}/${R.drawable.bg_badge_empty}"),
                    Uri.parse("android.resource://${context?.packageName}/${R.drawable.bg_badge_empty}"),
                    Uri.parse("android.resource://${context?.packageName}/${R.drawable.bg_badge_empty}")
                )
            }
            binding.mypageBadgeGrid.adapter = MyPageCollectionAdapter(badgeList, true)
        }
        viewModel.top3Recipes.observe(viewLifecycleOwner) { recipes ->
            val recipeList = if (recipes.isNotEmpty()) {
                recipes.map { Uri.parse(it) }
            } else {
                listOf(
                    Uri.parse("android.resource://${context?.packageName}/${R.drawable.img_blank}"),
                    Uri.parse("android.resource://${context?.packageName}/${R.drawable.img_blank}"),
                    Uri.parse("android.resource://${context?.packageName}/${R.drawable.img_blank}")
                )
            }
            binding.mypageRecipeGrid.adapter = MyPageCollectionAdapter(recipeList, false)
        }
        viewModel.top3Scraps.observe(viewLifecycleOwner) { scraps ->
            val scrapList = if (scraps.isNotEmpty()) {
                scraps.map { Uri.parse(it) }
            } else {
                listOf(
                    Uri.parse("android.resource://${context?.packageName}/${R.drawable.img_blank}"),
                    Uri.parse("android.resource://${context?.packageName}/${R.drawable.img_blank}"),
                    Uri.parse("android.resource://${context?.packageName}/${R.drawable.img_blank}")
                )
            }
            binding.mypageScrapGrid.adapter = MyPageCollectionAdapter(scrapList, false)
        }
        // ------------------------------------------------------------------------------------------ //

        // ---------------------------------- RecyclerView 간격 설정 ---------------------------------- //
        val spacingPx = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, spacing.toFloat(), resources.displayMetrics).toInt()

        // RecyclerView 아이템 간격 설정
        binding.mypageBadgeGrid.addItemDecoration(GridItemDecoration(spanCount, spacingPx, 16))
        binding.mypageRecipeGrid.addItemDecoration(GridItemDecoration(spanCount, spacingPx, 16))
        binding.mypageScrapGrid.addItemDecoration(GridItemDecoration(spanCount, spacingPx, 16))
        // ------------------------------------------------------------------------------------------ //

        // 프로필 수정 버튼 클릭 시 회원가입 페이지로 넘어가게
        binding.btnProfileEdit.setOnClickListener {
            val intent = Intent(context, ToolbarActivity::class.java).apply {
                putExtra("mode", 10)
                putExtra("nickname", "닉네임")
            }
            startActivity(intent)
        }

        // 로그아웃 버튼
        binding.btnLogout.setOnClickListener {
            LogoutDialog(requireActivity()).show()
        }

        // 팔로워 클릭 시 팔로워 목록 페이지로 넘어가게
        binding.textFollower.setOnClickListener {
            val intent = Intent(context, ToolbarActivity::class.java).apply {
                putExtra("mode", 11)
                putExtra("title", "팔로워 목록")
            }
            startActivity(intent)
        }

        // 팔로워 클릭 시 팔로워 목록 페이지로 넘어가게
        binding.textFollowing.setOnClickListener {
            val intent = Intent(context, ToolbarActivity::class.java).apply {
                putExtra("mode", 8)
                putExtra("title", "팔로잉 목록")
            }
            startActivity(intent)
        }

        // ------------------------------------ 더보기 클릭 이벤트 설정 --------------------------------- //
        // 내 레시피 더보기 클릭 시 내 레시피 페이지로 넘어가게
        binding.btnMoreRecipe.setOnClickListener {
            val intent = Intent(context, ToolbarActivity::class.java).apply {
                putExtra("mode", 5)
                putExtra("title", "내 레시피")
            }
            startActivity(intent)
        }

        // 내 스크랩 더보기 클릭 시 내 스크랩 페이지로 넘어가게
        binding.btnMoreScrap.setOnClickListener {
            val intent = Intent(context, ToolbarActivity::class.java).apply {
                putExtra("mode", 6)
                putExtra("title", "내 스크랩")
            }
            startActivity(intent)
        }

        // 내 뱃지 더보기 클릭 시 내 뱃지 페이지로 넘어가게
        binding.btnMoreBadge.setOnClickListener {
            val intent = Intent(context, ToolbarActivity::class.java).apply {
                putExtra("mode", 7)
                putExtra("title", "내 뱃지")
            }
            startActivity(intent)
        }
        // ------------------------------------------------------------------------------------------ //
    }

    private fun setProfile(profile : MemberEntity) {
        binding.apply {
            // Glide를 사용하여 이미지 로드
            Glide.with(requireContext())
                .load(profile.profileUrl)
                .placeholder(R.drawable.img_blank) // 로딩 중 표시할 이미지
                .error(R.drawable.img_blank) // 에러 발생 시 표시할 이미지
                .into(mypageProfileImg)
            userNickname.text = profile.nickname
        }
    }
    override fun onDestroyView() {
        super.onDestroyView()
        myPageBinding = null
    }
}
