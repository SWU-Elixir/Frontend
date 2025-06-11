package com.example.elixir.calendar.ui

import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.bumptech.glide.Glide
import com.example.elixir.R
import com.example.elixir.RetrofitClient
import com.example.elixir.calendar.data.DietLogData
import com.example.elixir.calendar.network.db.DietLogRepository
import com.example.elixir.calendar.viewmodel.MealViewModel
import com.example.elixir.calendar.viewmodel.MealViewModelFactory
import com.example.elixir.databinding.FragmentMealDetailBinding
import com.example.elixir.ingredient.network.IngredientDB
import com.example.elixir.ingredient.network.IngredientRepository
import com.example.elixir.member.network.MemberDB
import com.example.elixir.member.network.MemberRepository
import com.example.elixir.network.AppDatabase
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.flexbox.JustifyContent
import com.google.gson.Gson
import org.threeten.bp.format.DateTimeFormatter
import java.util.Locale

class MealDetailFragment : Fragment() {
    private var _binding: FragmentMealDetailBinding? = null
    private val binding get() = _binding!!

    private lateinit var dietRepository: DietLogRepository
    private lateinit var memberRepository: MemberRepository
    private lateinit var ingredientRepository: IngredientRepository

    private val mealViewModel: MealViewModel by viewModels {
        MealViewModelFactory(dietRepository, memberRepository, ingredientRepository)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMealDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 색상 및 배경 drawable 준비
        val whiteColor = ContextCompat.getColor(requireContext(), R.color.white)
        val grayColor = ContextCompat.getColor(requireContext(), R.color.elixir_gray)
        val scoreOrangeBackground = ContextCompat.getDrawable(requireContext(), R.drawable.bg_circle_filled_orange)
        val scoreGrayBackground = ContextCompat.getDrawable(requireContext(), R.drawable.bg_oval_outline_gray)
        val timeOrangeBackground = ContextCompat.getDrawable(requireContext(), R.drawable.bg_rect_filled_orange_5)
        val timeGrayBackground = ContextCompat.getDrawable(requireContext(), R.drawable.bg_rect_outline_gray_5)

        // -------------- 레포지토리 및 뷰모델 초기화 --------------
        val dietDao = AppDatabase.getInstance(requireContext()).dietLogDao()
        val dietApi = RetrofitClient.instanceDietApi
        dietRepository = DietLogRepository(dietDao, dietApi)

        val memberDao = MemberDB.getInstance(requireContext()).memberDao()
        val memberApi = RetrofitClient.instanceMemberApi
        memberRepository = MemberRepository(memberApi, memberDao)

        val ingredientDao = IngredientDB.getInstance(requireContext()).ingredientDao()
        val ingredientApi = RetrofitClient.instanceIngredientApi
        ingredientRepository = IngredientRepository(ingredientApi, ingredientDao)

        // 데이터 불러오기
        val mealDataJson = arguments?.getString("mealData")
        val dietLogData = mealDataJson?.let {
            Gson().fromJson(it, DietLogData::class.java)
        }
        Log.d("MealDetailFragment", "mealDataJson: $mealDataJson")

        // 이미지 처리
        context?.let {
            Glide.with(it)
                .load(dietLogData?.dietImg) // file://, content://, http:// 모두 지원
                .placeholder(R.drawable.img_blank) // 로딩 중 표시할 이미지
                .error(R.drawable.img_blank) // 실패 시 표시할 이미지
                .into(binding.recipeImage)
        }

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
        when (dietLogData?.dietCategory) {
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

        // 시간 반영
        binding.timeText.text = dietLogData?.time?.format(DateTimeFormatter.ofPattern("a h:mm", Locale.ENGLISH))

        val score = dietLogData?.score ?: 0

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
        if (score in 1..5) {
            val selectedButton = scoreButtons[score - 1]
            selectedButton.background = scoreOrangeBackground
            selectedButton.setTextColor(whiteColor)
        }

        mealViewModel.loadIngredients()

        mealViewModel.ingredientList.observe(viewLifecycleOwner) { ingredientList ->
            // ingredientMap 생성
            val ingredientMap = ingredientList.associateBy { it.id }

            // dietLogData?.ingredientTags는 태그로 보여줄 id 리스트라고 가정
            val ingredientTags = dietLogData?.ingredientTags ?: emptyList()

            // Adapter 연결
            binding.tagList.layoutManager = FlexboxLayoutManager(requireContext())
            binding.tagList.adapter = MealDetailIngredientAdapter(ingredientTags, ingredientMap)
        }

        // ------------------------ 리스트 ------------------------
        binding.tagList.apply {
            layoutManager = FlexboxLayoutManager(context).apply {
                flexDirection = FlexDirection.ROW
                justifyContent = JustifyContent.FLEX_START
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
