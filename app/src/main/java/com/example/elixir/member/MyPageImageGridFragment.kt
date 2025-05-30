package com.example.elixir.member

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.example.elixir.R
import com.example.elixir.databinding.FragmentMypageImageGridBinding
import com.example.elixir.RetrofitClient
import kotlinx.coroutines.launch

class MyPageImageGridFragment : Fragment() {
    private var _binding: FragmentMypageImageGridBinding? = null
    private val binding get() = _binding!!
    
    private var contentType: Int = 0 // 0: 레시피, 1: 스크랩, 2: 뱃지
    private var member: Int = -1

    companion object {
        private const val TAG = "MyPageImageGridFragment"
        const val TYPE_RECIPE = 0
        const val TYPE_SCRAP = 1
        const val TYPE_BADGE = 2

        fun newInstance(type: Int, memberId: Int = -1): MyPageImageGridFragment {
            Log.d(TAG, "newInstance - type: $type, memberId: $memberId")
            return MyPageImageGridFragment().apply {
                arguments = Bundle().apply {
                    putInt("type", type)
                    putInt("memberId", memberId)
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        contentType = arguments?.getInt("type", TYPE_RECIPE) ?: TYPE_RECIPE
        member = arguments?.getInt("memberId", -1) ?: -1
        Log.d(TAG, "onCreate - contentType: $contentType, member: $member")
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMypageImageGridBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
    }

    private fun setupRecyclerView() {
        binding.imageRecyclerView.layoutManager = GridLayoutManager(context, 3)
        when (contentType) {
            TYPE_RECIPE -> {
                loadMyRecipesAsync()
            }
            TYPE_SCRAP -> {
                loadMyScrapsAsync()
            }
            TYPE_BADGE -> {
                loadBadgesAsync()
            }
        }
    }

    private fun loadMyRecipesAsync() {
        lifecycleScope.launch {
            try {
                val api = RetrofitClient.instanceMemberApi
                Log.d(TAG, "레시피 로드 시작 - memberId: $member")
                val response = if (member != -1) {
                    Log.d(TAG, "특정 사용자의 레시피 목록 요청 - memberId: $member")
                    api.getMyRecipes(member)
                } else {
                    Log.d(TAG, "현재 사용자의 레시피 목록 요청")
                    api.getMyRecipes()
                }
                val recipeList = response.data
                
                if (recipeList.isNullOrEmpty()) {
                    Log.d(TAG, "레시피 목록이 비어있습니다")
                    binding.imageRecyclerView.adapter = BadgeGridAdapter(emptyList())
                    return@launch
                }

                val adapter = object : androidx.recyclerview.widget.RecyclerView.Adapter<BadgeViewHolder>() {
                    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BadgeViewHolder {
                        val binding = com.example.elixir.databinding.ItemMypageBadgeGridBinding.inflate(
                            LayoutInflater.from(parent.context), 
                            parent, 
                            false
                        )
                        return BadgeViewHolder(binding)
                    }

                    override fun onBindViewHolder(holder: BadgeViewHolder, position: Int) {
                        val (item, imageUrl) = recipeList[position]
                        
                        // Glide 옵션 설정
                        val requestOptions = RequestOptions()
                            .diskCacheStrategy(DiskCacheStrategy.ALL)
                            .placeholder(R.drawable.ic_recipe_white)
                            .error(R.drawable.ic_recipe_white)

                        // 이미지 로드
                        if (!imageUrl.isNullOrEmpty()) {
                            Glide.with(holder.binding.root)
                                .load(imageUrl)
                                .centerCrop()
                                .apply(requestOptions)
                                .into(holder.binding.badgeImage)
                        } else {
                            // 이미지 URL이 null이거나 비어있는 경우 기본 이미지 표시
                            holder.binding.badgeImage.setImageResource(R.drawable.ic_recipe_white)
                        }

                        holder.binding.badgeTitle.visibility = View.GONE
                        holder.binding.badgeSubtitle.visibility = View.GONE
                    }

                    override fun getItemCount() = recipeList.size
                }
                
                binding.imageRecyclerView.adapter = adapter
                Log.d(TAG, "레시피 목록 로드 완료: ${recipeList.size}개")
            } catch (e: Exception) {
                Log.e(TAG, "레시피 목록 로드 실패", e)
                binding.imageRecyclerView.adapter = BadgeGridAdapter(emptyList())
            }
        }
    }

    private fun loadMyScrapsAsync() {
        lifecycleScope.launch {
            try {
                val api = RetrofitClient.instanceMemberApi
                Log.d(TAG, "스크랩 로드 - memberId: $member")
                // 스크랩은 항상 현재 사용자의 것만 가져옴
                val response = api.getScrapRecipes()
                val recipeList = response.data
                
                if (recipeList.isNullOrEmpty()) {
                    Log.d(TAG, "스크랩 목록이 비어있습니다")
                    binding.imageRecyclerView.adapter = BadgeGridAdapter(emptyList())
                    return@launch
                }

                val adapter = object : androidx.recyclerview.widget.RecyclerView.Adapter<BadgeViewHolder>() {
                    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BadgeViewHolder {
                        val binding = com.example.elixir.databinding.ItemMypageBadgeGridBinding.inflate(
                            LayoutInflater.from(parent.context), 
                            parent, 
                            false
                        )
                        return BadgeViewHolder(binding)
                    }

                    override fun onBindViewHolder(holder: BadgeViewHolder, position: Int) {
                        val (item, imageUrl) = recipeList[position]
                        
                        // Glide 옵션 설정
                        val requestOptions = RequestOptions()
                            .diskCacheStrategy(DiskCacheStrategy.ALL)
                            .placeholder(R.drawable.ic_recipe_white)
                            .error(R.drawable.ic_recipe_white)

                        // 이미지 로드
                        if (!imageUrl.isNullOrEmpty()) {
                            Glide.with(holder.binding.root)
                                .load(imageUrl)
                                .centerCrop()
                                .apply(requestOptions)
                                .into(holder.binding.badgeImage)
                        } else {
                            // 이미지 URL이 null이거나 비어있는 경우 기본 이미지 표시
                            holder.binding.badgeImage.setImageResource(R.drawable.ic_recipe_white)
                        }

                        holder.binding.badgeTitle.visibility = View.GONE
                        holder.binding.badgeSubtitle.visibility = View.GONE
                    }

                    override fun getItemCount() = recipeList.size
                }
                
                binding.imageRecyclerView.adapter = adapter
                Log.d(TAG, "스크랩 목록 로드 완료: ${recipeList.size}개")
            } catch (e: Exception) {
                Log.e(TAG, "스크랩 목록 로드 실패", e)
                binding.imageRecyclerView.adapter = BadgeGridAdapter(emptyList())
            }
        }
    }

    private fun loadBadgesAsync() {
        lifecycleScope.launch {
            try {
                val api = RetrofitClient.instanceMemberApi
                Log.d(TAG, "뱃지 로드 시작 - memberId: $member")
                val response = if (member != -1) {
                    Log.d(TAG, "특정 사용자의 뱃지 목록 요청 - memberId: $member")
                    api.getAchievements(member)
                } else {
                    Log.d(TAG, "현재 사용자의 뱃지 목록 요청")
                    api.getAchievements()
                }
                val badgeList = response.data.map {
                    BadgeItem(
                        imageRes = 0, // Glide로 imageUrl 사용
                        title = it.achievementName ?: "알 수 없음",
                        year = it.year,
                        month = it.month
                    ) to it.achievementImageUrl
                }
                val adapter = object : androidx.recyclerview.widget.RecyclerView.Adapter<BadgeViewHolder>() {
                    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BadgeViewHolder {
                        val binding = com.example.elixir.databinding.ItemMypageBadgeGridBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                        return BadgeViewHolder(binding)
                    }
                    override fun onBindViewHolder(holder: BadgeViewHolder, position: Int) {
                        val (item, imageUrl) = badgeList[position]
                        Glide.with(holder.binding.root).load(imageUrl).into(holder.binding.badgeImage)
                        holder.binding.badgeTitle.text = item.title
                        holder.binding.badgeSubtitle.text = "${item.year}년 ${item.month}월 챌린지 성공"
                    }
                    override fun getItemCount() = badgeList.size
                }
                binding.imageRecyclerView.adapter = adapter
            } catch (e: Exception) {
                e.printStackTrace()
                binding.imageRecyclerView.adapter = BadgeGridAdapter(emptyList())
            }
        }
    }

    class BadgeViewHolder(val binding: com.example.elixir.databinding.ItemMypageBadgeGridBinding) : androidx.recyclerview.widget.RecyclerView.ViewHolder(binding.root)

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}