package com.clausfonseca.rosacha.view.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.clausfonseca.rosacha.R
import com.clausfonseca.rosacha.databinding.ItemRecyclerClientListBinding
import com.clausfonseca.rosacha.model.ClientModel

class ClientAdapter(
    private val context: android.content.Context,
    private val clientModelList: List<ClientModel>,
    var clickClient: ClickClient,
    var lastItemRecyclerView: LastItemRecyclerView,
    val clientSelected: (ClientModel, Int) -> Unit
) : RecyclerView.Adapter<ClientAdapter.MyViewHolder>() {

    companion object {

        val SELECT_REMOVE: Int = 1
        val SELECT_EDIT: Int = 2
        val SELECT_DETAILS: Int = 3
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ClientAdapter.MyViewHolder {
        return MyViewHolder(
            ItemRecyclerClientListBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    inner class MyViewHolder(val binding: ItemRecyclerClientListBinding) :
        RecyclerView.ViewHolder(binding.root)


    override fun getItemCount() = clientModelList.size

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val client = clientModelList[position]
        holder.binding.txtName.text = client.name
        holder.binding.txtPhone.text = client.phone
        holder.binding.txtEmail.text = client.email


        if (client.urlImagem == "") {
            Glide.with(context).load(R.drawable.no_image).into(holder.binding.imvClient)
        } else Glide.with(context).load(client.urlImagem).into(holder.binding.imvClient)

//        holder.binding.btnClientDelete.setOnClickListener {
//            clientSelected(client, SELECT_REMOVE)
//        }

        holder.binding.cardViewClient1.setOnClickListener {
            clickClient.clickClient(client)
        }

        // quando chegar na ultima posição que tem na tela chama a função abaixo
        if (position == itemCount - 1) {
            lastItemRecyclerView.lastItemRecyclerView(true)
        }
    }

    interface LastItemRecyclerView {
        fun lastItemRecyclerView(isShow: Boolean)
    }

    interface ClickClient {
        fun clickClient(clientModel: ClientModel) {
        }
    }
}