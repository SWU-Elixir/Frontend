package com.example.elixir.member.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.elixir.ToolbarActivity
import com.example.elixir.databinding.FragmentMypageFollowListBinding
import com.example.elixir.member.data.FollowItem
import com.example.elixir.member.network.MemberRepository
import com.example.elixir.member.viewmodel.MemberViewModel
import com.example.elixir.member.viewmodel.MemberViewModelFactory
import com.example.elixir.RetrofitClient
import com.example.elixir.network.AppDatabase

class MypageFollowListFragment : Fragment() {
    private var _binding: FragmentMypageFollowListBinding? = null
    private val binding get() = _binding!!

    private var mode: Int = MODE_FOLLOWER // 기본값
    private var targetMemberId: Int = -1  // member -> targetMemberId로 이름 변경

    private lateinit var memberViewModel: MemberViewModel // ViewModel 인스턴스

    companion object {
        private const val TAG = "MypageFollowListFragment"
        const val MODE_FOLLOWER = 0
        const val MODE_FOLLOWING = 1

        fun newInstance(mode: Int, memberId: Int = -1): MypageFollowListFragment {
            Log.d(TAG, "newInstance - mode: $mode, memberId: $memberId")
            return MypageFollowListFragment().apply {
                arguments = Bundle().apply {
                    putInt("mode", mode)
                    putInt("memberId", memberId)
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mode = arguments?.getInt("mode", MODE_FOLLOWER) ?: MODE_FOLLOWER
        targetMemberId = arguments?.getInt("memberId", -1) ?: -1
        Log.d(TAG, "onCreate - mode: $mode, targetMemberId: $targetMemberId")

        val appDB = AppDatabase.getInstance(requireContext())

        // ViewModel 초기화
        val memberDao = appDB.memberDao()
        val memberApi = RetrofitClient.instanceMemberApi
        val repository = MemberRepository(memberApi, memberDao)
        val factory = MemberViewModelFactory(repository)
        memberViewModel = ViewModelProvider(this, factory).get(MemberViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMypageFollowListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupObservers() // LiveData 관찰 설정
        loadFollowData() // 데이터 로드 요청
    }

    private fun setupRecyclerView() {
        binding.rvFollow.layoutManager = LinearLayoutManager(context)
        // 어댑터는 LiveData 관찰 시 설정
    }

    private fun setupObservers() {
        // 내 아이디 관찰
        memberViewModel.myId.observe(viewLifecycleOwner) { myId ->
            myId?.let {
                // 내 아이디가 로드되면 팔로우 목록을 다시 로드 (follow/unfollow 후에도 갱신되도록)
                loadFollowData()
            }
        }

        // 팔로잉 목록 관찰
        memberViewModel.followingList.observe(viewLifecycleOwner) { followList ->
            if (mode == MODE_FOLLOWING) {
                updateFollowList(followList)
            }
        }

        // 팔로워 목록 관찰
        memberViewModel.followerList.observe(viewLifecycleOwner) { followList ->
            if (mode == MODE_FOLLOWER) {
                updateFollowList(followList)
            }
        }

        // 에러 관찰
        memberViewModel.error.observe(viewLifecycleOwner) { errorMessage ->
            errorMessage?.let {
                Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
                Log.e(TAG, "팔로우 목록 로드 에러: $it")
            }
        }
    }

    private fun loadFollowData() {
        // 내 아이디 먼저 로드 요청
        memberViewModel.loadMyId()

        if (mode == MODE_FOLLOWING) {
            memberViewModel.loadFollowingList(targetMemberId)
        } else {
            memberViewModel.loadFollowerList(targetMemberId)
        }
    }

    private fun updateFollowList(followList: List<FollowItem>) {
        val currentUserId = memberViewModel.myId.value ?: -1 // 현재 사용자 ID 가져오기

        binding.rvFollow.adapter = FollowListAdapter(
            followList,
            currentUserId,
            onFollowChanged = {
                loadFollowData()
            },
            onItemClick = { selectedItem ->
                val memberId = selectedItem.targetMemberId
                val intent = Intent(requireContext(), ToolbarActivity::class.java).apply {
                    putExtra("mode", 13)
                    putExtra("memberId", memberId)
                    Log.d("Memberid", memberId.toString())
                }
                startActivity(intent)
            }
        )
        Log.d(TAG, "팔로우 목록 어댑터 업데이트 완료: ${followList.size}개")
        binding.rvFollow.visibility = if (followList.isEmpty()) View.GONE else View.VISIBLE
        //binding.tvNoFollow.visibility = if (followList.isEmpty()) View.VISIBLE else View.GONE
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}