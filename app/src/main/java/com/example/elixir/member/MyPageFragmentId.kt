package com.example.elixir.member

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.*
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.elixir.R
import com.example.elixir.RetrofitClient
import com.example.elixir.ToolbarActivity
import com.example.elixir.databinding.FragmentMypageBinding
import com.example.elixir.databinding.FragmentMypageIdBinding
import com.example.elixir.dialog.LogoutDialog
import com.example.elixir.member.data.MemberEntity
import com.example.elixir.member.data.ProfileEntity
import kotlinx.coroutines.launch

class MyPageFragmentId : Fragment() {

    private var myPageBinding: FragmentMypageIdBinding? = null
    private val binding get() = myPageBinding!!

    private val spanCount = 3 // 한 줄에 3개
    private val spacing = 16 // dp → px로 변환 필요
    private var memberId: Int = -1  // memberId를 클래스 변수로 선언
    private var memberNickname: String = ""
    private var myId: Int = -1 // 현재 로그인한 사용자의 ID
    private var isFollowing: Boolean = false // 팔로우 상태

    companion object {
        private const val TAG = "MyPageFragment"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        memberId = arguments?.getInt("memberId", -1) ?: -1
        Log.d(TAG, "onCreate - memberId: $memberId")  // 로그 추가
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        myPageBinding = FragmentMypageIdBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (memberId == -1) {
            Log.e(TAG, "memberId가 설정되지 않았습니다")
            return
        }

        // RecyclerView 설정
        binding.mypageBadgeGrid.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        binding.mypageRecipeGrid.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)

        // 데이터 로드
        loadMyId() // 내 ID 로드
        loadMemberProfile(memberId)
        loadTop3Achievements(memberId)
        loadTop3Recipes(memberId)
        checkFollowStatus() // 팔로우 상태 확인

