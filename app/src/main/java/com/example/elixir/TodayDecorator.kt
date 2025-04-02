package com.example.elixir

import android.content.Context
import android.graphics.drawable.Drawable
import android.text.style.ForegroundColorSpan
import com.prolificinteractive.materialcalendarview.CalendarDay
import com.prolificinteractive.materialcalendarview.DayViewDecorator
import com.prolificinteractive.materialcalendarview.DayViewFacade

class TodayDecorator(private val context: Context, private val drawable: Drawable, private val textColor: Int) : DayViewDecorator {

    private val today = CalendarDay.today()

    override fun shouldDecorate(day: CalendarDay): Boolean {
        return day == today
    }

    override fun decorate(view: DayViewFacade) {
        // 오늘 날짜의 배경을 변경
        view.setBackgroundDrawable(drawable)
        // 오늘 날짜의 글씨 색상 변경
        //view.addSpan(ForegroundColorSpan(textColor))
    }

}
