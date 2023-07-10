package com.clausfonseca.rosacha.view.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.clausfonseca.rosacha.databinding.ItemRecyclerSalesAddBinding
import com.clausfonseca.rosacha.model.ItensSales

class ItensSalesAdapter(
    private val context: Context,
    private var itemsSales: MutableList<ItensSales>,
    private var quantity: Int,
    val itemsSalesSelected: (MutableList<ItensSales>) -> Unit
) : RecyclerView.Adapter<ItensSalesAdapter.MyViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        return MyViewHolder(
            ItemRecyclerSalesAddBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun getItemCount() = itemsSales.size

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {

        val item = itemsSales[position]
        holder.binding.txtBarcode.text = item.barcode
        holder.binding.txtDescription.text = item.description
        holder.binding.txtSalesPrice.text = String.format("%.2f", item.salesPrice)
        holder.binding.txtQuantityValue.text = "${item.qtySales} X"

        holder.binding.btnDeleteIten.setOnClickListener {
            itemsSales.remove(item)
            notifyItemRemoved(holder.adapterPosition)
            itemsSalesSelected(itemsSales)
        }
    }

    inner class MyViewHolder(val binding: ItemRecyclerSalesAddBinding) :
        RecyclerView.ViewHolder(binding.root)
}