package com.example.elixir

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
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.prolificinteractive.materialcalendarview.*
import com.prolificinteractive.materialcalendarview.format.TitleFormatter
import com.prolificinteractive.materialcalendarview.format.WeekDayFormatter

data class DietLogItem(
    val dietTimes: String,       // 아침, 점심, 저녁, 간식
    val dietName: String,   // 흑임자 연근샐러드 등
    val dietIngredients: List<String>, // 재료 목록 (연근, 흑임자, 식초, 깨 등)
    val dietScore: Int, // 점수 표시
    val dietImageRes: Int? = null
)


class CalendarFragment : Fragment() {

    private lateinit var eventListView: ListView
    private lateinit var eventAdapter: DietLogAdapter
    private val eventMap = mutableMapOf<String, MutableList<DietLogItem>>() // 날짜별 식단 저장
    private lateinit var emptyDietText: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_calendar, container, false)

        // 리스트뷰 설정
        eventListView = view.findViewById(R.id.dietLogList)
        eventAdapter = DietLogAdapter(requireContext(), mutableListOf()) { item ->
            onDietLogClick(item)
        }
        eventListView.adapter = eventAdapter
        emptyDietText = view.findViewById(R.id.emptyDietText)

        // 더미 데이터 추가
        addDummyEvents()

        // 캘린더 뷰 설정
        val calendarView = view.findViewById<MaterialCalendarView>(R.id.calendarView)

        // 커스텀 요일 및 월 설정 적용 (Fragment에서 requireContext() 사용)
        calendarView.setWeekDayFormatter(CustomWeekDayFormatter(requireContext()))
        calendarView.setTitleFormatter(CustomTitleFormatter(requireContext()))

        try {
            val todayDrawable = ContextCompat.getDrawable(requireContext(), R.drawable.bg_calendar_circletoday)
            val textColor = ContextCompat.getColor(requireContext(), R.color.elixir_orange)
            if (todayDrawable != null) {
                val todayDecorator = CalendarTodayDecorator(requireContext(), todayDrawable, textColor)
                calendarView.addDecorator(todayDecorator)
            } else {
                Log.e("CalendarFragment", "drawable 리소스를 찾을 수 없습니다.")
            }
        } catch (e: Exception) {
            Log.e("CalendarFragment", "오늘 날짜 배경 설정 오류: ${e.message}")
        }
        calendarView.setOnDateChangedListener { _, date, _ ->
            val selectedDate = "%04d-%02d-%02d".format(date.year, date.month + 1, date.day)
            Log.d("CalendarFragment", "선택된 날짜: $selectedDate")
            updateEventList(selectedDate)
        }

        // 앱 시작 시 오늘 날짜 식단 자동으로 불러오기
        val today = CalendarDay.today()
        val todayFormatted = "%04d-%02d-%02d".format(today.year, today.month + 1, today.day)
        updateEventList(todayFormatted)

        // 점수별 색상 표시
        processDietLogScores(calendarView)

        // BottomSheet 설정
        val bottomSheet = view.findViewById<CardView>(R.id.bottomSheet)
        val bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet)

        // Fab 설정
        val fab: FloatingActionButton = view.findViewById(R.id.fab)
        fab.setOnClickListener {
            // 원하는 동작 수행
            Log.d("CalendarFragment", "FAB 클릭됨")
        }
        val behavior = BottomSheetBehavior.from(bottomSheet)

        behavior.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                when (newState) {
                    BottomSheetBehavior.STATE_COLLAPSED -> {
                        fab.hide() // 펼쳐졌을 때 FAB 보이기
                    }
                    else -> {
                        fab.show() // 나머지 상태에서는 숨기기
                    }
                }
            }

            override fun onSlide(bottomSheet: View, slideOffset: Float) {
                // 선택사항: 슬라이드에 따라 애니메이션 줄 수도 있음
            }
        })

        return view
    }

    // 요일 커스텀 설정
    class CustomWeekDayFormatter(context: Context) : WeekDayFormatter {
        private val weekdays = context.resources.getStringArray(R.array.custom_weekdays)

        override fun format(dayOfWeek: Int): CharSequence {
            return weekdays[(dayOfWeek - 1) % 7] // dayOfWeek가 1부터 시작하므로 0 기반 인덱스로 변환
        }
    }

    // 월 커스텀 설정
    class CustomTitleFormatter(context: Context) : TitleFormatter {
        private val months = context.resources.getStringArray(R.array.custom_months)
        override fun format(day: CalendarDay): CharSequence {
            return "${day.year}.${months[day.month]}"
        }
    }


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


    private fun getColorByScore(score: Int) = when (score) {
        5 -> Color.parseColor("#38F4F4")
        4 -> Color.parseColor("#D5F438")
        3 -> Color.parseColor("#FDE950")
        2 -> Color.parseColor("#FDA150")
        1 -> Color.parseColor("#FD5050")
        else -> Color.GRAY
    }

    // 더미 식단 데이터 추가
    private fun addDummyEvents() {
        eventMap["2025-03-29"] = mutableListOf(
            DietLogItem("아침", "흑임자 연근샐러드", listOf("연근", "흑임자", "식초", "깨"), 5, dietImageRes = R.drawable.png_recipe_sample),
            DietLogItem("점심", "김치찌개",  listOf("김치", "돼지고기", "두부"), 4),
            DietLogItem("저녁", "스테이크", listOf("소고기", "감자", "아스파라거스"), 3)
        )
        eventMap["2025-03-30"] = mutableListOf(
            DietLogItem("아침", "토스트", listOf("빵이좋아좋아", "계란", "딸기잼", "딸기잼", "딸기잼"), 2),
            DietLogItem("점심", "된장찌개",  listOf("된장", "두부", "애호박"), 1),
            DietLogItem("저녁", "샐러드", listOf("양상추", "토마토", "닭가슴살"), 5),
            DietLogItem("간식", "샐러드", listOf("양상추", "토마토", "닭가슴살"), 5)

        )
    }

    // 선택된 날짜에 맞는 식단 업데이트
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

    // 식단 아이템 클릭 시 동작
    private fun onDietLogClick(item: DietLogItem) {
        Log.d("CalendarFragment", "클릭된 식단: ${item.dietName} - ${item.dietTimes} - ${item.dietIngredients}")
    }


    class MultipleDotDecorator(private val colors: List<Int>, private val date: CalendarDay, private val textColor: Int) : DayViewDecorator {
        override fun shouldDecorate(day: CalendarDay): Boolean = day == date

        override fun decorate(view: DayViewFacade) {
            view.addSpan(CustomDotSpan(colors)) // 커스텀 DotSpan 사용
            view.addSpan(ForegroundColorSpan(textColor))
        }
    }


    class CustomDotSpan(private val colors: List<Int>) : LineBackgroundSpan {
        private val radius = 6f // 점 크기 설정
        private val verticalSpacing = 18f //  위아래 간격 추가

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
            // 점들의 총 너비 계산
            val totalWidth = (colors.size * radius * 2) + ((colors.size - 1) * 10f)
            var offsetX = (left + right) / 2 - (totalWidth / 2) + radius // 중앙정렬
            var offsetY = baseline + radius + verticalSpacing // 점을 아래로 내리는 간격 추가

            // 각 색상에 대해 점 그리기
            colors.forEach { color ->
                paint.color = color
                canvas.drawCircle(offsetX, offsetY, radius, paint) // Y 좌표 조정하여 간격 추가
                offsetX += radius * 2 + 10f // 점 간격 설정
            }
        }
    }

}



