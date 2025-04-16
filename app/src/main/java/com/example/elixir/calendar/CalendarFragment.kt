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
import android.widget.ListView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.elixir.R
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.prolificinteractive.materialcalendarview.*
import com.prolificinteractive.materialcalendarview.format.TitleFormatter
import com.prolificinteractive.materialcalendarview.format.WeekDayFormatter

// 식단 아이템 데이터 클래스
data class DietLogItem(
    val dietTimes: String,
    val dietName: String,
    val dietIngredients: List<String>,
    val dietScore: Int,
    val dietImageRes: Int? = null
)

class CalendarFragment : Fragment() {

    // UI 컴포넌트 선언
    private lateinit var eventListView: ListView
    private lateinit var eventAdapter: DietLogAdapter
    private lateinit var emptyDietText: TextView
    private lateinit var calendarView: MaterialCalendarView
    private lateinit var fab: FloatingActionButton

    // 날짜 관련 변수
    private val eventMap = mutableMapOf<String, MutableList<DietLogItem>>()
    private var selectedCalendarDay: CalendarDay = CalendarDay.today()
    private var selectedTextDecorator: SelectedDateTextColorDecorator? = null
    private var todayTextDecorator: TodayTextColorDecorator? = null
    private val today = CalendarDay.today()
    private var isFirstLaunch = true // 첫 실행 여부 확인용 플래그

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_calendar, container, false)

        // 리스트뷰 설정
        eventListView = view.findViewById(R.id.dietLogList)
        emptyDietText = view.findViewById(R.id.emptyDietText)
        eventAdapter = DietLogAdapter(requireContext(), mutableListOf()) { item ->
            onDietLogClick(item)
        }
        eventListView.adapter = eventAdapter

        // 샘플 데이터 추가
        addDummyEvents()

        // 캘린더 뷰 설정
        calendarView = view.findViewById(R.id.calendarView)
        calendarView.setWeekDayFormatter(CustomWeekDayFormatter(requireContext()))
        calendarView.setTitleFormatter(CustomTitleFormatter(requireContext()))

        // 오늘 날짜 데코레이터 (배경 원)
        try {
            val todayDrawable = ContextCompat.getDrawable(requireContext(),
                R.drawable.bg_calendar_circletoday
            )
            todayDrawable?.let {
                calendarView.addDecorator(CalendarTodayDecorator(requireContext(), it))
            }
        } catch (e: Exception) {
            Log.e("CalendarFragment", "오늘 날짜 배경 설정 오류: \${e.message}")
        }

        // 플로팅 액션 버튼 및 바텀시트 설정
        val bottomSheet = view.findViewById<CardView>(R.id.bottomSheet)
        val behavior = BottomSheetBehavior.from(bottomSheet)
        fab = view.findViewById(R.id.fab)
        fab.hide()
        var bottomSheetState = BottomSheetBehavior.STATE_COLLAPSED

        // 오늘 날짜를 기본 선택으로 설정
        selectedCalendarDay = today
        updateEventList(selectedCalendarDay.toString())
        updateSelectedDateDecorator()
        processDietLogScores(calendarView)

        // 날짜 선택 이벤트 처리
        calendarView.setOnDateChangedListener { _, date, _ ->
            selectedCalendarDay = date
            val selectedDateStr = "%04d-%02d-%02d".format(date.year, date.month + 1, date.day)
            updateEventList(selectedDateStr)
            updateSelectedDateDecorator()

            fab.visibility = if (selectedCalendarDay == today && bottomSheetState != BottomSheetBehavior.STATE_COLLAPSED) View.VISIBLE else View.GONE
        }

        // FAB 클릭 이벤트
        fab.setOnClickListener {
            Log.d("CalendarFragment", "FAB 클릭됨")
        }

        // 바텀시트 콜백
        behavior.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                bottomSheetState = newState
                fab.visibility = if (selectedCalendarDay == today && newState != BottomSheetBehavior.STATE_COLLAPSED) View.VISIBLE else View.GONE
            }
            override fun onSlide(bottomSheet: View, slideOffset: Float) {}
        })

        return view
    }

    // 선택된 날짜 데코레이터 갱신 함수
    private fun updateSelectedDateDecorator() {
        selectedTextDecorator?.let { calendarView.removeDecorator(it) }
        todayTextDecorator?.let { calendarView.removeDecorator(it) }

        val textColor = if (selectedCalendarDay == today) {
            ContextCompat.getColor(requireContext(), R.color.white)
        } else {
            ContextCompat.getColor(requireContext(), R.color.white)
        }

        selectedTextDecorator = SelectedDateTextColorDecorator(selectedCalendarDay, textColor)
        calendarView.addDecorator(selectedTextDecorator!!)

        // 선택된 날짜가 오늘이 아닌 경우 오렌지 텍스트 다시 표시
        if (selectedCalendarDay != today || isFirstLaunch) {
            val orangeColor = ContextCompat.getColor(requireContext(), R.color.elixir_orange)
            todayTextDecorator = TodayTextColorDecorator(today, orangeColor)
            calendarView.addDecorator(todayTextDecorator!!)
        }

        isFirstLaunch = false
    }

    // 선택된 날짜의 텍스트 색 변경
    class SelectedDateTextColorDecorator(
        private val selectedDay: CalendarDay,
        private val color: Int
    ) : DayViewDecorator {
        override fun shouldDecorate(day: CalendarDay): Boolean = day == selectedDay
        override fun decorate(view: DayViewFacade) {
            view.addSpan(ForegroundColorSpan(color))
        }
    }

    // 오늘 날짜만 오렌지 텍스트로 표시
    class TodayTextColorDecorator(
        private val day: CalendarDay,
        private val color: Int
    ) : DayViewDecorator {
        override fun shouldDecorate(date: CalendarDay): Boolean = date == day
        override fun decorate(view: DayViewFacade) {
            view.addSpan(ForegroundColorSpan(color))
        }
    }

    // 요일 커스텀 포매터
    class CustomWeekDayFormatter(context: Context) : WeekDayFormatter {
        private val weekdays = context.resources.getStringArray(R.array.custom_weekdays)
        override fun format(dayOfWeek: Int): CharSequence = weekdays[(dayOfWeek - 1) % 7]
    }

    // 월 커스텀 포매터
    class CustomTitleFormatter(context: Context) : TitleFormatter {
        private val months = context.resources.getStringArray(R.array.custom_months)
        override fun format(day: CalendarDay): CharSequence {
            return "${day.year}.${months[day.month]}"
        }
    }

    // 점수별 데코레이터 (점 색상)
    private fun processDietLogScores(calendarView: MaterialCalendarView) {
        val dotMap = mutableMapOf<CalendarDay, MutableList<Int>>()
        eventMap.forEach { (dateStr, items) ->
            val (year, month, day) = dateStr.split("-").map { it.toInt() }
            val calendarDay = CalendarDay.from(year, month - 1, day)
            val colors = items.map { getColorByScore(it.dietScore) }
            dotMap[calendarDay] = colors.toMutableList()
        }
        dotMap.forEach { (day, colors) ->
            val defaultTextColor = Color.BLACK
            calendarView.addDecorator(MultipleDotDecorator(colors, day, defaultTextColor))
        }
    }

    // 점수에 따라 색상 반환
    private fun getColorByScore(score: Int) = when (score) {
        5 -> Color.parseColor("#E738F4")
        4 -> Color.parseColor("#38F4F4")
        3 -> Color.parseColor("#D5F438")
        2 -> Color.parseColor("#FDE950")
        1 -> Color.parseColor("#FD5050")
        else -> Color.GRAY
    }
 
    // 더미 이벤트 추가
    private fun addDummyEvents() {
        eventMap["2025-03-29"] = mutableListOf(
            DietLogItem("아침", "흑임자 연근샐러드", listOf("연근", "흑임자", "식초", "깨"), 5, R.drawable.png_recipe_sample),
            DietLogItem("점심", "김치찌개", listOf("김치", "돼지고기", "두부"), 4),
            DietLogItem("저녁", "스테이크", listOf("소고기", "감자", "아스파라거스"), 3)
        )
        eventMap["2025-03-30"] = mutableListOf(
            DietLogItem("아침", "토스트", listOf("빵이좋아좋아", "계란", "딸기잼"), 2),
            DietLogItem("점심", "된장찌개", listOf("된장", "두부", "애호박"), 1),
            DietLogItem("저녁", "샐러드", listOf("양상추", "토마토", "닭가슴살"), 5),
            DietLogItem("간식", "샐러드", listOf("양상추", "토마토", "닭가슴살"), 5)
        )
    }

    // 선택된 날짜의 식단 업데이트
    private fun updateEventList(selectedDate: String) {
        val events = eventMap[selectedDate]
        if (events.isNullOrEmpty()) {
            eventListView.visibility = View.GONE
            emptyDietText.visibility = View.VISIBLE
        } else {
            eventListView.visibility = View.VISIBLE
            emptyDietText.visibility = View.GONE
            eventAdapter.updateData(events)
        }
    }

    // 식단 클릭 이벤트
    private fun onDietLogClick(item: DietLogItem) {
        Log.d("CalendarFragment", "클릭된 식단: \${item.dietName} - \${item.dietTimes} - \${item.dietIngredients}")
    }

    // 여러 색상 점 데코레이터
    class MultipleDotDecorator(private val colors: List<Int>, private val date: CalendarDay, private val textColor: Int) : DayViewDecorator {
        override fun shouldDecorate(day: CalendarDay): Boolean = day == date
        override fun decorate(view: DayViewFacade) {
            view.addSpan(CustomDotSpan(colors))
            view.addSpan(ForegroundColorSpan(textColor))
        }
    }

    // 점을 그리는 커스텀 Span
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
}
