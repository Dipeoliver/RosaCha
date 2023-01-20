package com.clausfonseca.rosacha.view.dashboard.client

import android.app.AlertDialog
import android.net.Uri
import android.os.Bundle
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.br.jafapps.bdfirestore.util.DialogProgress
import com.br.jafapps.bdfirestore.util.Util
import com.clausfonseca.rosacha.R
import com.clausfonseca.rosacha.data.firebase.FirebaseHelper
import com.clausfonseca.rosacha.databinding.FragmentClientBinding
import com.clausfonseca.rosacha.databinding.FragmentClientListBinding
import com.clausfonseca.rosacha.model.Client
import com.clausfonseca.rosacha.view.adapter.ClientAdapter
import com.google.firebase.firestore.FirebaseFirestore

class ListClientFragment : Fragment() {

    private lateinit var binding: FragmentClientListBinding
    private lateinit var clientAdapter: ClientAdapter
    private val clientlist = mutableListOf<Client>()

    var db: FirebaseFirestore? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentClientListBinding.inflate(inflater, container, false)
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
                configDialog(client)
            }
            ClientAdapter.SELECT_EDIT -> {
            }
        }
    }

    private fun configDialog(client: Client) {

        val builder = AlertDialog.Builder(requireContext())

        //set title for alert dialog
//        builder.setTitle("Atenção")
        builder.setTitle(Html.fromHtml("<font color='#FB2391'>Atenção</font>"));

        //set message for alert dialog
//        builder.setMessage(Html.fromHtml("<font color='#FB2391'>Realmente deseja excluir o cliente: ${client.name}</font>"));
        builder.setMessage("Realmente deseja excluir: ${client.name}")
        builder.setIcon(R.drawable.ic_warning)

        //performing positive action
        builder.setPositiveButton("Yes") { dialogInterface, which ->
            deleteClient(client)
        }
//        //performing cancel action
//        builder.setNeutralButton("Cancel"){dialogInterface , which ->
//            Toast.makeText(applicationContext,"clicked cancel\n operation cancel",Toast.LENGTH_LONG).show()
//        }
        //performing negative action
        builder.setNegativeButton("No") { dialogInterface, which ->
            dialogInterface.dismiss()
        }
        // Create the AlertDialog
        val alertDialog: AlertDialog = builder.create()
        // Set other dialog properties
        alertDialog.setCancelable(false)
        alertDialog.show()
    }

    private fun deleteClient(client: Client) {
        val reference = db!!.collection("Clients")

        reference.document(client.id).delete().addOnCompleteListener() { task ->
            if (task.isSuccessful) {
                Util.exibirToast(requireContext(), "Deletado com Sucesso")
                getClients()
            } else {
                Util.exibirToast(requireContext(), "erro ao deletar no banco ${task.exception.toString()}")
            }
        }
    }
}

