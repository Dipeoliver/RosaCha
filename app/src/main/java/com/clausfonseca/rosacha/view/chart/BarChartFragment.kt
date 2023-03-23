package com.clausfonseca.rosacha.view.chart

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.clausfonseca.rosacha.R
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.utils.ColorTemplate


class BarChartFragment : Fragment() {
    lateinit var barChart: BarChart
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_bar_chart, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        barChart = view.findViewById(R.id.bar_Chart)

        val list: ArrayList<BarEntry> = ArrayList()

        list.add(BarEntry(1f, 100f))
        list.add(BarEntry(2f, 101f))
        list.add(BarEntry(3f, 102f))
        list.add(BarEntry(4f, 103f))
        list.add(BarEntry(5f, 104f))
        list.add(BarEntry(6f, 99f))
        list.add(BarEntry(7f, 120f))
        list.add(BarEntry(8f, 58f))
        list.add(BarEntry(9f, 76f))
        list.add(BarEntry(10f, 102.8f))
        list.add(BarEntry(11f, 110f))
        list.add(BarEntry(12f, 100f))

        val barDataSet = BarDataSet(list, "monthly values")

        barDataSet.setColors(ColorTemplate.COLORFUL_COLORS, 255)
//        barDataSet.setColors(
//            Color.rgb(192, 192, 192), Color.rgb(169, 169, 169),
//            Color.rgb(128, 128, 128), Color.rgb(105, 105, 105),Color.rgb(79, 79, 79),
//        )
        barDataSet.valueTextColor = Color.BLACK
        val barData = BarData(barDataSet)
        barChart.setFitBars(true)
        barChart.data = barData
        barChart.description.text = "annual sales"
        barChart.animateY(2000)
    }
}