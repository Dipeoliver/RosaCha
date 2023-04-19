package com.clausfonseca.rosacha.utils.pdf

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.clausfonseca.rosacha.R
import com.clausfonseca.rosacha.model.ItensSales
import org.json.JSONArray

class MarksRecyclerAdapter(private val subjectItemList: List<ItensSales>) :
    RecyclerView.Adapter<MarksRecyclerAdapter.MarksViewHolder>() {

    class MarksViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val itemBarcode: TextView = view.findViewById(R.id.txt_barcode_recy)
        val itemValue: TextView = view.findViewById(R.id.txt_total_value_recy)
        val itemDescription: TextView = view.findViewById(R.id.txt_description_recy)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MarksViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_recycler_pdf, parent, false)
        return MarksViewHolder(view)
    }

    override fun onBindViewHolder(holder: MarksViewHolder, position: Int) {
        // transforma objeto em json
        val jsonArray = JSONArray(subjectItemList)

        // pegando valores dentro json
        holder.itemBarcode.text = jsonArray.getJSONObject(position).getString("barcode").toString()
        holder.itemDescription.text = jsonArray.getJSONObject(position).getString("description").toString()
        holder.itemValue.text = jsonArray.getJSONObject(position).getDouble("salesPrice").toString()
    }

    override fun getItemCount(): Int {
        return subjectItemList.size
    }
}