package com.example.elixir.member

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.elixir.databinding.FragmentMypageFollowListBinding
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.launch

class MypageFollowListFragment : Fragment() {
    private var _binding: FragmentMypageFollowListBinding? = null
    private val binding get() = _binding!!

    private var mode: Int = MODE_FOLLOWER // 기본값
    private var member: Int = -1  // 초기값을 -1로 변경

    companion object {
        private const val TAG = "MypageFollowListFragment"
        const val MODE_FOLLOWER = 0
        const val MODE_FOLLOWING = 1

        fun newInstance(mode: Int, memberId: Int = -1): MypageFollowListFragment {
            Log.d(TAG, "newInstance - mode: $mode, memberId: $memberId")  // 로그 추가
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
        member = arguments?.getInt("memberId", -1) ?: -1
        Log.d(TAG, "onCreate - mode: $mode, member: $member")  // 로그 추가
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
    }

    private fun setupRecyclerView() {
        binding.recyclerView.layoutManager = LinearLayoutManager(context)

        lifecycleScope.launch {
            try {
                val api = com.example.elixir.RetrofitClient.instanceMemberApi
                val myIdApi = api.getProfile()
                val id = myIdApi.data.id
                Log.d(TAG, "setupRecyclerView - mode: $mode, member: $member")  // 로그 추가

                // 내가 팔로우하는 사람들의 ID 목록
                val myFollowingResponse = api.getFollowing()
                val myFollowingIds = myFollowingResponse.data.map { it.id ?: it.followId }.toSet()

                // 현재 모드에 따른 목록 요청
                val response = if (mode == MODE_FOLLOWING) {
                    if (member != -1) {
                        Log.d(TAG, "특정 사용자의 팔로잉 목록 요청 - memberId: $member")  // 로그 추가
                        api.getFollowing(member)
                    } else {
                        Log.d(TAG, "현재 사용자의 팔로잉 목록 요청")  // 로그 추가
                        api.getFollowing()
                    }
                } else {
                    if (member != -1) {
                        Log.d(TAG, "특정 사용자의 팔로워 목록 요청 - memberId: $member")  // 로그 추가
                        api.getFollower(member)
                    } else {
                        Log.d(TAG, "현재 사용자의 팔로워 목록 요청")  // 로그 추가
                        api.getFollower()
                    }
                }
                val followList = response.data.map {
                    FollowItem(
                        followId = it.followId,
                        targetMemberId = it.id ?: it.followId,
                        profileImageRes = it.profileUrl ?: "",
                        memberTitle = it.title,
                        memberNickname = it.nickname ?: "알 수 없음",
                        isFollowing = myFollowingIds.contains(it.id ?: it.followId)
                    )
                }

                binding.recyclerView.adapter = FollowListAdapter(
                    followList,
                    id, // 내 아이디 전달
                    onFollowChanged = { setupRecyclerView() },
                    onItemClick = { selectedItem ->
                        val memberId = selectedItem.targetMemberId
                        val intent = Intent(requireContext(), com.example.elixir.ToolbarActivity::class.java).apply {
                            putExtra("mode", 13)
                            putExtra("memberId", memberId)
                            Log.d("Memberid", memberId.toString())
                        }
                        startActivity(intent)
                    }
                )

                Log.d(TAG, "팔로우 목록 로드 완료: ${followList.size}개")
            } catch (e: Exception) {
                Log.e(TAG, "팔로우 목록 로드 실패", e)
                binding.recyclerView.adapter = FollowListAdapter(emptyList(), -1)
            }
        }
    }

    override fun onDestroyView() {
        try {
            // Fragment가 destroy될 때 코루틴 작업 취소
            viewLifecycleOwner.lifecycleScope.coroutineContext.cancelChildren()
        } catch (e: Exception) {
            Log.e(TAG, "코루틴 취소 실패", e)
        } finally {
            super.onDestroyView()
            _binding = null
        }
    }
}
