package com.example.elixir.member.ui

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.*
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.elixir.R
import com.example.elixir.ToolbarActivity
import com.example.elixir.databinding.FragmentMypageBinding
import com.example.elixir.dialog.LogoutDialog
import com.example.elixir.member.network.MemberRepository
import com.example.elixir.member.viewmodel.MemberViewModel
import com.example.elixir.member.viewmodel.MemberViewModelFactory
import com.example.elixir.RetrofitClient
import com.example.elixir.member.data.ProfileEntity
import com.example.elixir.member.network.MemberDB


class MyPageFragment : Fragment() {

    private var myPageBinding: FragmentMypageBinding? = null
    private val binding get() = myPageBinding!!

    private lateinit var memberViewModel: MemberViewModel // ViewModel 인스턴스 선언

    private val spanCount = 3 // 한 줄에 3개
    private val spacing = 16 // dp → px로 변환 필요

    private var profileEditLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            // 프로필 수정 성공 시 프로필 정보 새로고침
            memberViewModel.loadProfile() // ViewModel을 통해 프로필 정보 로드 요청
        }
    }

    companion object {
        private const val TAG = "MyPageFragment"
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        myPageBinding = FragmentMypageBinding.inflate(inflater, container, false)

        // ViewModel 초기화 (Repository와 Dao 주입)
        val memberDao = MemberDB.getInstance(requireContext()).memberDao()
        val memberApi = RetrofitClient.instanceMemberApi
        val repository = MemberRepository(memberApi, memberDao)
        val factory = MemberViewModelFactory(repository)
        memberViewModel = ViewModelProvider(this, factory).get(MemberViewModel::class.java)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // RecyclerView 설정
        binding.mypageBadgeGrid.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        binding.mypageRecipeGrid.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        binding.mypageScrapGrid.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)

        // 데이터 로드 요청 (ViewModel을 통해)
        memberViewModel.loadProfile()
        memberViewModel.loadTop3Challenges()
        memberViewModel.loadMyRecipes()
        memberViewModel.loadScrapRecipes()

        // LiveData 관찰
        setupObservers()

        // RecyclerView 간격 설정
        val spacingPx = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            spacing.toFloat(),
            resources.displayMetrics
        ).toInt()
        binding.mypageBadgeGrid.addItemDecoration(GridItemDecoration(spanCount, spacingPx, 16))
        binding.mypageRecipeGrid.addItemDecoration(GridItemDecoration(spanCount, spacingPx, 16))
        binding.mypageScrapGrid.addItemDecoration(GridItemDecoration(spanCount, spacingPx, 16))

        // 버튼 클릭 이벤트 설정
        setupClickListeners()
    }

    private fun setupObservers() {
        memberViewModel.profile.observe(viewLifecycleOwner) { profile ->
            profile?.let {
                setProfile(it)
            } ?: run {
                Log.e(TAG, "회원 정보를 불러올 수 없습니다.")
                Toast.makeText(requireContext(), "회원 정보를 불러올 수 없습니다.", Toast.LENGTH_SHORT).show()
            }
        }

        memberViewModel.top3Challenges.observe(viewLifecycleOwner) { achievements ->
            val badgeList = if (achievements.isNotEmpty()) {
                achievements.map {
                    Uri.parse(it.achievementImageUrl ?: "")
                }
            } else {
                emptyList()
            }
            binding.badgeNo.visibility = if (badgeList.isEmpty()) View.VISIBLE else View.GONE
            binding.mypageBadgeGrid.adapter = MyPageCollectionAdapter(badgeList, true)
        }

        memberViewModel.myRecipes.observe(viewLifecycleOwner) { recipes ->
            val recipeList = if (recipes.isNotEmpty()) {
                recipes.take(3).map { // 상위 3개만 보여주도록 take(3) 적용
                    Uri.parse(it.imageUrl ?: "")
                }
            } else {
                emptyList()
            }
            binding.MyRecipeNo.visibility = if (recipeList.isEmpty()) View.VISIBLE else View.GONE
            binding.mypageRecipeGrid.adapter = MyPageCollectionAdapter(recipeList, false)
        }

        memberViewModel.scrapRecipes.observe(viewLifecycleOwner) { scraps ->
            val scrapList = if (scraps.isNotEmpty()) {
                scraps.take(3).map { Uri.parse(it.imageUrl ?: "") } // 상위 3개만 보여주도록 take(3) 적용
            } else {
                emptyList()
            }
            binding.MyScrapNo.visibility = if (scrapList.isEmpty()) View.VISIBLE else View.GONE
            binding.mypageScrapGrid.adapter = MyPageCollectionAdapter(scrapList, false)
        }

        memberViewModel.error.observe(viewLifecycleOwner) { errorMessage ->
            errorMessage?.let {
                Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupClickListeners() {
        // 프로필 수정 버튼
        binding.btnProfileEdit.setOnClickListener {
            val intent = Intent(context, ToolbarActivity::class.java).apply {
                putExtra("mode", 14)
            }
            profileEditLauncher.launch(intent)
        }

        // 로그아웃 버튼
        binding.btnLogout.setOnClickListener {
            LogoutDialog(requireActivity()).show()
        }

        // 팔로워 클릭
        binding.textFollower.setOnClickListener {
            val intent = Intent(context, ToolbarActivity::class.java).apply {
                putExtra("mode", 11)
                putExtra("title", "팔로워 목록")
            }
            startActivity(intent)
        }

        // 팔로잉 클릭
        binding.textFollowing.setOnClickListener {
            val intent = Intent(context, ToolbarActivity::class.java).apply {
                putExtra("mode", 8)
                putExtra("title", "팔로잉 목록")
            }
            startActivity(intent)
        }

        // 더보기 버튼들
        binding.btnMoreRecipe.setOnClickListener {
            val intent = Intent(context, ToolbarActivity::class.java).apply {
                putExtra("mode", 5)
                putExtra("title", "내 레시피")
            }
            startActivity(intent)
        }

        binding.btnMoreScrap.setOnClickListener {
            val intent = Intent(context, ToolbarActivity::class.java).apply {
                putExtra("mode", 6)
                putExtra("title", "내 스크랩")
            }
            startActivity(intent)
        }

        binding.btnMoreBadge.setOnClickListener {
            val intent = Intent(context, ToolbarActivity::class.java).apply {
                putExtra("mode", 7)
                putExtra("title", "내 뱃지")
            }
            startActivity(intent)
        }
    }

    private fun setProfile(profile: ProfileEntity) {
        binding.apply {
            Glide.with(requireContext())
                .load(profile.profileUrl)
                .placeholder(R.drawable.img_blank)
                .error(R.drawable.img_blank)
                .into(mypageProfileImg)
            userNickname.text = profile.nickname
            cntFollower.text = profile.followerCount.toString()
            cntFollowing.text = profile.followingCount.toString()
            userTitle.text = profile.title
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        myPageBinding = null
    }
}