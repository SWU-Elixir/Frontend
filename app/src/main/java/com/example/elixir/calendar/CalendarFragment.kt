package com.example.elixir.calendar

import android.content.Context
import android.graphics.*
import android.os.Bundle
import android.text.style.ForegroundColorSpan
import android.text.style.LineBackgroundSpan
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.elixir.R
import com.example.elixir.databinding.FragmentCalendarBinding
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.prolificinteractive.materialcalendarview.*
import com.prolificinteractive.materialcalendarview.format.TitleFormatter
import com.prolificinteractive.materialcalendarview.format.WeekDayFormatter
import java.math.BigInteger

// 식단 아이템 데이터 클래스
// 각 식사 시간, 식단명, 재료, 점수, 이미지 리소스를 포함
// 점수는 1~5로, 시각화 시 점 색상 구분에 사용


// ----------------------------- 프래그먼트 -------------------------------------
class CalendarFragment : Fragment() {

    private var _binding: FragmentCalendarBinding? = null
    private val binding get() = _binding!!

    private lateinit var mealAdapter: MealListAdapter

    // 날짜 관련 변수
    private val eventMap = mutableMapOf<String, MutableList<MealPlanData>>()
    private var selectedCalendarDay: CalendarDay = CalendarDay.today()
    private var selectedTextDecorator: SelectedDateTextColorDecorator? = null
    private var todayTextDecorator: TodayTextColorDecorator? = null
    private val today = CalendarDay.today()
    private var isFirstLaunch = true

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCalendarBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // -------------------- 리스트뷰 설정 --------------------
        mealAdapter = MealListAdapter(requireContext(), mutableListOf(), fragmentManager = parentFragmentManager)
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
        } catch (e: Exception) {
            Log.e("CalendarFragment", "오늘 날짜 배경 설정 오류: ${e.message}")
        }

        // ----------------- FAB 및 바텀시트 ---------------------
        val behavior = BottomSheetBehavior.from(binding.bottomSheet)
        binding.fab.hide()
        var bottomSheetState = BottomSheetBehavior.STATE_COLLAPSED

        // 오늘 날짜 기본 선택 및 점수 데코레이터 처리
        selectedCalendarDay = today
        updateEventList(selectedCalendarDay.toString())
        updateSelectedDateDecorator()
        processDietLogScores(binding.calendarView)

        // 날짜 선택 이벤트 처리
        binding.calendarView.setOnDateChangedListener { _, date, _ ->
            selectedCalendarDay = date
            val selectedDateStr = "%04d-%02d-%02d".format(date.year, date.month + 1, date.day)
            updateEventList(selectedDateStr)
            updateSelectedDateDecorator()

            binding.fab.visibility = if (selectedCalendarDay == today && bottomSheetState != BottomSheetBehavior.STATE_COLLAPSED) View.VISIBLE else View.GONE
        }

        // FAB 클릭 이벤트
        binding.fab.setOnClickListener {
            Log.d("CalendarFragment", "FAB 클릭됨")
        }

        // 바텀시트 상태 변화 시 FAB 제어
        behavior.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                bottomSheetState = newState
                binding.fab.visibility = if (selectedCalendarDay == today && newState != BottomSheetBehavior.STATE_COLLAPSED) View.VISIBLE else View.GONE
            }
            override fun onSlide(bottomSheet: View, slideOffset: Float) {}
        })
    }

    // ------------------------ 데코레이터 ---------------------------

    // 선택된 날짜의 텍스트 색상 변경 데코레이터
    private fun updateSelectedDateDecorator() {
        selectedTextDecorator?.let { binding.calendarView.removeDecorator(it) }
        todayTextDecorator?.let { binding.calendarView.removeDecorator(it) }

        val textColor = ContextCompat.getColor(requireContext(), R.color.white)
        selectedTextDecorator = SelectedDateTextColorDecorator(selectedCalendarDay, textColor)
        binding.calendarView.addDecorator(selectedTextDecorator!!)

        if (selectedCalendarDay != today || isFirstLaunch) {
            val orangeColor = ContextCompat.getColor(requireContext(), R.color.elixir_orange)
            todayTextDecorator = TodayTextColorDecorator(today, orangeColor)
            binding.calendarView.addDecorator(todayTextDecorator!!)
        }
        isFirstLaunch = false
    }

    // 오늘 날짜 텍스트 색상 (오렌지)
    class TodayTextColorDecorator(private val day: CalendarDay, private val color: Int) : DayViewDecorator {
        override fun shouldDecorate(date: CalendarDay): Boolean = date == day
        override fun decorate(view: DayViewFacade) {
            view.addSpan(ForegroundColorSpan(color))
        }
    }

    // 선택 날짜 텍스트 색상 (흰색)
    class SelectedDateTextColorDecorator(private val selectedDay: CalendarDay, private val color: Int) : DayViewDecorator {
        override fun shouldDecorate(day: CalendarDay): Boolean = day == selectedDay
        override fun decorate(view: DayViewFacade) {
            view.addSpan(ForegroundColorSpan(color))
        }
    }

    // ------------------------ 커스텀 포매터 ---------------------------

    // 요일 표시 한글화
    class CustomWeekDayFormatter(context: Context) : WeekDayFormatter {
        private val weekdays = context.resources.getStringArray(R.array.custom_weekdays)
        override fun format(dayOfWeek: Int): CharSequence = weekdays[(dayOfWeek - 1) % 7]
    }

    // 월 표시 한글화
    class CustomTitleFormatter(context: Context) : TitleFormatter {
        private val months = context.resources.getStringArray(R.array.custom_months)
        override fun format(day: CalendarDay): CharSequence = "${day.year}.${months[day.month]}"
    }

    // ------------------------ 데코레이터 (점 표시) ---------------------------

    // 점수 기반 색상 점 데코레이터 적용
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

    // 점수에 따라 점 색상 반환
    private fun getColorByScore(score: Int) = when (score) {
        5 -> Color.parseColor("#E738F4")
        4 -> Color.parseColor("#38F4F4")
        3 -> Color.parseColor("#D5F438")
        2 -> Color.parseColor("#FDE950")
        1 -> Color.parseColor("#FD5050")
        else -> Color.GRAY
    }

    // 점 1개 이상 있는 날짜에 여러 점 표시
    class MultipleDotDecorator(private val colors: List<Int>, private val date: CalendarDay, private val textColor: Int) : DayViewDecorator {
        override fun shouldDecorate(day: CalendarDay): Boolean = day == date
        override fun decorate(view: DayViewFacade) {
            view.addSpan(CustomDotSpan(colors))
            view.addSpan(ForegroundColorSpan(textColor))
        }
    }

    // 점을 그리는 커스텀 span 클래스
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

    // ------------------------ 이벤트 처리 ---------------------------

    // 날짜에 따른 식단 표시 업데이트
    private fun updateEventList(selectedDate: String) {
        val events = eventMap[selectedDate]
        if (events.isNullOrEmpty()) {
            binding.mealPlanList.visibility = View.GONE
            binding.emptyMealText.visibility = View.VISIBLE
        } else {
            binding.mealPlanList.visibility = View.VISIBLE
            binding.emptyMealText.visibility = View.GONE
            mealAdapter.updateData(events)
        }
    }

    // 식단 클릭 시 동작
    private fun onDietLogClick(item: MealPlanData) {
        Log.d("CalendarFragment", "클릭된 식단: ${item.name} - ${item.mealtimes} - ${item.mealPlanIngredients}")
    }

    // ------------------------ 더미 데이터 ---------------------------

    // 샘플 식단 데이터 추가
    private fun addDummyEvents() {
        eventMap["2025-03-29"] = mutableListOf(
            MealPlanData(
                id = BigInteger("1001"),
                memberId = BigInteger("1"),
                name = "연어 아보카도 샐러드",
                imageUrl = R.drawable.png_recipe_sample,
                createdAt = "2025-03-29",
                mealtimes = "아침",
                score = 5,
                mealPlanIngredients = listOf("연어", "아보카도", "올리브유", "잣")
            ),
            MealPlanData(
                id = BigInteger("1002"),
                memberId = BigInteger("1"),
                name = "렌틸콩 채소 수프",
                imageUrl = null,
                createdAt = "2025-03-29",
                mealtimes = "점심",
                score = 4,
                mealPlanIngredients = listOf("렌틸콩", "당근", "셀러리", "양파")
            ),
            MealPlanData(
                id = BigInteger("1003"),
                memberId = BigInteger("1"),
                name = "두부 채소 볶음",
                imageUrl = null,
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
                imageUrl = null,
                createdAt = "2025-03-30",
                mealtimes = "아침",
                score = 5,
                mealPlanIngredients = listOf("귀리", "블루베리", "요거트", "아몬드")
            ),
            MealPlanData(
                id = BigInteger("1005"),
                memberId = BigInteger("1"),
                name = "퀴노아 보울",
                imageUrl = R.drawable.png_recipe_sample,
                createdAt = "2025-03-30",
                mealtimes = "점심",
                score = 4,
                mealPlanIngredients = listOf("퀴노아", "아보카도", "병아리콩", "시금치")
            ),
            MealPlanData(
                id = BigInteger("1006"),
                memberId = BigInteger("1"),
                name = "베리 스무디",
                imageUrl = null,
                createdAt = "2025-03-30",
                mealtimes = "간식",
                score = 5,
                mealPlanIngredients = listOf("딸기", "블루베리", "바나나", "아몬드밀크")
            )
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}