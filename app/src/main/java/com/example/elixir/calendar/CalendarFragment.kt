/**
 * 캘린더 프래그먼트
 * 
 * 주요 기능:
 * 1. 식단 일정을 캘린더에 표시
 * 2. 날짜별 식단 목록 조회
 * 3. 식단 점수에 따른 시각화 (점 표시)
 * 4. 바텀시트를 통한 식단 상세 정보 표시
 */
package com.example.elixir.calendar

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.*
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.text.style.ForegroundColorSpan
import android.text.style.LineBackgroundSpan
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import com.example.elixir.ToolbarActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.elixir.R
import com.example.elixir.databinding.FragmentCalendarBinding
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.prolificinteractive.materialcalendarview.*
import com.prolificinteractive.materialcalendarview.format.TitleFormatter
import com.prolificinteractive.materialcalendarview.format.WeekDayFormatter
import java.math.BigInteger

// ----------------------------- 프래그먼트 클래스 -------------------------------------
class CalendarFragment : Fragment() {
    // 바인딩
    private lateinit var calendarBinding: FragmentCalendarBinding

    // 뷰 바인딩 변수
    private var _binding: FragmentCalendarBinding? = null
    private val binding get() = _binding!!

    // 어댑터 및 데이터 변수
    private lateinit var mealAdapter: MealListAdapter
    private val eventMap = mutableMapOf<String, MutableList<MealPlanData>>() // 날짜별 식단 데이터 저장
    private var selectedCalendarDay: CalendarDay = CalendarDay.today() // 선택된 날짜
    private var selectedTextDecorator: SelectedDateTextColorDecorator? = null // 선택 날짜 텍스트 색상 데코레이터
    private var todayTextDecorator: TodayTextColorDecorator? = null // 오늘 날짜 텍스트 색상 데코레이터
    private val today = CalendarDay.today() // 오늘 날짜
    private var isFirstLaunch = true // 첫 실행 여부


    // ------------------------ 생명주기 메서드 ---------------------------

