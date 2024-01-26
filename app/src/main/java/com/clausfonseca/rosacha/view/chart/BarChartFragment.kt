package com.clausfonseca.rosacha.view.chart

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.clausfonseca.rosacha.R
import com.clausfonseca.rosacha.databinding.FragmentBarChartBinding
import com.clausfonseca.rosacha.utils.DialogProgress
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.XAxis.XAxisPosition
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.utils.ColorTemplate
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Calendar


class BarChartFragment : Fragment() {
    private lateinit var binding: FragmentBarChartBinding

    lateinit var barChart: BarChart
    private val monthResults: MutableList<Double> = MutableList(12) { 0.0 }
    private var dbSales: String = ""
    var db: FirebaseFirestore? = null
    private var dbMonthSales: String = ""

    private val calendar = Calendar.getInstance()
    var mes = ""
    var monthValue: Double = 0.0
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentBarChartBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        dbMonthSales = getString(R.string.db_month_sales)
        db = FirebaseFirestore.getInstance()
        dbSales = getString(R.string.db_sales)
        barChart = view.findViewById(R.id.bar_Chart)
        graphConfig()
        getSales()
    }

    private fun graphConfig() {
        val xAxis: XAxis = barChart.xAxis
        xAxis.position = XAxisPosition.BOTTOM
        xAxis.axisMinimum = 0f
        xAxis.axisMaximum = 11f
        xAxis.granularity = 1f
        xAxis.labelRotationAngle = 270.0f
        xAxis.setLabelCount(12, true)
        xAxis.valueFormatter = MonthValueFormatter()// xVal is a string array
        xAxis.setDrawGridLines(false)

        val rightAxis: YAxis = barChart.axisRight
        rightAxis.setDrawGridLines(false)
        rightAxis.axisMinimum = 0f // this replaces setStartAtZero(true)

        val leftAxis: YAxis = barChart.axisLeft
        leftAxis.setDrawGridLines(false)
        leftAxis.axisMinimum = 0f // this replaces setStartAtZero(true)
    }

    private fun getSales() {
//        when (calendar.get(Calendar.MONTH) + 1) {
//            1 -> mes = "jan"
//            2 -> mes = "feb"
//            3 -> mes = "mar"
//            4 -> mes = "apr"
//            5 -> mes = "may"
//            6 -> mes = "jun"
//            7 -> mes = "jul"
//            8 -> mes = "aug"
//            9 -> mes = "sep"
//            10 -> mes = "oct"
//            11 -> mes = "nov"
//            12 -> mes = "dec"
//        }
//        ouvinte7()

        val dialogProgress = DialogProgress()
        dialogProgress.show(childFragmentManager, "0")

        val docRef = db?.collection(dbMonthSales)?.document(calendar.get(Calendar.YEAR).toString())
        docRef?.get()
            ?.addOnSuccessListener() { results ->
                dialogProgress.dismiss()

                results.data?.forEach {
                    if (it.value is Double || it.value is Long) {
                        monthValue = if (it.value is Long) {
                            (it.value as Long).toDouble()
                        } else
                            it.value as Double
//                        Log.d("teste de lista", monthValue.toString())

//                         it.value as Double
                    }
                    when (it.key.toString()) {
                        "jan" -> monthResults[0] = monthValue
                        "feb" -> monthResults[1] = monthValue
                        "mar" -> monthResults[2] = monthValue
                        "apr" -> monthResults[3] = monthValue
                        "may" -> monthResults[4] = monthValue
                        "jun" -> monthResults[5] = monthValue
                        "jul" -> monthResults[6] = monthValue
                        "aug" -> monthResults[7] = monthValue
                        "sep" -> monthResults[8] = monthValue
                        "oct" -> monthResults[9] = monthValue
                        "nov" -> monthResults[10] = monthValue
                        "dec" -> monthResults[11] = monthValue
                        else -> ""
                    }
                }
                getGraph()
                binding.txtMonthSales.text = String.format("%.2f", (monthResults[calendar.get(Calendar.MONTH)]))
            }
    }

    private fun getGraph() {
        val list: ArrayList<BarEntry> = ArrayList()
        list.add(BarEntry(0f, monthResults[0].toFloat()))
        list.add(BarEntry(1f, monthResults[1].toFloat()))
        list.add(BarEntry(2f, monthResults[2].toFloat()))
        list.add(BarEntry(3f, monthResults[3].toFloat()))
        list.add(BarEntry(4f, monthResults[4].toFloat()))
        list.add(BarEntry(5f, monthResults[5].toFloat()))
        list.add(BarEntry(6f, monthResults[6].toFloat()))
        list.add(BarEntry(7f, monthResults[7].toFloat()))
        list.add(BarEntry(8f, monthResults[8].toFloat()))
        list.add(BarEntry(9f, monthResults[9].toFloat()))
        list.add(BarEntry(10f, monthResults[10].toFloat()))
        list.add(BarEntry(11f, monthResults[11].toFloat()))

        val barDataSet = BarDataSet(list, calendar.get(Calendar.YEAR).toString())

        barDataSet.valueTextSize = 14f
        barDataSet.setColors(ColorTemplate.COLORFUL_COLORS, 255)
//        barDataSet.setColors(
//            Color.rgb(192, 192, 192), Color.rgb(169, 169, 169),
//            Color.rgb(128, 128, 128), Color.rgb(105, 105, 105),Color.rgb(79, 79, 79),
//        )
        barDataSet.valueTextColor = Color.BLACK
        val barData = BarData(barDataSet)
        barChart.setFitBars(false)
        barChart.data = barData
        barChart.description.text = ""
        barChart.animateY(2000)
        barChart.legend.isEnabled = false

//        https://github.com/PhilJay/MPAndroidChart/blob/master/MPChartExample/src/main/java/com/xxmassdeveloper/mpchartexample/CombinedChartActivity.java
    }
}