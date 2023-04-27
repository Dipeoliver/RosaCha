package com.clausfonseca.rosacha.view.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.clausfonseca.rosacha.databinding.ItemRecyclerSalesAddBinding
import com.clausfonseca.rosacha.databinding.ItemRecyclerSalesDetailBinding
import com.clausfonseca.rosacha.model.ItensSales

class ItemsSalesDetailAdapter(
    private val context: Context,
    private var itemsSales: MutableList<ItensSales>,
) : RecyclerView.Adapter<ItemsSalesDetailAdapter.MyViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        return MyViewHolder(
            ItemRecyclerSalesDetailBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ItemsSalesDetailAdapter.MyViewHolder, position: Int) {
        val item = itemsSales[position]

        holder.binding.txtBarcodeDetail.text = item.barcode
        holder.binding.txtDescriptionDetail.text = item.description
        holder.binding.txtSalesPriceDetail.text = String.format("%.2f", item.salesPrice)
    }

    override fun getItemCount()= itemsSales.size

    fun updateList(list: MutableList<ItensSales>) {
        itemsSales = list
        notifyDataSetChanged()
    }

    inner class MyViewHolder(val binding: ItemRecyclerSalesDetailBinding) :
        RecyclerView.ViewHolder(binding.root)
}

