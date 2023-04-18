package com.clausfonseca.rosacha.view.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.clausfonseca.rosacha.R
import com.clausfonseca.rosacha.databinding.ItemRecyclerProductListBinding
import com.clausfonseca.rosacha.model.Product
import com.clausfonseca.rosacha.view.dashboard.product.ListProductFragment

class ProductAdapter(
    private val context: Context,
    private val productList: List<Product>,
    var clickProduto: ListProductFragment,
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
            ItemRecyclerProductListBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    inner class MyViewHolder(val binding: ItemRecyclerProductListBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun getItemCount() = productList.size

    //    exibir as informações de cada tarefa
    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val product = productList[position]
        holder.binding.txtBarcode.text = product.barcode
        holder.binding.txtDescription.text = product.description
        holder.binding.txtSalesPrice.text = String.format("%.2f", product.salesPrice)

        if (product.urlImagem == "") {
            Glide.with(context).load(R.drawable.no_image).into(holder.binding.imvProduto)
        } else Glide.with(context).load(product.urlImagem).into(holder.binding.imvProduto)

//        // click no card view
//        holder.binding.cardViewProduct1.setOnClickListener {
//            clickProduto.clickProduto((product))
//        }

        // quando chegar na ultima posição que tem na tela chama a função abaixo
        if (position == itemCount - 1) {
            lastItemRecyclerView.lastItemRecyclerView(true)
        }
    }
//
//    interface ClickProduto {
//        fun clickProduto(product: Product)
//    }

    interface LastItemRecyclerView {
        fun lastItemRecyclerView(isShow: Boolean)
    }
}