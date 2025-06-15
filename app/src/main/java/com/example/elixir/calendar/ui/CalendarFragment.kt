package com.example.elixir.calendar.ui

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.*
import android.net.Uri
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
import com.example.elixir.ToolbarActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.elixir.R
import com.example.elixir.RetrofitClient
import com.example.elixir.calendar.data.DietLogData
import com.example.elixir.calendar.data.MealDto
import com.example.elixir.calendar.network.db.DietLogRepository
import com.example.elixir.calendar.viewmodel.MealViewModel
import com.example.elixir.calendar.viewmodel.MealViewModelFactory
import com.example.elixir.databinding.FragmentCalendarBinding
import com.example.elixir.ingredient.network.IngredientDB
import com.example.elixir.ingredient.network.IngredientRepository
import com.example.elixir.member.network.MemberDB
import com.example.elixir.member.network.MemberRepository
import com.example.elixir.network.AppDatabase
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.gson.Gson
import com.prolificinteractive.materialcalendarview.*
import com.prolificinteractive.materialcalendarview.format.TitleFormatter
import com.prolificinteractive.materialcalendarview.format.WeekDayFormatter
import org.threeten.bp.LocalDateTime
import org.threeten.bp.format.DateTimeFormatter

// ----------------------------- 프래그먼트 클래스 -------------------------------------
class CalendarFragment : Fragment(), OnMealClickListener {
    override fun onMealClick(item: DietLogData) {
        val intent = Intent(requireContext(), ToolbarActivity::class.java).apply {
            putExtra("mode", 4)
            putExtra("mealName", item.dietTitle)
            putExtra("mealData", Gson().toJson(item))
            putExtra("year", item.time.year)
            putExtra("month", item.time.monthValue)
            putExtra("day", item.time.dayOfMonth)
            putExtra("dietLogId", item.id) // 식단 기록 ID 추가
        }
        mealDetailLauncher.launch(intent)
    }

    // 뷰 바인딩 변수
    private var _binding: FragmentCalendarBinding? = null
    private val binding get() = _binding!!

    private lateinit var dietRepository: DietLogRepository
    private lateinit var memberRepository: MemberRepository
    private lateinit var ingredientRepository: IngredientRepository

    private val mealViewModel: MealViewModel by viewModels {
        MealViewModelFactory(dietRepository, memberRepository, ingredientRepository)
    }

    // 상세 화면 런처 등록
    private lateinit var mealDetailLauncher: ActivityResultLauncher<Intent>

    // 어댑터 및 데이터 변수
    private lateinit var mealAdapter: MealListAdapter
    private val eventMap = mutableMapOf<String, MutableList<DietLogData>>() // 날짜별 식단 데이터 저장
    private var selectedCalendarDay: CalendarDay = CalendarDay.today() // 선택된 날짜
    private var selectedTextDecorator: SelectedDateTextColorDecorator? = null // 선택 날짜 텍스트 색상 데코레이터
    private var todayTextDecorator: TodayTextColorDecorator? = null // 오늘 날짜 텍스트 색상 데코레이터
    private val today = CalendarDay.today() // 오늘 날짜
    private var isFirstLaunch = true // 첫 실행 여부

    // CalendarTodayDecorator 인스턴스를 멤버 변수로 유지
    private var calendarTodayDecorator: CalendarTodayDecorator? = null

    // Dot 데코레이터들을 저장할 리스트 추가
    private val dotDecorators = mutableListOf<MultipleDotDecorator>()

    // DietLogFragment 띄우기
    private lateinit var dietLogLauncher: ActivityResultLauncher<Intent>

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

        // -------------------- 리스트뷰 설정 --------------------
        // 1. 어댑터를 빈 리스트로 먼저 생성
        mealAdapter = MealListAdapter(requireContext(), mutableListOf(), this)
        binding.mealPlanList.adapter = mealAdapter

        // 2. ViewModel에서 데이터 요청 (초기 선택 날짜)
        mealViewModel.getDietLogsByDate(
            "%04d-%02d-%02d".format(selectedCalendarDay.year, selectedCalendarDay.month + 1, selectedCalendarDay.day)
        )

        // 3. LiveData observe로 데이터가 오면 어댑터에 전달
        mealViewModel.dailyDietLogs.observe(viewLifecycleOwner) { mealList ->
            val dietLogList = mealList?.map { convertMealDtoToDietLogData(it) } ?: emptyList()
            mealAdapter.updateData(dietLogList)
        }

