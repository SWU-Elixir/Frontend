package com.example.elixir.member

import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.elixir.R
import com.example.elixir.databinding.FragmentMypageFollowListBinding
import kotlinx.coroutines.launch

class MypageFollowListFragment : Fragment() {
    private var _binding: FragmentMypageFollowListBinding? = null
    private val binding get() = _binding!!
    private var mode: Int = 0 // 0: follower, 1: following

    companion object {
        private const val TAG = "MypageFollowListFragment"
        const val MODE_FOLLOWER = 0
        const val MODE_FOLLOWING = 1
        fun newInstance(mode: Int): MypageFollowListFragment {
            return MypageFollowListFragment().apply {
                this.mode = mode
            }
        }
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
                // 1. 내가 팔로우하는 사람들의 id set 만들기
                val myFollowingResponse = api.getFollowing()
                val myFollowingIds = myFollowingResponse.data.map { it.id ?: it.followId }.toSet()

                // 2. 팔로워/팔로잉 리스트 불러오기
                val followList = when (mode) {
                    MODE_FOLLOWING -> {
                        val response = api.getFollower()
                        Log.d(TAG, "팔로잉 목록 응답: ${response.data}")
                        response.data.map {
                            FollowItem(
                                followId = it.followId,
                                targetMemberId = it.id ?: it.followId,
                                profileImageRes = it.profileUrl ?: "",
                                memberTitle = it.title,
                                memberNickname = it.nickname ?: "알 수 없음",
                                isFollowing = myFollowingIds.contains(it.id ?: it.followId)
                            )
                        }
                    }
                    else -> {
                        val response = api.getFollowing()
                        Log.d(TAG, "팔로워 목록 응답: ${response.data}")
                        response.data.map {
                            FollowItem(
                                followId = it.followId,
                                targetMemberId = it.id ?: it.followId,
                                profileImageRes = it.profileUrl ?: "",
                                memberTitle = it.title,
                                memberNickname = it.nickname ?: "알 수 없음",
                                isFollowing = myFollowingIds.contains(it.id ?: it.followId)
                            )
                        }
                    }
                }
                binding.recyclerView.adapter = FollowListAdapter(followList) {
                    setupRecyclerView()
                }
                Log.d(TAG, "팔로우 목록 로드 완료: ${followList.size}개")
            } catch (e: Exception) {
                Log.e(TAG, "팔로우 목록 로드 실패", e)
                binding.recyclerView.adapter = FollowListAdapter(emptyList())
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}