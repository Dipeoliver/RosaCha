package com.clausfonseca.rosacha.view.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.clausfonseca.rosacha.databinding.ItemProductAdapterBinding
import com.clausfonseca.rosacha.view.model.Product
import com.google.firebase.database.core.Context

class ProductAdapter(
    private val context: android.content.Context,
    private val productList: List<Product>,
    val taskSelected: (Product, Int) -> Unit
) : RecyclerView.Adapter<ProductAdapter.MyViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        return MyViewHolder(
            ItemProductAdapterBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    inner class MyViewHolder(val binding: ItemProductAdapterBinding) :
        RecyclerView.ViewHolder(binding.root)

    //    mostrar o tamanho da lista
    override fun getItemCount() = productList.size

    //    exibir as informações de cada tarefa
    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val product = productList[position]
        holder.binding.txtBarcode.text = product.barcode
        holder.binding.txtBrand.text = product.brand
        holder.binding.txtProv.text = product.provider
        holder.binding.txtDescription.text = product.description
        holder.binding.txtOwner.text = product.owner.toString()
        holder.binding.txtCostPrice.text = product.cost_price.toString()
        holder.binding.txtSalesPrice.text = product.sales_price.toString()

//        holder.binding.btnDelete.setOnClickListener {
//            taskSelected(task, SELECT_REMOVE)
//        }
//        holder.binding.btnEdit.setOnClickListener {
//            taskSelected(task, SELECT_EDIT)
//        }
//        holder.binding.btnEdit.setOnClickListener {
//            taskSelected(task, SELECT_EDIT)
//        }
    }
}