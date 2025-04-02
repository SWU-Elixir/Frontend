package com.example.elixir

import android.graphics.*
import android.os.Bundle
import android.text.TextPaint
import android.text.style.ForegroundColorSpan
import android.text.style.LineBackgroundSpan
import android.text.style.ReplacementSpan
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ListView
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.prolificinteractive.materialcalendarview.*
import com.prolificinteractive.materialcalendarview.spans.DotSpan

data class DietLogItem(
    val mealTimes: String,       // 아침, 점심, 저녁, 간식
    val mealName: String,   // 흑임자 연근샐러드 등
    val ingredients: List<String>, // 재료 목록 (연근, 흑임자, 식초, 깨 등)
    val score: Int // 점수 표시
)


class CalendarFragment : Fragment() {

    private lateinit var eventListView: ListView
    private lateinit var eventAdapter: DietLogAdapter
    private val eventMap = mutableMapOf<String, MutableList<DietLogItem>>() // 날짜별 식단 저장

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_calendar, container, false)

        // 리스트뷰 설정
        eventListView = view.findViewById(R.id.listview)
        eventAdapter = DietLogAdapter(requireContext(), mutableListOf()) { meal ->
            onMealItemClick(meal)
        }
        eventListView.adapter = eventAdapter

        // 더미 데이터 추가
        addDummyEvents()

        // 캘린더 뷰 설정
        val calendarView = view.findViewById<MaterialCalendarView>(R.id.calendarView)
        try {
            val todayDrawable = ContextCompat.getDrawable(requireContext(), R.drawable.bg_calendar_circletoday)
            val textColor = ContextCompat.getColor(requireContext(), R.color.elixir_orange)
            if (todayDrawable != null) {
                val todayDecorator = TodayDecorator(requireContext(), todayDrawable, textColor)
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

        // 점수별 색상 표시
        processDietLogScores(calendarView)

        // BottomSheet 설정
        val bottomSheet = view.findViewById<CardView>(R.id.bottomsheet)
        val bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet)

        return view
    }

    private fun setupCalendarDecorations(calendarView: MaterialCalendarView) {
        try {
            // 오늘 날짜 표시
            ContextCompat.getDrawable(requireContext(), R.drawable.bg_calendar_circletoday)?.let {
                calendarView.addDecorator(
                    TodayDecorator(
                        requireContext(),
                        it,
                        ContextCompat.getColor(requireContext(), R.color.elixir_orange)
                    )
                )
            }

            // 점수별 데코레이터 추가
            processDietLogScores(calendarView)

        } catch (e: Exception) {
            Log.e("CalendarFragment", "데코레이터 설정 오류: ${e.message}")
        }

        calendarView.setOnDateChangedListener { _, date, _ ->
            updateEventList("%04d-%02d-%02d".format(date.year, date.month + 1, date.day))
        }
    }

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
            DietLogItem("아침", "흑임자 연근샐러드", listOf("연근", "흑임자", "식초", "깨"), 5),
            DietLogItem("점심", "김치찌개",  listOf("김치", "돼지고기", "두부"), 4),
            DietLogItem("저녁", "스테이크", listOf("소고기", "감자", "아스파라거스"), 3)
        )
        eventMap["2025-03-30"] = mutableListOf(
            DietLogItem("아침", "토스트", listOf("빵이좋아좋아", "계란", "딸기잼", "딸기잼", "딸기잼"), 2),
            DietLogItem("점심", "된장찌개",  listOf("된장", "두부", "애호박"), 1),
            DietLogItem("저녁", "샐러드", listOf("양상추", "토마토", "닭가슴살"), 5)
        )
    }

    // 선택된 날짜에 맞는 식단 업데이트
    private fun updateEventList(selectedDate: String) {
        val events = eventMap[selectedDate] ?: mutableListOf(DietLogItem("없음", "식단 없음",  emptyList(), 0))
        eventAdapter.updateData(events)
    }

    // 식단 아이템 클릭 시 동작
    private fun onMealItemClick(meal: DietLogItem) {
        //Log.d("CalendarFragment", "클릭된 식단: ${meal.mealType} - ${meal.mealTitle} - ${meal.ingredients}")
        // 추가적인 동작을 수행할 수 있음 (예: 상세 보기, 편집 등)
    }


    class MultipleDotDecorator(private val colors: List<Int>, private val date: CalendarDay, private val textColor: Int) : DayViewDecorator {
        override fun shouldDecorate(day: CalendarDay): Boolean = day == date

        override fun decorate(view: DayViewFacade) {
            view.addSpan(CustomDotSpan(colors)) // 커스텀 DotSpan 사용
            view.addSpan(ForegroundColorSpan(textColor))
        }
    }


    class CustomDotSpan(private val colors: List<Int>) : LineBackgroundSpan {
        private val radius = 8f // 점 크기 설정

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
            val totalWidth = (colors.size * radius * 2) + ((colors.size - 1) * 4f)
            var offsetX = (left + right) / 2 - (totalWidth / 2) + radius // 중앙 정렬

            // 각 색상에 대해 점 그리기
            colors.forEach { color ->
                paint.color = color
                canvas.drawCircle(offsetX, baseline + radius + 4f, radius, paint) // 점 위치 조정
                offsetX += radius * 2 + 4f // 점 간격 설정
            }
        }
    }

}



