package com.example.elixir.calendar

import android.app.Activity
import android.content.Intent
import android.content.res.ColorStateList
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.example.elixir.R
import com.example.elixir.recipe.RecipeTagAdapter
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.flexbox.JustifyContent
import java.math.BigInteger

class MealDetailFragment : Fragment() {

    // 상단
    private lateinit var backButton: ImageButton
    private lateinit var menuButton: ImageButton
    private lateinit var mealName: TextView

    // 레시피 정보
    private lateinit var createAt: TextView
    private lateinit var mealTimeMorning: TextView
    private lateinit var mealTimeLunch: TextView
    private lateinit var mealTimeDinner: TextView
    private lateinit var mealTimeSnack: TextView
    private lateinit var tagList: RecyclerView
    private lateinit var score1: TextView
    private lateinit var score2: TextView
    private lateinit var score3: TextView
    private lateinit var score4: TextView
    private lateinit var score5: TextView

    // DietLogFragment 띄우기
    private lateinit var dietLogLauncher: ActivityResultLauncher<Intent>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_meal_detail, container, false)

        // ------------------------ 1. 뷰 초기화 ------------------------
        backButton = view.findViewById(R.id.backButton)
        menuButton = view.findViewById(R.id.menuButton)

        mealName = view.findViewById(R.id.mealTitle)
        createAt = view.findViewById(R.id.timeText)
        mealTimeMorning = view.findViewById(R.id.mealTimeMorning)
        mealTimeLunch = view.findViewById(R.id.mealTimeLunch)
        mealTimeDinner = view.findViewById(R.id.mealTimeDinner)
        mealTimeSnack = view.findViewById(R.id.mealTimeSnack)
        tagList = view.findViewById(R.id.tagList)
        score1 = view.findViewById(R.id.score1)
        score2 = view.findViewById(R.id.score2)
        score3 = view.findViewById(R.id.score3)
        score4 = view.findViewById(R.id.score4)
        score5 = view.findViewById(R.id.score5)

        // ------------------------ 2. 더미 데이터 적용 ------------------------
        val dummyData = MealPlanData(
            id = BigInteger("1001"),
            memberId = BigInteger("1"),
            name = "연어 아보카도 샐러드",
            imageUrl = Uri.parse("android.resource://${context?.packageName}/${R.drawable.png_recipe_sample}").toString(),
            createdAt = "2025-03-29",
            mealtimes = "아침",
            score = 5,
            mealPlanIngredients = listOf("연어", "아보카도", "올리브유", "잣")
        )

        // ------------------------ 3. 데이터 바인딩 ------------------------
        mealName.text = dummyData.name

        // 색상 가져오기
        val orangeColor = ContextCompat.getColor(requireContext(), R.color.elixir_orange)
        val whiteColor = ContextCompat.getColor(requireContext(), R.color.white)
        val grayColor = ContextCompat.getColor(requireContext(), R.color.elixir_gray)
        // 오렌지, 그레이 배경 drawable 준비 (예시)
        val orangeBackground = ContextCompat.getDrawable(requireContext(), R.drawable.bg_oval_outline_orange)
        val grayBackground = ContextCompat.getDrawable(requireContext(), R.drawable.bg_oval_outline_gray)

        val selectedMealTime = dummyData.mealtimes
        val selectedScore = dummyData.score

        // 모든 버튼 기본 색상 회색으로 초기화
        mealTimeMorning.backgroundTintList = ColorStateList.valueOf(grayColor)
        mealTimeLunch.backgroundTintList = ColorStateList.valueOf(grayColor)
        mealTimeDinner.backgroundTintList = ColorStateList.valueOf(grayColor)
        mealTimeSnack.backgroundTintList = ColorStateList.valueOf(grayColor)

        mealTimeMorning.setTextColor(grayColor)
        mealTimeLunch.setTextColor(grayColor)
        mealTimeDinner.setTextColor(grayColor)
        mealTimeSnack.setTextColor(grayColor)

        // 선택된 식사 시간만 오렌지로 변경
        when (selectedMealTime) {
            "아침" -> {
                mealTimeMorning.backgroundTintList = ColorStateList.valueOf(orangeColor)
                mealTimeMorning.setTextColor(orangeColor)
            }
            "점심" -> {
                mealTimeLunch.backgroundTintList = ColorStateList.valueOf(orangeColor)
                mealTimeLunch.setTextColor(orangeColor)
            }
            "저녁" -> {
                mealTimeDinner.backgroundTintList = ColorStateList.valueOf(orangeColor)
                mealTimeDinner.setTextColor(orangeColor)
            }
            "간식" -> {
                mealTimeSnack.backgroundTintList = ColorStateList.valueOf(orangeColor)
                mealTimeSnack.setTextColor(orangeColor)
            }
        }

        // 일단 모두 회색 배경으로 초기화
        score1.background = grayBackground
        score2.background = grayBackground
        score3.background = grayBackground
        score4.background = grayBackground
        score5.background = grayBackground

        score1.setTextColor(grayColor)
        score2.setTextColor(grayColor)
        score3.setTextColor(grayColor)
        score4.setTextColor(grayColor)
        score5.setTextColor(grayColor)

        // 선택된 점수만 오렌지로 변경
        when (selectedScore) {
            1 -> {
                score1.background = orangeBackground
                score1.setTextColor(whiteColor)
            }
            2 -> {
                score2.background = orangeBackground
                score2.setTextColor(whiteColor)
            }
            3 -> {
                score3.background = orangeBackground
                score3.setTextColor(whiteColor)
            }
            4 -> {
                score4.background = orangeBackground
                score4.setTextColor(whiteColor)
            }
            5 -> {
                score5.background = orangeBackground
                score5.setTextColor(orangeColor)
            }
        }

        // 메뉴 버튼 클릭 시 팝업 메뉴 표시
        menuButton.setOnClickListener {
            val popupMenu = PopupMenu(context, menuButton)
            popupMenu.menuInflater.inflate(R.menu.item_menu_drop, popupMenu.menu)

            popupMenu.setOnMenuItemClickListener { menuItem ->
                when (menuItem.itemId) {
                    R.id.menu_edit -> {
                        dietLogLauncher = registerForActivityResult(
                            ActivityResultContracts.StartActivityForResult()
                        ) { result ->
                            if (result.resultCode == Activity.RESULT_OK) {
                                val intent = result.data
                                val mealPlanData =
                                    intent?.extras?.getSerializable("mealData") as? MealPlanData
                                // 로그로 mealPlanData 확인
                                Log.d("DietLogFragment", "Received mealPlanData: $mealPlanData")

                                
                            } else {
                                Toast.makeText(context, "식단 작성 실패", Toast.LENGTH_SHORT).show()
                            }
                        }
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
        tagList.apply {
            layoutManager = FlexboxLayoutManager(context).apply {
                flexDirection = FlexDirection.ROW
                justifyContent = JustifyContent.FLEX_START
            }
            adapter = RecipeTagAdapter(dummyData.mealPlanIngredients)
        }


        // ------------------------ 5. 뒤로 가기 버튼 ------------------------
        backButton.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        return view
    }

}
