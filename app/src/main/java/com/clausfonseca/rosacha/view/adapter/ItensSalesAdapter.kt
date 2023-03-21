package com.clausfonseca.rosacha.view.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.clausfonseca.rosacha.databinding.ItemSalesAdapterBinding
import com.clausfonseca.rosacha.model.ItensSales

class ItensSalesAdapter(
    private val context: Context,
    private val itensSales: MutableList<ItensSales>,
    val itensSalesSelected: (MutableList<ItensSales>) -> Unit
) : RecyclerView.Adapter<ItensSalesAdapter.MyViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        return MyViewHolder(
            ItemSalesAdapterBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun getItemCount() = itensSales.size

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {

        val item = itensSales[position]
        holder.binding.txtBarcode.text = item.barcode
        holder.binding.txtDescription.text = item.description
        holder.binding.txtSalesPrice.text = String.format("%.2f", item.salesPrice)

        holder.binding.btnDeleteIten.setOnClickListener {

            itensSales.remove(item)
            notifyItemRemoved(holder.adapterPosition)
            itensSalesSelected(itensSales)
        }
    }


    inner class MyViewHolder(val binding: ItemSalesAdapterBinding) :
        RecyclerView.ViewHolder(binding.root)
}