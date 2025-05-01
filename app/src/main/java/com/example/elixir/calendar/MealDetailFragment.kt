package com.example.elixir.calendar

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.elixir.R
import com.example.elixir.databinding.FragmentMealDetailBinding
import com.example.elixir.recipe.RecipeTagAdapter
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.flexbox.JustifyContent
import java.math.BigInteger

class MealDetailFragment : Fragment() {

    private var _binding: FragmentMealDetailBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMealDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // ------------------------ 2. 더미 데이터 적용 ------------------------
        val dummyData = MealPlanData(
            id = BigInteger("1001"),
            memberId = BigInteger("1"),
            name = "연어 아보카도 샐러드",
            imageUrl = R.drawable.png_recipe_sample,
            createdAt = "2025-03-29",
            mealtimes = "아침",
            score = 5,
            mealPlanIngredients = listOf("연어", "아보카도", "올리브유", "잣")
        )

        // ------------------------ 3. 데이터 바인딩 ------------------------
        binding.mealTitle.text = dummyData.name

        // 색상 및 배경 drawable 준비
        val whiteColor = ContextCompat.getColor(requireContext(), R.color.white)
        val grayColor = ContextCompat.getColor(requireContext(), R.color.elixir_gray)
        val scoreOrangeBackground = ContextCompat.getDrawable(requireContext(), R.drawable.bg_circle_filled_orange)
        val scoreGrayBackground = ContextCompat.getDrawable(requireContext(), R.drawable.bg_oval_outline_gray)
        val timeOrangeBackground = ContextCompat.getDrawable(requireContext(), R.drawable.bg_rect_filled_orange_5)
        val timeGrayBackground = ContextCompat.getDrawable(requireContext(), R.drawable.bg_rect_outline_gray_5)

        val selectedMealTime = dummyData.mealtimes
        val selectedScore = dummyData.score

        // 식사시간 버튼 리스트
        val mealTimeButtons = listOf(
            binding.mealTimeMorning,
            binding.mealTimeLunch,
            binding.mealTimeDinner,
            binding.mealTimeSnack
        )

        // 모두 초기화
        for (button in mealTimeButtons) {
            button.background = timeGrayBackground
            button.setTextColor(grayColor)
        }

        // 선택된 식사 시간만 오렌지로 변경
        when (selectedMealTime) {
            "아침" -> {
                binding.mealTimeMorning.background = timeOrangeBackground
                binding.mealTimeMorning.setTextColor(whiteColor)
            }
            "점심" -> {
                binding.mealTimeLunch.background = timeOrangeBackground
                binding.mealTimeLunch.setTextColor(whiteColor)
            }
            "저녁" -> {
                binding.mealTimeDinner.background = timeOrangeBackground
                binding.mealTimeDinner.setTextColor(whiteColor)
            }
            "간식" -> {
                binding.mealTimeSnack.background = timeOrangeBackground
                binding.mealTimeSnack.setTextColor(whiteColor)
            }
        }

        // 점수 버튼 리스트로 묶기
        val scoreButtons = listOf(
            binding.score1,
            binding.score2,
            binding.score3,
            binding.score4,
            binding.score5
        )

        // 1. 모두 초기화 (회색 배경 & 회색 텍스트, 배경Tint 초기화)
        for (button in scoreButtons) {
            button.background = scoreGrayBackground
            button.backgroundTintList = null // <-- 이거 꼭 해줘야 색상 꼬임 방지!
            button.setTextColor(grayColor)
        }

        // 2. 선택된 점수만 오렌지 배경 & 흰색 텍스트로 변경
        if (selectedScore in 1..5) {
            val selectedButton = scoreButtons[selectedScore - 1]
            selectedButton.background = scoreOrangeBackground
            selectedButton.setTextColor(whiteColor)
        }

        // 메뉴 버튼 클릭 시 팝업 메뉴 표시
        binding.menuButton.setOnClickListener {
            val popupMenu = PopupMenu(context, binding.menuButton)
            popupMenu.menuInflater.inflate(R.menu.item_menu_drop, popupMenu.menu)

            popupMenu.setOnMenuItemClickListener { menuItem ->
                when (menuItem.itemId) {
                    R.id.menu_edit -> {
                        Toast.makeText(context, "댓글 수정 클릭됨", Toast.LENGTH_SHORT).show()
                        true
                    }
                    R.id.menu_delete -> {
                        Toast.makeText(context, "댓글 삭제 클릭됨", Toast.LENGTH_SHORT).show()
                        true
                    }
                    else -> false
                }
            }
            popupMenu.show()
        }

        // ------------------------ 4. 리스트 ------------------------
        binding.tagList.apply {
            layoutManager = FlexboxLayoutManager(context).apply {
                flexDirection = FlexDirection.ROW
                justifyContent = JustifyContent.FLEX_START
            }
            adapter = RecipeTagAdapter(dummyData.mealPlanIngredients)
        }

        // ------------------------ 5. 뒤로 가기 버튼 ------------------------
        binding.backButton.setOnClickListener {
            parentFragmentManager.popBackStack()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
