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
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.elixir.R
import com.example.elixir.ToolbarActivity
import com.example.elixir.databinding.FragmentCalendarBinding
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.prolificinteractive.materialcalendarview.*
import com.prolificinteractive.materialcalendarview.format.TitleFormatter
import com.prolificinteractive.materialcalendarview.format.WeekDayFormatter
import java.math.BigInteger

// 식단 아이템 데이터 클래스
// 각 식사 시간, 식단명, 재료, 점수, 이미지 리소스를 포함
// 점수는 1~5로, 시각화 시 점 색상 구분에 사용


// ----------------------------- 프래그먼트 -------------------------------------
class CalendarFragment : Fragment() {
    // 바인딩
    private lateinit var calendarBinding: FragmentCalendarBinding

    // UI 컴포넌트 선언
    private lateinit var mealAdapter: MealListAdapter

    // 날짜 관련 변수
    private val eventMap = mutableMapOf<String, MutableList<MealPlanData>>()
    private var selectedCalendarDay: CalendarDay = CalendarDay.today()
    private var selectedTextDecorator: SelectedDateTextColorDecorator? = null
    private var todayTextDecorator: TodayTextColorDecorator? = null
    private val today = CalendarDay.today()
    private var isFirstLaunch = true

    // DietLogFragment 띄우기
    private lateinit var dietLogLauncher: ActivityResultLauncher<Intent>

    // 바텀시트 상태 저장용
    private var bottomSheetState = BottomSheetBehavior.STATE_COLLAPSED

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        calendarBinding = FragmentCalendarBinding.inflate(inflater, container, false)
        return calendarBinding.root
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 리스트뷰 및 어댑터 설정
        mealAdapter = MealListAdapter(requireContext(), mutableListOf(), fragmentManager = parentFragmentManager)
        calendarBinding.mealPlanList.adapter = mealAdapter
        addDummyEvents()

        // 캘린더 설정
        calendarBinding.calendarView.apply {
            setWeekDayFormatter(CustomWeekDayFormatter(requireContext()))
            setTitleFormatter(CustomTitleFormatter(requireContext()))
            try {
                val todayDrawable = ContextCompat.getDrawable(requireContext(), R.drawable.bg_oval_outline_orange)
                todayDrawable?.let {
                    addDecorator(CalendarTodayDecorator(requireContext(), it))
                }
            } catch (e: Exception) {
                Log.e("CalendarFragment", "오늘 날짜 배경 설정 오류: ${e.message}")
            }
        }

        // 오늘 날짜 기본 선택 및 점수 데코레이터 처리
        selectedCalendarDay = today
        updateEventList(selectedCalendarDay.toString())
        updateSelectedDateDecorator()
        processDietLogScores(calendarBinding.calendarView)

        // 날짜 선택 리스너
        calendarBinding.calendarView.setOnDateChangedListener { _, date, _ ->
            selectedCalendarDay = date
            val selectedDateStr = "%04d-%02d-%02d".format(date.year, date.month + 1, date.day)
            updateEventList(selectedDateStr)
            updateSelectedDateDecorator()

            calendarBinding.fab.visibility = if (selectedCalendarDay == today && bottomSheetState != BottomSheetBehavior.STATE_COLLAPSED) View.VISIBLE else View.GONE
        }

        // FAB 설정
        calendarBinding.fab.hide()
        calendarBinding.fab.setOnClickListener {
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
                calendarBinding.fab.visibility = if (selectedCalendarDay == today && newState != BottomSheetBehavior.STATE_COLLAPSED) View.VISIBLE else View.GONE
            }
            override fun onSlide(bottomSheet: View, slideOffset: Float) {}
        })
    }

    // ------------------------ 데코레이터 ---------------------------

    // 선택된 날짜의 텍스트 색상 변경 데코레이터
    private fun updateSelectedDateDecorator() {
        selectedTextDecorator?.let { calendarBinding.calendarView.removeDecorator(it) }
        todayTextDecorator?.let { calendarBinding.calendarView.removeDecorator(it) }

        val textColor = ContextCompat.getColor(requireContext(), R.color.white)
        selectedTextDecorator = SelectedDateTextColorDecorator(selectedCalendarDay, textColor)
        calendarBinding.calendarView.addDecorator(selectedTextDecorator!!)
        if (selectedCalendarDay != today || isFirstLaunch) {
            val orangeColor = ContextCompat.getColor(requireContext(), R.color.elixir_orange)
            todayTextDecorator = TodayTextColorDecorator(today, orangeColor)
            calendarBinding.calendarView.addDecorator(todayTextDecorator!!)
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
            // 이벤트가 없으면 adapter 비우기
            mealAdapter.updateData(mutableListOf())
        } else {
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

}