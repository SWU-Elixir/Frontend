package com.example.elixir

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import com.example.elixir.databinding.FragmentMypageImageGridBinding

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
            TYPE_RECIPE, TYPE_SCRAP -> {
                val images = if (contentType == TYPE_RECIPE) loadMyRecipes() else loadMyScraps()
                val imageGridAdapter = ImageGridAdapter(images)
                binding.imageRecyclerView.adapter = imageGridAdapter
            }
            TYPE_BADGE -> {
                val badges = loadBadges()
                val badgeGridAdapter = BadgeGridAdapter(badges)
                binding.imageRecyclerView.adapter = badgeGridAdapter
            }
        }
    }

    private fun loadMyRecipes(): List<Int> {
        // TODO: 레시피 데이터 로드
        return listOf(
            R.drawable.img_blank,
            R.drawable.img_blank,
            R.drawable.img_blank,
            R.drawable.img_blank
        )
    }

    private fun loadMyScraps(): List<Int> {
        // TODO: 스크랩 데이터 로드
        return listOf(
            R.drawable.img_blank,
            R.drawable.img_blank,
            R.drawable.img_blank,
            R.drawable.img_blank
        )
    }

    private fun loadBadges(): List<BadgeItem> {
        // TODO: 뱃지 데이터 로드
        return listOf(
            BadgeItem(R.drawable.png_badge, "봄나물 마스터", 2025, 3),
            BadgeItem(R.drawable.bg_badge_empty, "봄나물 마스터", 2025, 3),
            BadgeItem(R.drawable.bg_badge_empty, "봄나물 마스터", 2025, 3)
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}