package com.clausfonseca.rosacha.view.chart

import com.github.mikephil.charting.components.AxisBase
import com.github.mikephil.charting.formatter.ValueFormatter

class MonthValueFormatter : ValueFormatter() {


    override fun getAxisLabel(value: Float, axis: AxisBase?): String {
        return when (value) {
            0.0f -> "January"
            1.0f -> "February"
            2.0f -> "March"
            3.0f -> "April"
            4.0f -> "May"
            5.0f -> "June"
            6.0f -> "July"
            7.0f -> "August"
            8.0f -> "September"
            9.0f ->  "October"
            10.0f -> "November"
            11.0f -> "December"
            else -> ""
        }
    }
}