    /**
     * 프래그먼트 상태 저장
     * 선택된 날짜 정보를 저장하여 화면 회전 시에도 유지
     */
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt("selected_year", selectedCalendarDay.year)
        outState.putInt("selected_month", selectedCalendarDay.month)
        outState.putInt("selected_day", selectedCalendarDay.day)
    }

    /**
     * 프래그먼트 상태 복원
     * 저장된 날짜 정보를 복원하여 이전 상태 유지
     */
    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
        savedInstanceState?.let {
            val year = it.getInt("selected_year", today.year)
            val month = it.getInt("selected_month", today.month)
            val day = it.getInt("selected_day", today.day)
            selectedCalendarDay = CalendarDay.from(year, month, day)
        }
    }

    /**
     * 뷰 생성
     * 프래그먼트의 레이아웃을 인플레이트
     */
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCalendarBinding.inflate(inflater, container, false)
        return binding.root
    }

    /**
     * 뷰 생성 완료 후 초기화
     * 캘린더, 리스트뷰, 바텀시트 등의 UI 컴포넌트 설정
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // -------------------- 리스트뷰 설정 --------------------
        mealAdapter = MealListAdapter(requireContext(), mutableListOf())
        binding.mealPlanList.adapter = mealAdapter
        addDummyEvents() // 더미 데이터 세팅

        // -------------------- 캘린더 설정 ----------------------
        binding.calendarView.setWeekDayFormatter(CustomWeekDayFormatter(requireContext()))
        binding.calendarView.setTitleFormatter(CustomTitleFormatter(requireContext()))

        // 오늘 날짜 배경 원 데코레이터
        try {
            val todayDrawable = ContextCompat.getDrawable(requireContext(), R.drawable.bg_oval_outline_orange)
            todayDrawable?.let {
                binding.calendarView.addDecorator(CalendarTodayDecorator(requireContext(), it))
            }
        }


        // ----------------- FAB 및 바텀시트 ---------------------
        val behavior = BottomSheetBehavior.from(binding.bottomSheet)
        binding.fab.hide()
        var bottomSheetState = BottomSheetBehavior.STATE_COLLAPSED

        // 초기 날짜 설정 및 데코레이터 적용
        if (isFirstLaunch) {
            selectedCalendarDay = today
            isFirstLaunch = false
        }
        
        // 초기 상태 설정
        val initialDateStr = "%04d-%02d-%02d".format(selectedCalendarDay.year, selectedCalendarDay.month + 1, selectedCalendarDay.day)
        updateEventList(initialDateStr)
        updateSelectedDateDecorator()
        processDietLogScores(binding.calendarView)
        binding.calendarView.setSelectedDate(selectedCalendarDay)

        // 날짜 선택 이벤트 처리
        binding.calendarView.setOnDateChangedListener { _, date, _ ->
            selectedCalendarDay = date
            val selectedDateStr = "%04d-%02d-%02d".format(date.year, date.month + 1, date.day)
            
            // 선택된 날짜 업데이트
            binding.calendarView.setSelectedDate(date)
            
            // 데코레이터 업데이트
            updateSelectedDateDecorator()
            
            // 리스트 업데이트
            updateEventList(selectedDateStr)

            binding.fab.visibility = if (selectedCalendarDay == today && bottomSheetState != BottomSheetBehavior.STATE_COLLAPSED) View.VISIBLE else View.GONE
        }

        // FAB 클릭 이벤트
        binding.fab.setOnClickListener {
            Log.d("CalendarFragment", "FAB 클릭됨")
            val intent = Intent(context, ToolbarActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
                putExtra("mode", 2)
                putExtra("year", selectedCalendarDay.year)
                putExtra("month", selectedCalendarDay.month + 1)
                putExtra("day", selectedCalendarDay.day)
            }

            dietLogLauncher.launch(intent)
        }

        // ActivityResultLauncher 등록
        dietLogLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val intent = result.data
                val mealPlanData = intent?.extras?.getSerializable("mealData") as? MealPlanData
                // 로그로 mealPlanData 확인
                Log.d("DietLogFragment", "Received mealPlanData: $mealPlanData")

                if (intent != null) {

                    Toast.makeText(context, mealPlanData?.name, Toast.LENGTH_SHORT).show()
                    mealPlanData?.let {
                        val selectedDateStr = "%04d-%02d-%02d".format(
                            selectedCalendarDay.year,
                            selectedCalendarDay.month + 1,
                            selectedCalendarDay.day
                        )
                        if (eventMap[selectedDateStr] == null) {
                            eventMap[selectedDateStr] = mutableListOf()
                        }
                        eventMap[selectedDateStr]?.add(it)
                        updateEventList(selectedDateStr) // 리스트 갱신
                    }
                }
            } else {
                Toast.makeText(context, "식단 작성 실패", Toast.LENGTH_SHORT).show()
            }
        }

        // 바텀시트 설정
        val behavior = BottomSheetBehavior.from(calendarBinding.bottomSheet)
        behavior.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                bottomSheetState = newState
                binding.fab.visibility = if (selectedCalendarDay == today && newState != BottomSheetBehavior.STATE_COLLAPSED) View.VISIBLE else View.GONE
            }
            override fun onSlide(bottomSheet: View, slideOffset: Float) {}
        })
    }

    /**
     * 프래그먼트 재개 시 상태 복원
     */
    override fun onResume() {
        super.onResume()
        // 선택된 날짜 스타일 복원
        updateSelectedDateDecorator()
        val selectedDateStr = "%04d-%02d-%02d".format(selectedCalendarDay.year, selectedCalendarDay.month + 1, selectedCalendarDay.day)
        updateEventList(selectedDateStr)
        binding.calendarView.setSelectedDate(selectedCalendarDay)
    }

    // ------------------------ 데코레이터 클래스 ---------------------------

    /**
     * 선택된 날짜의 텍스트 색상 변경 데코레이터 업데이트
     */
    private fun updateSelectedDateDecorator() {
        selectedTextDecorator?.let { binding.calendarView.removeDecorator(it) }
        todayTextDecorator?.let { binding.calendarView.removeDecorator(it) }

        val textColor = ContextCompat.getColor(requireContext(), R.color.white)
        selectedTextDecorator = SelectedDateTextColorDecorator(selectedCalendarDay, textColor)
        binding.calendarView.addDecorator(selectedTextDecorator!!)

        if (selectedCalendarDay != today) {
            val orangeColor = ContextCompat.getColor(requireContext(), R.color.elixir_orange)
            todayTextDecorator = TodayTextColorDecorator(today, orangeColor)
            binding.calendarView.addDecorator(todayTextDecorator!!)
        }
    }

    /**
     * 오늘 날짜 텍스트 색상 데코레이터 (오렌지)
     */
    class TodayTextColorDecorator(private val day: CalendarDay, private val color: Int) : DayViewDecorator {
        override fun shouldDecorate(date: CalendarDay): Boolean = date == day
        override fun decorate(view: DayViewFacade) {
            view.addSpan(ForegroundColorSpan(color))
        }
    }

    /**
     * 선택 날짜 텍스트 색상 데코레이터 (흰색)
     */
    class SelectedDateTextColorDecorator(private val selectedDay: CalendarDay, private val color: Int) : DayViewDecorator {
        override fun shouldDecorate(day: CalendarDay): Boolean = day == selectedDay
        override fun decorate(view: DayViewFacade) {
            view.addSpan(ForegroundColorSpan(color))
        }
    }

    // ------------------------ 커스텀 포매터 클래스 ---------------------------

    /**
     * 요일 표시 한글화 포매터
     */
    class CustomWeekDayFormatter(context: Context) : WeekDayFormatter {
        private val weekdays = context.resources.getStringArray(R.array.custom_weekdays)
        override fun format(dayOfWeek: Int): CharSequence = weekdays[(dayOfWeek - 1) % 7]
    }

    /**
     * 월 표시 한글화 포매터
     */
    class CustomTitleFormatter(context: Context) : TitleFormatter {
        private val months = context.resources.getStringArray(R.array.custom_months)
        override fun format(day: CalendarDay): CharSequence = "${day.year}.${months[day.month]}"
    }

    // ------------------------ 점 표시 데코레이터 ---------------------------

    /**
     * 점수 기반 색상 점 데코레이터 적용
     * 각 날짜의 식단 점수에 따라 다른 색상의 점을 표시
     */
    private fun processDietLogScores(calendarView: MaterialCalendarView) {
        val dotMap = mutableMapOf<CalendarDay, MutableList<Int>>()
        eventMap.forEach { (dateStr, items) ->
            val (year, month, day) = dateStr.split("-").map { it.toInt() }
            val calendarDay = CalendarDay.from(year, month - 1, day)
            val colors = items.map { getColorByScore(it.score) }
            dotMap[calendarDay] = colors.toMutableList()
        }
        dotMap.forEach { (day, colors) ->
            val defaultTextColor = Color.BLACK
            calendarView.addDecorator(MultipleDotDecorator(colors, day, defaultTextColor))
        }
    }

    /**
     * 점수에 따른 점 색상 반환
     * 5점: 보라색, 4점: 하늘색, 3점: 연두색, 2점: 노란색, 1점: 빨간색
     */
    private fun getColorByScore(score: Int) = when (score) {
        5 -> Color.parseColor("#E738F4")
        4 -> Color.parseColor("#38F4F4")
        3 -> Color.parseColor("#D5F438")
        2 -> Color.parseColor("#FDE950")
        1 -> Color.parseColor("#FD5050")
        else -> Color.GRAY
    }

    /**
     * 여러 점을 표시하는 데코레이터
     */
    class MultipleDotDecorator(private val colors: List<Int>, private val date: CalendarDay, private val textColor: Int) : DayViewDecorator {
        override fun shouldDecorate(day: CalendarDay): Boolean = day == date
        override fun decorate(view: DayViewFacade) {
            view.addSpan(CustomDotSpan(colors))
            view.addSpan(ForegroundColorSpan(textColor))
        }
    }

    /**
     * 점을 그리는 커스텀 span 클래스
     */
    class CustomDotSpan(private val colors: List<Int>) : LineBackgroundSpan {
        private val radius = 5f
        private val verticalSpacing = 12f
        override fun drawBackground(
            canvas: Canvas,
            paint: Paint,
            left: Int,
            right: Int,
            top: Int,
            baseline: Int,
            bottom: Int,
            text: CharSequence,
            start: Int,
            end: Int,
            lineNumber: Int
        ) {
            val totalWidth = (colors.size * radius * 2) + ((colors.size - 1) * 8f)
            var offsetX = (left + right) / 2 - (totalWidth / 2) + radius
            val offsetY = baseline + radius + verticalSpacing
            colors.forEach { color ->
                paint.color = color
                canvas.drawCircle(offsetX, offsetY, radius, paint)
                offsetX += radius * 2 + 8f
            }
        }
    }

    // ------------------------ 이벤트 처리 메서드 ---------------------------

    /**
     * 선택된 날짜의 식단 목록 업데이트
     * @param selectedDate 선택된 날짜 (YYYY-MM-DD 형식)
     */
    private fun updateEventList(selectedDate: String) {
        val events = eventMap[selectedDate]
        if (events.isNullOrEmpty()) {
            binding.mealPlanList.visibility = View.GONE
            binding.emptyMealText.visibility = View.VISIBLE
        } else {
            binding.mealPlanList.visibility = View.VISIBLE
            binding.emptyMealText.visibility = View.GONE
            mealAdapter.updateData(events)
            binding.mealPlanList.smoothScrollToPosition(0)
        }
    }

    // ------------------------ 더미 데이터 ---------------------------

    /**
     * 샘플 식단 데이터 추가
     * 개발 및 테스트를 위한 더미 데이터
     */
    private fun addDummyEvents() {
        eventMap["2025-03-29"] = mutableListOf(
            MealPlanData(
                id = BigInteger("1001"),
                memberId = BigInteger("1"),
                name = "연어 아보카도 샐러드",
                imageUrl = Uri.parse("android.resource://${context?.packageName}/${R.drawable.png_recipe_sample}").toString(),
                createdAt = "2025-03-29",
                mealtimes = "아침",
                score = 5,
                mealPlanIngredients = listOf("연어", "아보카도", "올리브유", "잣")
            ),
            MealPlanData(
                id = BigInteger("1002"),
                memberId = BigInteger("1"),
                name = "렌틸콩 채소 수프",
                imageUrl = Uri.parse("android.resource://${context?.packageName}/${R.drawable.png_recipe_sample}").toString(),
                createdAt = "2025-03-29",
                mealtimes = "점심",
                score = 4,
                mealPlanIngredients = listOf("렌틸콩", "당근", "셀러리", "양파")
            ),
            MealPlanData(
                id = BigInteger("1003"),
                memberId = BigInteger("1"),
                name = "두부 채소 볶음",
                imageUrl = Uri.parse("android.resource://${context?.packageName}/${R.drawable.png_recipe_sample}").toString(),
                createdAt = "2025-03-29",
                mealtimes = "저녁",
                score = 3,
                mealPlanIngredients = listOf("두부", "브로콜리", "마늘", "참기름")
            )
        )

        eventMap["2025-03-30"] = mutableListOf(
            MealPlanData(
                id = BigInteger("1004"),
                memberId = BigInteger("1"),
                name = "귀리 베리볼",
                imageUrl = Uri.parse("android.resource://${context?.packageName}/${R.drawable.png_recipe_sample}").toString(),
                createdAt = "2025-03-30",
                mealtimes = "아침",
                score = 5,
                mealPlanIngredients = listOf("귀리", "블루베리", "요거트", "아몬드")
            ),
            MealPlanData(
                id = BigInteger("1005"),
                memberId = BigInteger("1"),
                name = "퀴노아 보울",
                imageUrl = Uri.parse("android.resource://${context?.packageName}/${R.drawable.png_recipe_sample}").toString(),
                createdAt = "2025-03-30",
                mealtimes = "점심",
                score = 4,
                mealPlanIngredients = listOf("퀴노아", "아보카도", "병아리콩", "시금치")
            ),
            MealPlanData(
                id = BigInteger("1006"),
                memberId = BigInteger("1"),
                name = "베리 스무디",
                imageUrl = Uri.parse("android.resource://${context?.packageName}/${R.drawable.png_recipe_sample}").toString(),
                createdAt = "2025-03-30",
                mealtimes = "간식",
                score = 5,
                mealPlanIngredients = listOf("딸기", "블루베리", "바나나", "아몬드밀크")
            )
        )
    }

    /**
     * 뷰 제거 시 바인딩 해제
     */
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}