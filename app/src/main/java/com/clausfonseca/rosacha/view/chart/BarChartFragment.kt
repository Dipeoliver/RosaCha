package com.clausfonseca.rosacha.view.chart

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.clausfonseca.rosacha.R
import com.clausfonseca.rosacha.databinding.FragmentBarChartBinding
import com.clausfonseca.rosacha.databinding.FragmentSalesAddBinding
import com.clausfonseca.rosacha.utils.DialogProgress
import com.github.mikephil.charting.charts.BarChart
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
    private val calendar = Calendar.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentBarChartBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        db = FirebaseFirestore.getInstance()
        dbSales = getString(R.string.db_sales)
        barChart = view.findViewById(R.id.bar_Chart)
        getSales()


    }

    private fun getGraph() {
        val list: ArrayList<BarEntry> = ArrayList()
        list.add(BarEntry(1f, monthResults[0].toFloat()))
        list.add(BarEntry(2f, monthResults[1].toFloat()))
        list.add(BarEntry(3f, monthResults[2].toFloat()))
        list.add(BarEntry(4f, monthResults[3].toFloat()))
        list.add(BarEntry(5f, monthResults[4].toFloat()))
        list.add(BarEntry(6f, monthResults[5].toFloat()))
        list.add(BarEntry(7f, monthResults[6].toFloat()))
        list.add(BarEntry(8f, monthResults[7].toFloat()))
        list.add(BarEntry(9f, monthResults[8].toFloat()))
        list.add(BarEntry(10f, monthResults[9].toFloat()))
        list.add(BarEntry(11f, monthResults[10].toFloat()))
        list.add(BarEntry(12f, monthResults[11].toFloat()))

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
        barChart.description.text = calendar.get(Calendar.YEAR).toString()
        barChart.animateY(2000)
    }

    private fun getSales() {
        val dialogProgress = DialogProgress()
        dialogProgress.show(childFragmentManager, "0")
        calendar.get(Calendar.YEAR)

        val query = db!!.collection(dbSales).whereEqualTo("year", calendar.get(Calendar.YEAR))

        query.addSnapshotListener { results, error ->
            dialogProgress.dismiss()
            if (results != null) {

                for (result in results) {
                    val month: Int = result.getLong("month")?.toInt() ?: -1
//                    Log.d("result", result["month"].toString())
                    if (month != -1) {
                        monthResults[month - 1] += result.getDouble("totalPrice") ?: 0.0
                    }
                }
                getGraph()
                binding.txtMonthSales.text = String.format("%.2f", (monthResults[calendar.get(Calendar.MONTH)]))
            }
        }
    }
}