        // -------------------- 캘린더 설정 ----------------------
        binding.calendarView.setWeekDayFormatter(CustomWeekDayFormatter(requireContext()))
        binding.calendarView.setTitleFormatter(CustomTitleFormatter(requireContext()))

        mealViewModel.getAllDietLogs(30)

        // 오늘 날짜 배경 원 데코레이터 (최초 1회만 추가)
        try {
            val todayDrawable = ContextCompat.getDrawable(requireContext(), R.drawable.bg_oval_outline_orange)
            todayDrawable?.let {
                calendarTodayDecorator = CalendarTodayDecorator(requireContext(), it)
                binding.calendarView.addDecorator(calendarTodayDecorator!!)
            }
        } catch (e: Exception) {
            Log.e("CalendarFragment", "오늘 날짜 배경 설정 오류: ${e.message}")
        }


        // ----------------- FAB 및 바텀시트 ---------------------
        val behavior = BottomSheetBehavior.from(binding.bottomSheet)
        binding.fab.hide()
        var bottomSheetState = BottomSheetBehavior.STATE_COLLAPSED
        behavior.peekHeight = 130

        // 초기 날짜 설정
        if (isFirstLaunch) {
            selectedCalendarDay = today
            isFirstLaunch = false
        }

        // 식재료 불러오기
        mealViewModel.loadIngredients()
        mealViewModel.ingredientList.observe(viewLifecycleOwner) { ingredientList ->
            val ingredientMap = ingredientList.associateBy { it.id }
            mealAdapter.setIngredientMap(ingredientMap)
        }

        // 날짜 선택 이벤트 처리
        binding.calendarView.setOnDateChangedListener { _, date, _ ->
            selectedCalendarDay = date
            val selectedDateStr = "%04d-%02d-%02d".format(date.year, date.month + 1, date.day)
            mealViewModel.getDietLogsByDate(selectedDateStr)
            mealViewModel.getAllDietLogs(30)

            updateSelectedDateDecorator()

            binding.fab.visibility = if (selectedCalendarDay == today && bottomSheetState != BottomSheetBehavior.STATE_COLLAPSED) View.VISIBLE else View.GONE
        }

