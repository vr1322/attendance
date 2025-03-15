package com.example.attendance;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.text.style.StyleSpan;

import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.DayViewDecorator;
import com.prolificinteractive.materialcalendarview.DayViewFacade;
import com.prolificinteractive.materialcalendarview.format.DayFormatter;

import java.util.HashSet;

public class CustomDecorator implements DayViewDecorator, DayFormatter {

    private final HashSet<CalendarDay> dates;
    private final int color;

    public CustomDecorator(int color, HashSet<CalendarDay> dates) {
        this.color = color;
        this.dates = dates;
    }

    @Override
    public boolean shouldDecorate(CalendarDay day) {
        return dates.contains(day);
    }

    @Override
    public void decorate(DayViewFacade view) {
        view.addSpan(new ForegroundColorSpan(color)); // Text color
        view.addSpan(new StyleSpan(android.graphics.Typeface.BOLD)); // Bold text
        view.addSpan(new RelativeSizeSpan(1.2f)); // Slightly bigger text
    }
    @Override
    public String format(CalendarDay day) {
        return day != null ? day.getDay() + "" : "";
    }

}
