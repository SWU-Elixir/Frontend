package com.example.elixir.member.ui

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.example.elixir.R
import com.example.elixir.databinding.FragmentMypageImageGridBinding
import com.example.elixir.RetrofitClient
import com.example.elixir.member.data.BadgeItem
import com.example.elixir.recipe.ui.fragment.RecipeDetailFragment
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
        when (contentType) {
            TYPE_RECIPE -> {
                setupNormalLayout()
                binding.rvImage.layoutManager = GridLayoutManager(context, 3)
                loadMyRecipesAsync()
            }
            TYPE_SCRAP -> {
                setupNormalLayout()
                binding.rvImage.layoutManager = GridLayoutManager(context, 3)
                loadMyScrapsAsync()
            }
            TYPE_BADGE -> {
                setupBadgeLayout()
                loadBadgesAsync()
            }
        }
    }

    private fun setupNormalLayout() {
        // 일반 레시피/스크랩용 레이아웃
        binding.rvImage.visibility = View.VISIBLE
        binding.layoutChallengeSection.visibility = View.GONE
        binding.layoutAchievementSection.visibility = View.GONE
    }

    private fun setupBadgeLayout() {
        // 뱃지용 분할 레이아웃
        binding.rvImage.visibility = View.GONE
        binding.layoutChallengeSection.visibility = View.VISIBLE
        binding.layoutAchievementSection.visibility = View.VISIBLE

        // 각 RecyclerView의 LayoutManager 설정
        binding.rvChallengeeBadges.layoutManager = GridLayoutManager(context, 3)
        binding.rvAchievementBadges.layoutManager = GridLayoutManager(context, 3)
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
                    binding.rvImage.adapter = BadgeGridAdapter(emptyList())
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
                        val (recipeId, imageUrl) = recipeList[position]

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
                                .into(holder.binding.imgBadge)
                        } else {
                            // 이미지 URL이 null이거나 비어있는 경우 기본 이미지 표시
                            holder.binding.imgBadge.setImageResource(R.drawable.ic_recipe_white)
                        }

                        holder.binding.tvBadge.visibility = View.GONE
                        holder.binding.tvBadgeSubTitle.visibility = View.GONE

                        // 전체 아이템 클릭 이벤트 추가
                        holder.binding.root.setOnClickListener {
                            Log.d("RecipeAdapter", "아이템 클릭됨: $recipeId")
                            val detailFragment = RecipeDetailFragment().apply {
                                arguments = Bundle().apply {
                                    putInt("recipeId", recipeId)
                                }
                            }
                            // fragment_overlay를 visible로!
                            activity?.findViewById<View>(R.id.fragment_overlay)?.visibility = View.VISIBLE

                            parentFragmentManager.beginTransaction()
                                .add(R.id.fragment_overlay, detailFragment)
                                .addToBackStack(null)
                                .commit()
                        }
                    }

                    override fun getItemCount() = recipeList.size
                }

                binding.rvImage.adapter = adapter
                Log.d(TAG, "레시피 목록 로드 완료: ${recipeList.size}개")
            } catch (e: Exception) {
                Log.e(TAG, "레시피 목록 로드 실패", e)
                binding.rvImage.adapter = BadgeGridAdapter(emptyList())
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
                    binding.rvImage.adapter = BadgeGridAdapter(emptyList())
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
                        val (recipeId, imageUrl) = recipeList[position]

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
                                .into(holder.binding.imgBadge)
                        } else {
                            // 이미지 URL이 null이거나 비어있는 경우 기본 이미지 표시
                            holder.binding.imgBadge.setImageResource(R.drawable.ic_recipe_white)
                        }

                        holder.binding.tvBadge.visibility = View.GONE
                        holder.binding.tvBadgeSubTitle.visibility = View.GONE

                        // 전체 아이템 클릭 이벤트 추가
                        holder.binding.root.setOnClickListener {
                            Log.d("RecipeAdapter", "아이템 클릭됨: $recipeId")
                            val detailFragment = RecipeDetailFragment().apply {
                                arguments = Bundle().apply {
                                    putInt("recipeId", recipeId)
                                }
                            }
                            // fragment_overlay를 visible로!
                            activity?.findViewById<View>(R.id.fragment_overlay)?.visibility = View.VISIBLE

                            parentFragmentManager.beginTransaction()
                                .add(R.id.fragment_overlay, detailFragment)
                                .addToBackStack(null)
                                .commit()
                        }
                    }

                    override fun getItemCount() = recipeList.size
                }

                binding.rvImage.adapter = adapter
                Log.d(TAG, "스크랩 목록 로드 완료: ${recipeList.size}개")
            } catch (e: Exception) {
                Log.e(TAG, "스크랩 목록 로드 실패", e)
                binding.rvImage.adapter = BadgeGridAdapter(emptyList())
            }
        }
    }

    private fun createRecipeAdapter(recipeList: List<Pair<Int, String?>>): androidx.recyclerview.widget.RecyclerView.Adapter<BadgeViewHolder> {
        return object : androidx.recyclerview.widget.RecyclerView.Adapter<BadgeViewHolder>() {
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BadgeViewHolder {
                val binding = com.example.elixir.databinding.ItemMypageBadgeGridBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
                return BadgeViewHolder(binding)
            }

            override fun onBindViewHolder(holder: BadgeViewHolder, position: Int) {
                val (recipeId, imageUrl) = recipeList[position]

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
                        .into(holder.binding.imgBadge)
                } else {
                    // 이미지 URL이 null이거나 비어있는 경우 기본 이미지 표시
                    holder.binding.imgBadge.setImageResource(R.drawable.ic_recipe_white)
                }

                holder.binding.tvBadge.visibility = View.GONE
                holder.binding.tvBadgeSubTitle.visibility = View.GONE

                // 전체 아이템 클릭 이벤트 추가
                holder.binding.root.setOnClickListener {
                    Log.d("RecipeAdapter", "아이템 클릭됨: $recipeId")
                    val detailFragment = RecipeDetailFragment().apply {
                        arguments = Bundle().apply {
                            putInt("recipeId", recipeId)
                        }
                    }
                    // fragment_overlay를 visible로!
                    activity?.findViewById<View>(R.id.fragment_overlay)?.visibility = View.VISIBLE

                    parentFragmentManager.beginTransaction()
                        .add(R.id.fragment_overlay, detailFragment)
                        .addToBackStack(null)
                        .commit()
                }
            }

            override fun getItemCount() = recipeList.size
        }
    }

    private fun loadBadgesAsync() {
        lifecycleScope.launch {
            try {
                val api = RetrofitClient.instanceMemberApi
                Log.d(TAG, "뱃지 로드 시작 - memberId: $member")

                // 1. 챌린지 업적 데이터 가져오기
                val challengeResponse = if (member != -1) {
                    Log.d(TAG, "특정 사용자의 챌린지 업적 목록 요청 - memberId: $member")
                    api.getChallenges(member)
                } else {
                    Log.d(TAG, "현재 사용자의 챌린지 업적 목록 요청")
                    api.getChallenges()
                }

                val challengeBadgeItems = if (challengeResponse.status == 200 && challengeResponse.data != null) {
                    challengeResponse.data.map {
                        // 챌린지 업적 데이터에 연도/월 정보를 subtitle로 추가
                        BadgeItem(
                            imageRes = 0,
                            title = it.achievementName ?: "알 수 없음",
                            year = it.year,
                            month = it.month,
                            subtitle = "${it.year}년 ${it.month}월 챌린지" // 챌린지 서브타이틀
                        ) to (it.achievementImageUrl ?: "")
                    }
                } else {
                    Log.e(TAG, "챌린지 업적 API 응답 실패: ${challengeResponse.message}")
                    emptyList()
                }

                // 2. 업적 데이터 가져오기
                val achievementResponse = api.getAchievements()

                val achievementBadgeItems = if (achievementResponse.status == 200 && achievementResponse.data != null) {
                    achievementResponse.data.map {
                        // 순수 업적 데이터에 고정된 subtitle 추가
                        BadgeItem(
                            imageRes = 0,
                            title = it.achievementName ?: "알 수 없음",
                            year = 0,
                            month = 0,
                            subtitle = it.description
                        ) to (it.achievementImageUrl ?: "")
                    }
                } else {
                    Log.e(TAG, "순수 업적 API 응답 실패: ${achievementResponse.message}")
                    emptyList()
                }

                // 3. 각각의 RecyclerView에 어댑터 설정
                setupChallengeRecyclerView(challengeBadgeItems)
                setupAchievementRecyclerView(achievementBadgeItems)

                Log.d(TAG, "뱃지 로드 완료 - 챌린지: ${challengeBadgeItems.size}개, 업적: ${achievementBadgeItems.size}개")
            } catch (e: Exception) {
                Log.e(TAG, "뱃지/업적 로드 실패", e)
                e.printStackTrace()
                // 에러 시 빈 어댑터 설정
                binding.rvChallengeeBadges.adapter = BadgeGridAdapter(emptyList())
                binding.rvAchievementBadges.adapter = BadgeGridAdapter(emptyList())
            }
        }
    }

    private fun setupChallengeRecyclerView(challengeBadgeItems: List<Pair<BadgeItem, String>>) {
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
                val (item, imageUrl) = challengeBadgeItems[position]

                if (imageUrl.isNotEmpty()) {
                    Glide.with(holder.binding.root.context)
                        .load(imageUrl)
                        .placeholder(R.drawable.bg_badge_empty)
                        .error(R.drawable.bg_badge_empty)
                        .into(holder.binding.imgBadge)
                } else {
                    holder.binding.imgBadge.setImageResource(R.drawable.bg_badge_empty)
                    Log.w(TAG, "이미지 URL이 없습니다: ${item.title}")
                }

                holder.binding.tvBadge.text = item.title
                holder.binding.tvBadge.visibility = View.VISIBLE

                // subtitle 필드가 있을 경우에만 표시
                if (!item.subtitle.isNullOrBlank()) {
                    holder.binding.tvBadgeSubTitle.text = item.subtitle
                    holder.binding.tvBadgeSubTitle.visibility = View.VISIBLE
                } else {
                    holder.binding.tvBadgeSubTitle.visibility = View.GONE
                }
            }

            override fun getItemCount() = challengeBadgeItems.size
        }

        binding.rvChallengeeBadges.adapter = adapter
    }

    private fun setupAchievementRecyclerView(achievementBadgeItems: List<Pair<BadgeItem, String>>) {
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
                val (item, imageUrl) = achievementBadgeItems[position]

                if (imageUrl.isNotEmpty()) {
                    Glide.with(holder.binding.root.context)
                        .load(imageUrl)
                        .placeholder(R.drawable.bg_badge_empty)
                        .error(R.drawable.bg_badge_empty)
                        .into(holder.binding.imgBadge)
                } else {
                    holder.binding.imgBadge.setImageResource(R.drawable.bg_badge_empty)
                    Log.w(TAG, "이미지 URL이 없습니다: ${item.title}")
                }

                holder.binding.tvBadge.text = item.title
                holder.binding.tvBadge.visibility = View.VISIBLE

                // subtitle 필드가 있을 경우에만 표시
                if (!item.subtitle.isNullOrBlank()) {
                    holder.binding.tvBadgeSubTitle.text = item.subtitle
                    holder.binding.tvBadgeSubTitle.visibility = View.VISIBLE
                } else {
                    holder.binding.tvBadgeSubTitle.visibility = View.GONE
                }
            }

            override fun getItemCount() = achievementBadgeItems.size
        }

        binding.rvAchievementBadges.adapter = adapter
    }

    class BadgeViewHolder(val binding: com.example.elixir.databinding.ItemMypageBadgeGridBinding) : androidx.recyclerview.widget.RecyclerView.ViewHolder(binding.root)

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}