package com.clausfonseca.rosacha.view.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.clausfonseca.rosacha.databinding.ItemProductAdapterBinding
import com.clausfonseca.rosacha.model.Product
import com.clausfonseca.rosacha.view.dashboard.product.ListProductFragment

class ProductAdapter(
    private val context: Context,
    private val productList: List<Product>,
    var lastItemRecyclerView: ListProductFragment,
    val productSelected: (Product, Int) -> Unit
) : RecyclerView.Adapter<ProductAdapter.MyViewHolder>() {

    companion object {

        val SELECT_REMOVE: Int = 1
        val SELECT_EDIT: Int = 2
        val SELECT_DETAILS: Int = 3
    }

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

    override fun getItemCount() = productList.size

    //    exibir as informações de cada tarefa
    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val product = productList[position]
        holder.binding.txtBarcode.text = product.barcode
//        holder.binding.txtBrand.text = product.brand
//        holder.binding.txtProv.text = product.provider
        holder.binding.txtDescription.text = product.description
//        holder.binding.txtOwner.text = product.owner.toString()
        holder.binding.txtCostPrice.text = product.cost_price.toString()
        holder.binding.txtSalesPrice.text = product.sales_price.toString()

        holder.binding.btnProductDelete.setOnClickListener {
            productSelected(product, SELECT_REMOVE)
        }
        holder.binding.btnProductUpdate.setOnClickListener {
            productSelected(product, SELECT_EDIT)
        }
        // quando chegar na ultima posição que tem na tela chama a função abaixo
        if (position == itemCount - 1) {
            lastItemRecyclerView.lastItemRecyclerView(true)
        }
    }

    interface LastItemRecyclerView {
        fun lastItemRecyclerView(isShow: Boolean)
    }
}