        // FAB 클릭 이벤트
        binding.fab.setOnClickListener {
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
                // 수정/삭제/추가 후 캘린더 데이터를 다시 로드하여 UI 갱신
                val selectedDateStr = "%04d-%02d-%02d".format(selectedCalendarDay.year, selectedCalendarDay.month + 1, selectedCalendarDay.day)
                mealViewModel.getDietLogsByDate(selectedDateStr) // 현재 선택된 날짜의 데이터 다시 불러오기
            } else {
                Toast.makeText(context, "식단 작업 실패", Toast.LENGTH_SHORT).show()
            }
        }

        // 상세 화면 런처 등록
        mealDetailLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                // 상세 화면(DietLogFragment)에서 돌아온 후 캘린더 데이터 다시 로드
                val selectedDateStr = "%04d-%02d-%02d".format(selectedCalendarDay.year, selectedCalendarDay.month + 1, selectedCalendarDay.day)
                mealViewModel.getDietLogsByDate(selectedDateStr)
            }
        }

        // 바텀시트 설정
        behavior.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                bottomSheetState = newState
                binding.fab.visibility = if (selectedCalendarDay == today && newState != BottomSheetBehavior.STATE_COLLAPSED) View.VISIBLE else View.GONE
            }
            override fun onSlide(bottomSheet: View, slideOffset: Float) {}
        })

        // LiveData 관찰 시작
        observeDietLogs()
    }

    /**
     * 프래그먼트 재개 시 상태 복원
     */
    override fun onResume() {
        super.onResume()
        // 선택된 날짜 스타일 복원
        updateSelectedDateDecorator()
        mealViewModel.getAllDietLogs(30)
        val selectedDateStr = "%04d-%02d-%02d".format(selectedCalendarDay.year, selectedCalendarDay.month + 1, selectedCalendarDay.day)
        mealViewModel.getDietLogsByDate(selectedDateStr) // 데이터 다시 불러오기
        // 캘린더 선택 날짜는 항상 유지
        binding.calendarView.setSelectedDate(selectedCalendarDay)
    }

    // ------------------------ 데코레이터 클래스 ---------------------------

    /**
     * 선택된 날짜의 텍스트 색상 변경 데코레이터 업데이트
     * 점 데코레이터는 유지하면서 선택/오늘 날짜 데코레이터만 갱신
     */
    private fun updateSelectedDateDecorator() {
        // 기존 선택 날짜 데코레이터만 제거
        selectedTextDecorator?.let {
            binding.calendarView.removeDecorator(it)
            selectedTextDecorator = null
        }

        todayTextDecorator?.let {
            binding.calendarView.removeDecorator(it)
            todayTextDecorator = null
        }

        // 새로운 선택 날짜 데코레이터 추가
        val textColor = ContextCompat.getColor(requireContext(), R.color.white)
        selectedTextDecorator = SelectedDateTextColorDecorator(selectedCalendarDay, textColor)
        binding.calendarView.addDecorator(selectedTextDecorator!!)

        // 오늘 날짜가 선택된 날짜와 다를 경우만 오늘 날짜 데코레이터 추가
        if (selectedCalendarDay != today) {
            val orangeColor = ContextCompat.getColor(requireContext(), R.color.elixir_orange)
            todayTextDecorator = TodayTextColorDecorator(today, orangeColor)
            binding.calendarView.addDecorator(todayTextDecorator!!)
        }

        // 변경사항 즉시 반영 (점 데코레이터는 유지됨)
        binding.calendarView.invalidateDecorators()
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
     * 점 데코레이터만 선별적으로 갱신
     */
    private fun processDietLogScores(calendarView: MaterialCalendarView) {
        // 기존 dot 데코레이터만 캘린더에서 제거
        dotDecorators.forEach { decorator ->
            calendarView.removeDecorator(decorator)
        }
        dotDecorators.clear()

        val dotMap = mutableMapOf<CalendarDay, MutableList<Int>>()
        eventMap.forEach { (dateStr, items) ->
            val (year, month, day) = dateStr.split("-").map { it.toInt() }
            val calendarDay = CalendarDay.from(year, month - 1, day)
            val colors = items.map { getColorByScore(it.score) }
            dotMap[calendarDay] = colors.toMutableList()
        }

        dotMap.forEach { (day, colors) ->
            val decorator = MultipleDotDecorator(colors, day)
            dotDecorators.add(decorator)
        }

        // 새로 생성된 점 데코레이터들을 캘린더에 추가
        dotDecorators.forEach { decorator ->
            calendarView.addDecorator(decorator)
        }

        calendarView.invalidateDecorators()
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
    class MultipleDotDecorator(private val colors: List<Int>, private val date: CalendarDay) : DayViewDecorator {
        override fun shouldDecorate(day: CalendarDay): Boolean = day == date
        override fun decorate(view: DayViewFacade) {
            view.addSpan(CustomDotSpan(colors))
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

    // ------------------------ 데이터 변환 메서드 ---------------------------
    /**
     * DietLogData를 MealDto로 변환
     * @param dietLogData 변환할 DietLogData 객체
     * @return 변환된 MealDto 객체
     */
    private fun convertMealDtoToDietLogData(mealDto: MealDto): DietLogData {
        return DietLogData(
            id = mealDto.id,  // MealDto의 id를 DietLogData의 id로 사용
            dietImg = mealDto.imageUrl ?: "",
            time = LocalDateTime.parse(mealDto.time),  // String → LocalDateTime 변환
            dietTitle = mealDto.name,
            dietCategory = mealDto.type,
            ingredientTags = mealDto.ingredientTagId,
            score = mealDto.score
        )
    }

    // observeDietLogs에서 eventMap을 초기화하고 데코레이터를 적용합니다.
    private fun observeDietLogs() {
        mealViewModel.dailyDietLogs.observe(viewLifecycleOwner) { mealList ->
            // eventMap 클리어: 항상 최신 데이터로 갱신
            eventMap.clear()
            mealList?.forEach { mealDto ->
                val date = mealDto.time.substring(0, 10)
                val dietLogData = convertMealDtoToDietLogData(mealDto)
                val list = eventMap[date] ?: mutableListOf()
                list.add(dietLogData)
                eventMap[date] = list
            }
            val selectedDateStr = "%04d-%02d-%02d".format(selectedCalendarDay.year, selectedCalendarDay.month + 1, selectedCalendarDay.day)
            updateEventList(selectedDateStr)

            // 데이터가 변경될 때마다 점 데코레이터를 새로 적용
            processDietLogScores(binding.calendarView)
        }
    }

    /**
     * 뷰 제거 시 바인딩 해제
     */
    override fun onDestroyView() {
        // 프래그먼트가 destroy될 때 데코레이터를 모두 제거
        _binding?.calendarView?.removeDecorators()
        dotDecorators.clear() // dot 데코레이터 리스트도 클리어

        super.onDestroyView()
        _binding = null // binding 객체를 null로 설정하여 메모리 누수 방지
    }
}