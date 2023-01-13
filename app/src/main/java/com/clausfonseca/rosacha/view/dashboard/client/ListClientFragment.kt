package com.clausfonseca.rosacha.view.dashboard.client

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.br.jafapps.bdfirestore.util.DialogProgress
import com.clausfonseca.rosacha.data.firebase.FirebaseHelper
import com.clausfonseca.rosacha.databinding.FragmentClientBinding
import com.clausfonseca.rosacha.databinding.FragmentListClientBinding
import com.clausfonseca.rosacha.model.Client
import com.clausfonseca.rosacha.view.adapter.ClientAdapter
import com.google.firebase.firestore.FirebaseFirestore

class ListClientFragment : Fragment() {

    private lateinit var binding: FragmentListClientBinding
    private lateinit var clientAdapter: ClientAdapter
    private val clientlist = mutableListOf<Client>()

    var db: FirebaseFirestore? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentListClientBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        db = FirebaseFirestore.getInstance()
        getClients()
        initClick()
    }

    override fun onResume() {
        super.onResume()
        getClients()
    }

    private fun initClick() {
        binding.fabAddClient.setOnClickListener {
            val uri = Uri.parse("android-app://com.clausfonseca.rosacha/addClient_Fragment")
            findNavController().navigate(uri)
//            binding.root.removeAllViewsInLayout()
        }
    }

    // Firestore DataBase
    private fun getClients() {
        val dialogProgress = DialogProgress()
        dialogProgress.show(childFragmentManager, "0")

        db!!.collection("Clients").get().addOnSuccessListener { results ->

            if (results != null) {
                clientlist.clear()

                // result é uma lista
                for (result in results) {
                    val key = result.id // pegar o nome  da pasta do documento
                    val client = result.toObject(Client::class.java)

                    clientlist.add(client)
                }
                initAdapter()
                dialogProgress.dismiss()

            } else {
                dialogProgress.dismiss()
                Toast.makeText(
                    requireContext(),
                    "Erro ao exibir o documento, ele não existe",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }.addOnFailureListener { error ->
            dialogProgress.dismiss()
            Toast.makeText(
                requireContext(),
                "Error ${error.message.toString()}",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun initAdapter() {
        binding.rvClient.layoutManager = LinearLayoutManager(requireContext())
        binding.rvClient.setHasFixedSize(true)
        clientAdapter = ClientAdapter(requireContext(), clientlist) { client, select ->
            optionSelect(client, select)
        }
        binding.rvClient.adapter = clientAdapter
    }

    private fun optionSelect(client: Client, select: Int) {
        when (select) {
            ClientAdapter.SELECT_REMOVE -> {
                deleteClient(client)
            }
            ClientAdapter.SELECT_EDIT -> {
            }
        }
    }

    private fun deleteClient(client: Client) {
        FirebaseHelper
            .getDatabase()
            .child("Client")
            .child("Clients")
            .child(client.id)
            .removeValue()
    }
}

