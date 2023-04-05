package com.clausfonseca.rosacha.view.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.clausfonseca.rosacha.databinding.ItemRecyclerSalesListBinding
import com.clausfonseca.rosacha.model.Sales
import com.clausfonseca.rosacha.view.dashboard.sales.ListSalesFragment

class SalesAdapter(
    private val context: Context,
    private val salesList: MutableList<Sales>,
    var lastItemRecyclerView: ListSalesFragment,
) : RecyclerView.Adapter<SalesAdapter.MyViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        return MyViewHolder(
            ItemRecyclerSalesListBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val sales = salesList[position]
        holder.binding.txtId.text = sales.id
        holder.binding.txtDate.text = sales.salesDate?.substring(0, 10) ?: "0000/00/00"
        holder.binding.txtClient.text = sales.client
        holder.binding.txtTotalSales.text = sales.totalPrice.toString()

        if (position == itemCount - 1) {
            lastItemRecyclerView.lastItemRecyclerView(true)
        }
    }

    override fun getItemCount() = salesList.size

    inner class MyViewHolder(val binding: ItemRecyclerSalesListBinding) :
        RecyclerView.ViewHolder(binding.root)

    interface LastItemRecyclerView {
        fun lastItemRecyclerView(isShow: Boolean)
        abstract fun setSingleChoiceItems(filter: Array<String>, selecteDItemIndex: Int, any: Any): Any
    }
}