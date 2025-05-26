package com.example.elixir.member

import android.os.Bundle
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
                val followList = when (mode) {
                    MODE_FOLLOWING -> {
                        val response = api.getFollowing()
                        response.data.map { FollowItem(R.drawable.ic_profile, it.title, it.nickname) }
                    }
                    else -> {
                        val response = api.getFollower()
                        response.data.map { FollowItem(R.drawable.ic_profile, it.title, it.nickname) }
                    }
                }
                binding.recyclerView.adapter = FollowListAdapter(followList)
            } catch (e: Exception) {
                e.printStackTrace()
                binding.recyclerView.adapter = FollowListAdapter(emptyList())
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}