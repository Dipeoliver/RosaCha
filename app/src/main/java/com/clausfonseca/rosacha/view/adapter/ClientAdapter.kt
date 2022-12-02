package com.clausfonseca.rosacha.view.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.clausfonseca.rosacha.databinding.ClientAdapterBinding
import com.clausfonseca.rosacha.view.model.Client

class ClientAdapter(
    private val context: android.content.Context,
    private val clientList: List<Client>,
    val clientSelected: (Client, Int) -> Unit
) : RecyclerView.Adapter<ClientAdapter.MyViewHolder>() {

    companion object {

        val SELECT_REMOVE: Int = 1
        val SELECT_EDIT: Int = 2
        val SELECT_DETAILS: Int = 3
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ClientAdapter.MyViewHolder {
        return MyViewHolder(
            ClientAdapterBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    inner class MyViewHolder(val binding: ClientAdapterBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun getItemCount() = clientList.size

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val client = clientList[position]
        holder.binding.txtName.text = client.name
        holder.binding.txtPhone.text = client.phone
        holder.binding.txtEmail.text = client.email
        holder.binding.txtBirthday.text = client.birthday

        holder.binding.btnClientDelete.setOnClickListener {
            clientSelected(client, SELECT_REMOVE)
        }

        holder.binding.btnClientUpdate.setOnClickListener {
            clientSelected(client, SELECT_EDIT)
        }
    }
}