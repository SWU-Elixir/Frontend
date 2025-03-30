package com.example.elixir

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.prolificinteractive.materialcalendarview.MaterialCalendarView
import androidx.core.content.ContextCompat
import android.util.Log
import android.widget.LinearLayout
import androidx.cardview.widget.CardView
import com.google.android.material.bottomsheet.BottomSheetBehavior

class CalendarFragment : Fragment() {


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_calendar, container, false)

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

        val BottomSheet = view.findViewById<CardView>(R.id.bottomsheet)
        val BottomSheetBehavior = BottomSheetBehavior.from(BottomSheet)


        return view
    }

    //calendarView.setOnDateChangedListener(this);
    //    override fun onDateChanged(widget: MaterialCalendarView, date: CalendarDay, selected: Boolean) {
//        if (date != null) {
//            val selectedDate = "${date.year}-${date.month + 1}-${date.day}"
//            val bottomSheet = CalendarBottomSheetFragment.newInstance(selectedDate)
//            bottomSheet.show(supportFragmentManager, "calendarBottomSheet")
//        }
//    }
}
