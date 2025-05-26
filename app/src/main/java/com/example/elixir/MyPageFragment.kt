package com.example.elixir

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.TypedValue
import android.view.*
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.elixir.databinding.FragmentMypageBinding
import com.example.elixir.dialog.LogoutDialog

class MyPageFragment : Fragment() {

    private var myPageBinding: FragmentMypageBinding? = null
    private val binding get() = myPageBinding!!

    private val spanCount = 3 // 한 줄에 3개
    private val spacing = 16 // dp → px로 변환 필요

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        myPageBinding = FragmentMypageBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // --------------------------- RecyclerView에 사용할 더미 데이터 설정 --------------------------- //
        // 뱃지 테스트 소스
        val badgeList = listOf(
            Uri.parse("android.resource://${context?.packageName}/${R.drawable.bg_badge_empty}"),
            Uri.parse("android.resource://${context?.packageName}/${R.drawable.bg_badge_empty}"),
            Uri.parse("android.resource://${context?.packageName}/${R.drawable.bg_badge_empty}")
        )

        // 내 레시피 테스트 소스
        val recipeList = listOf(
            Uri.parse("android.resource://${context?.packageName}/${R.drawable.img_blank}"),
            Uri.parse("android.resource://${context?.packageName}/${R.drawable.img_blank}"),
            Uri.parse("android.resource://${context?.packageName}/${R.drawable.img_blank}")
        )

        // 내 스크랩 테스트 소스
        val scrapList = listOf(
            Uri.parse("android.resource://${context?.packageName}/${R.drawable.img_blank}"),
            Uri.parse("android.resource://${context?.packageName}/${R.drawable.img_blank}"),
            Uri.parse("android.resource://${context?.packageName}/${R.drawable.img_blank}")
        )
        // ------------------------------------------------------------------------------------------ //

        // ------------------------------------ RecyclerView 설정 ------------------------------------ //
        // 뱃지 RecyclerView 설정
        binding.mypageBadgeGrid.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        binding.mypageBadgeGrid.adapter = MyPageCollectionAdapter(badgeList, true)

        // 내 레시피 RecyclerView 설정
        binding.mypageRecipeGrid.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        binding.mypageRecipeGrid.adapter = MyPageCollectionAdapter(recipeList, false)

        // 내 스크랩 RecyclerView 설정
        binding.mypageScrapGrid.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        binding.mypageScrapGrid.adapter = MyPageCollectionAdapter(scrapList, false)

        // ---------------------------------- RecyclerView 간격 설정 ---------------------------------- //
        val spacingPx = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, spacing.toFloat(), resources.displayMetrics).toInt()

        // RecyclerView 아이템 간격 설정
        binding.mypageBadgeGrid.addItemDecoration(GridItemDecoration(spanCount, spacingPx, 16))
        binding.mypageRecipeGrid.addItemDecoration(GridItemDecoration(spanCount, spacingPx, 16))
        binding.mypageScrapGrid.addItemDecoration(GridItemDecoration(spanCount, spacingPx, 16))
        // ------------------------------------------------------------------------------------------ //

        // 프로필 수정 버튼 클릭 시 회원가입 페이지로 넘어가게
        binding.btnProfileEdit.setOnClickListener {
            val intent = Intent(context, ToolbarActivity::class.java).apply {
                putExtra("mode", 10)
                putExtra("nickname", "닉네임")
            }
            startActivity(intent)
        }

        // 로그아웃 버튼
        binding.btnLogout.setOnClickListener {
            LogoutDialog(requireActivity()).show()
        }

        // 팔로워 클릭 시 팔로워 목록 페이지로 넘어가게
        binding.textFollower.setOnClickListener {
            val intent = Intent(context, ToolbarActivity::class.java).apply {
                putExtra("mode", 8)
                putExtra("title", "팔로우 목록")
            }
            startActivity(intent)
        }

        // ------------------------------------ 더보기 클릭 이벤트 설정 --------------------------------- //
        // 내 레시피 더보기 클릭 시 내 레시피 페이지로 넘어가게
        binding.btnMoreRecipe.setOnClickListener {
            val intent = Intent(context, ToolbarActivity::class.java).apply {
                putExtra("mode", 5)
                putExtra("title", "내 레시피")
            }
            startActivity(intent)
        }

        // 내 스크랩 더보기 클릭 시 내 스크랩 페이지로 넘어가게
        binding.btnMoreScrap.setOnClickListener {
            val intent = Intent(context, ToolbarActivity::class.java).apply {
                putExtra("mode", 6)
                putExtra("title", "내 스크랩")
            }
            startActivity(intent)
        }

        // 내 뱃지 더보기 클릭 시 내 뱃지 페이지로 넘어가게
        binding.btnMoreBadge.setOnClickListener {
            val intent = Intent(context, ToolbarActivity::class.java).apply {
                putExtra("mode", 7)
                putExtra("title", "내 뱃지")
            }
            startActivity(intent)
        }
        // ------------------------------------------------------------------------------------------ //
    }

    override fun onDestroyView() {
        super.onDestroyView()
        myPageBinding = null
    }
}
