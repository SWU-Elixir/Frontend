package com.example.elixir.member

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.bumptech.glide.Glide
import com.example.elixir.R
import com.example.elixir.databinding.FragmentMypageImageGridBinding
import com.example.elixir.RetrofitClient
import kotlinx.coroutines.launch

class MyPageImageGridFragment : Fragment() {
    private var _binding: FragmentMypageImageGridBinding? = null
    private val binding get() = _binding!!
    
    private var contentType: Int = 0 // 0: 레시피, 1: 스크랩, 2: 뱃지

    companion object {
        const val TYPE_RECIPE = 0
        const val TYPE_SCRAP = 1
        const val TYPE_BADGE = 2

        fun newInstance(type: Int): MyPageImageGridFragment {
            return MyPageImageGridFragment().apply {
                contentType = type
            }
        }
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
                val response = api.getMyRecipes()
                val recipeList = response.data
                val adapter = object : androidx.recyclerview.widget.RecyclerView.Adapter<RecipeViewHolder>() {
                    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecipeViewHolder {
                        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_mypage_badge_grid, parent, false)
                        return RecipeViewHolder(view)
                    }
                    override fun onBindViewHolder(holder: RecipeViewHolder, position: Int) {
                        val item = recipeList[position]
                        Glide.with(holder.itemView).load(item.imageUrl).into(holder.itemView.findViewById(R.id.badgeImage))
                        holder.itemView.findViewById<TextView>(R.id.badgeTitle).text = ""
                        holder.itemView.findViewById<TextView>(R.id.badgeSubtitle).text = ""
                    }
                    override fun getItemCount() = recipeList.size
                }
                binding.imageRecyclerView.adapter = adapter
            } catch (e: Exception) {
                e.printStackTrace()
                binding.imageRecyclerView.adapter = BadgeGridAdapter(emptyList())
            }
        }
    }

    private fun loadMyScrapsAsync() {
        lifecycleScope.launch {
            try {
                val api = RetrofitClient.instanceMemberApi
                val response = api.getScrapRecipes()
                val recipeList = response.data
                val adapter = object : androidx.recyclerview.widget.RecyclerView.Adapter<RecipeViewHolder>() {
                    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecipeViewHolder {
                        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_mypage_badge_grid, parent, false)
                        return RecipeViewHolder(view)
                    }
                    override fun onBindViewHolder(holder: RecipeViewHolder, position: Int) {
                        val item = recipeList[position]
                        Glide.with(holder.itemView).load(item.imageUrl).into(holder.itemView.findViewById(R.id.badgeImage))
                        holder.itemView.findViewById<TextView>(R.id.badgeTitle).text = ""
                        holder.itemView.findViewById<TextView>(R.id.badgeSubtitle).text = ""
                    }
                    override fun getItemCount() = recipeList.size
                }
                binding.imageRecyclerView.adapter = adapter
            } catch (e: Exception) {
                e.printStackTrace()
                binding.imageRecyclerView.adapter = BadgeGridAdapter(emptyList())
            }
        }
    }

    private fun loadBadgesAsync() {
        lifecycleScope.launch {
            try {
                val api = RetrofitClient.instanceMemberApi
                val response = api.getAchievements()
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

    class RecipeViewHolder(view: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(view)
    class BadgeViewHolder(val binding: com.example.elixir.databinding.ItemMypageBadgeGridBinding) : androidx.recyclerview.widget.RecyclerView.ViewHolder(binding.root)

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}