        // RecyclerView 간격 설정
        val spacingPx = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, spacing.toFloat(), resources.displayMetrics).toInt()
        binding.mypageBadgeGrid.addItemDecoration(GridItemDecoration(spanCount, spacingPx, 16))
        binding.mypageRecipeGrid.addItemDecoration(GridItemDecoration(spanCount, spacingPx, 16))

        // 버튼 클릭 이벤트 설정
        setupClickListeners()

        // 팔로우 버튼 클릭 이벤트
        binding.followButton.setOnClickListener {
            if (myId == -1) {
                Toast.makeText(requireContext(), "로그인이 필요합니다.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (myId == memberId) {
                binding.followButton.visibility = View.GONE
                return@setOnClickListener
            }

            lifecycleScope.launch {
                try {
                    val api = RetrofitClient.instanceMemberApi
                    val response = if (isFollowing) {
                        api.unfollow(memberId)
                    } else {
                        api.follow(memberId)
                    }

                    if (response.isSuccessful) {
                        isFollowing = !isFollowing
                        updateFollowButtonUI()
                        // 팔로워 수 업데이트
                        loadMemberProfile(memberId)
                    } else {
                        Toast.makeText(requireContext(), "요청 실패: ${response.code()}", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    Toast.makeText(requireContext(), "네트워크 오류: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun loadMyId() {
        lifecycleScope.launch {
            try {
                val api = RetrofitClient.instanceMemberApi
                val response = api.getProfile()
                if (response.status == 200) {
                    myId = response.data.id
                    // 내 ID와 현재 프로필 ID가 같으면 팔로우 버튼 숨기기
                    if (myId == memberId) {
                        binding.followButton.visibility = View.GONE
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "내 ID 로드 실패", e)
            }
        }
    }

    private fun checkFollowStatus() {
        lifecycleScope.launch {
            try {
                val api = RetrofitClient.instanceMemberApi
                val response = api.getFollowing()
                if (response.status == 200) {
                    isFollowing = response.data.any { it.id == memberId }
                    updateFollowButtonUI()
                }
            } catch (e: Exception) {
                Log.e(TAG, "팔로우 상태 확인 실패", e)
            }
        }
    }

    private fun updateFollowButtonUI() {
        binding.followButton.apply {
            text = if (isFollowing) getString(R.string.following) else getString(R.string.follow)
            setBackgroundResource(
                if (isFollowing) R.drawable.bg_rect_outline_gray
                else R.drawable.bg_rect_filled_orange
            )
            setTextColor(
                resources.getColor(
                    if (isFollowing) R.color.black
                    else R.color.white,
                    null
                )
            )
        }
    }

    private fun loadMemberProfile(memberId: Int) {
        lifecycleScope.launch {
            try {
                val api = RetrofitClient.instanceMemberApi
                val response = api.getProfile(memberId)
                if (response.status == 200) {
                    response.data?.let { member ->
                        setProfile(member)
                    }
                } else {
                    Log.e(TAG, "회원 정보 로드 실패: ${response.message}")
                    Toast.makeText(requireContext(), "회원 정보를 불러올 수 없습니다.", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Log.e(TAG, "회원 정보 로드 실패", e)
                Toast.makeText(requireContext(), "회원 정보를 불러올 수 없습니다: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun loadTop3Achievements(memberId: Int) {
        lifecycleScope.launch {
            try {
                val api = RetrofitClient.instanceMemberApi
                val response = api.getTop3Achievements(memberId)
                if (response.status == 200) {
                    val achievements = response.data
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
            } catch (e: Exception) {
                Log.e(TAG, "뱃지 목록 로드 실패", e)
                binding.mypageBadgeGrid.adapter = MyPageCollectionAdapter(emptyList(), true)
            }
        }
    }

    private fun loadTop3Recipes(memberId: Int) {
        lifecycleScope.launch {
            try {
                val api = RetrofitClient.instanceMemberApi
                val response = api.getMyRecipes(memberId)
                if (response.status == 200) {
                    val recipes = response.data
                    val recipeList = if (recipes.isNotEmpty()) {
                        recipes.map {
                            Uri.parse(it.imageUrl ?: "")
                        }
                    } else {
                        emptyList()
                    }
                    binding.MyRecipeNo.visibility = if (recipeList.isEmpty()) View.VISIBLE else View.GONE
                    binding.mypageRecipeGrid.adapter = MyPageCollectionAdapter(recipeList, false)
                }
            } catch (e: Exception) {
                Log.e(TAG, "레시피 목록 로드 실패", e)
                binding.mypageRecipeGrid.adapter = MyPageCollectionAdapter(emptyList(), false)
            }
        }
    }

    private fun setupClickListeners() {
        // 팔로워 클릭
        binding.textFollower.setOnClickListener {
            val intent = Intent(context, ToolbarActivity::class.java).apply {
                putExtra("mode", 11)
                putExtra("memberId", memberId)
                putExtra("title", "팔로워 목록")
            }
            startActivity(intent)
        }

        // 팔로잉 클릭
        binding.textFollowing.setOnClickListener {
            val intent = Intent(context, ToolbarActivity::class.java).apply {
                putExtra("mode", 8)
                putExtra("memberId", memberId)
                putExtra("title", "팔로잉 목록")
            }
            startActivity(intent)
        }

        // 더보기 버튼들
//        binding.btnMoreRecipe.setOnClickListener {
//            val intent = Intent(context, ToolbarActivity::class.java).apply {
//                putExtra("mode", 5)
//                putExtra("memberId", memberId)
//                putExtra("title", "내 레시피")
//            }
//            startActivity(intent)
//        }

        binding.btnMoreBadge.setOnClickListener {
            val intent = Intent(context, ToolbarActivity::class.java).apply {
                putExtra("mode", 7)
                putExtra("memberId", memberId)
                putExtra("title", memberNickname + "의 뱃지")
            }
            startActivity(intent)
        }
    }

    @SuppressLint("SetTextI18n")
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
            badgeText.text = profile.nickname + " 의 뱃지";
            recipeText.text = profile.nickname + " 의 레시피";
            memberNickname = profile.nickname.toString()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        myPageBinding = null
    }
}
