package com.clausfonseca.rosacha.view.dashboard.client

import android.app.AlertDialog
import android.net.Uri
import android.os.Bundle
import android.text.Html
import android.text.InputType
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.br.jafapps.bdfirestore.util.DialogProgress
import com.br.jafapps.bdfirestore.util.Util
import com.clausfonseca.rosacha.R
import com.clausfonseca.rosacha.databinding.FragmentClientListBinding
import com.clausfonseca.rosacha.model.Client
import com.clausfonseca.rosacha.view.adapter.ClientAdapter
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class ListClientFragment : Fragment(), ClientAdapter.LastItemRecyclerView {

    private lateinit var binding: FragmentClientListBinding
    private lateinit var clientAdapter: ClientAdapter
    private val clientlist = mutableListOf<Client>()
    var db: FirebaseFirestore? = null
    var nextquery: Query? = null
    var isFilterOn = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
//        val menuHost: MenuHost = requireActivity()
//        menuHost.addMenuProvider(this, viewLifecycleOwner, Lifecycle.State.RESUMED)

        binding = FragmentClientListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        db = FirebaseFirestore.getInstance()
        initListeners()
        initAdapter()
        getClients()
        searchClient()
    }

    override fun onResume() {
        super.onResume()
        getClients()
    }

    override fun lastItemRecyclerView(isShow: Boolean) {
        if (isFilterOn)
        else getMoreClients()
    }

    private fun initListeners() {
        binding.fabAddClient.setOnClickListener {
            val uri = Uri.parse("android-app://com.clausfonseca.rosacha/addClient_Fragment")
            findNavController().navigate(uri)
//            binding.root.removeAllViewsInLayout()
        }
    }

    // To control the click into searchView
    private fun searchClient() {
        binding.svClient.inputType = InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS
        binding.svClient.setOnQueryTextListener(object : SearchView.OnQueryTextListener,
            android.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                Log.d("Diego-onQueryTextSubmit", query.toString())
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                isFilterOn = true
                filterSearchClient(newText.toString())
                Log.d("Diego-onQueryTextChange", newText.toString())
                return true
            }
        })
        binding.svClient.setOnCloseListener(object : SearchView.OnCloseListener,
            android.widget.SearchView.OnCloseListener {
            override fun onClose(): Boolean {
                binding.svClient.onActionViewCollapsed()
                clientlist.clear()
                clientAdapter.notifyDataSetChanged()
                getClients()
                isFilterOn = false
                return true
            }
        })
    }


    // Firestore DataBase -----------------------------------------------
    private fun filterSearchClient(newText: String) {
        db!!.collection("Clients").orderBy("name").startAt(newText)
            .endAt(newText + "\uf8ff")?.limit(5)?.get()?.addOnSuccessListener { results ->
                if (results != null) {
                    clientlist.clear()
                    for (result in results) {
                        val client = result.toObject(Client::class.java)
                        clientlist.add(client)
                    }
                    clientAdapter.notifyDataSetChanged()
                }
            }?.addOnFailureListener { error ->
                Toast.makeText(
                    requireContext(),
                    "Error ${error.message.toString()}",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

    private fun getClients() {
        val dialogProgress = DialogProgress()
        dialogProgress.show(childFragmentManager, "0")

        db!!.collection("Clients").orderBy("name").limit(10).get().addOnSuccessListener { results ->
            dialogProgress.dismiss()

            if (results != null) {
                clientlist.clear()

                // result é uma lista
                for (result in results) {
                    val key = result.id // pegar o nome  da pasta do documento
                    val client = result.toObject(Client::class.java)
                    clientlist.add(client)
                }
                // pegar ultimo item da query
                val lastresult = results.documents[results.size() - 1]
                nextquery = db!!.collection("Clients").orderBy("name").startAfter(lastresult).limit(10)

                clientAdapter.notifyDataSetChanged()
                //initAdapter()
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

    private fun getMoreClients() {
        nextquery?.get()?.addOnSuccessListener { results ->

            // o if e para verificar se chegou o fim da lista
            if (results.size() > 0) {
                // pegar ultimo item da query
                val lastresult = results.documents[results.size() - 1]

                nextquery = db!!.collection("Clients").orderBy("name").startAfter(lastresult).limit(10)

                for (result in results) {
                    val client = result.toObject(Client::class.java)
                    clientlist.add(client)
                }
                // notificar que teve atualizalçao
                clientAdapter.notifyDataSetChanged()
            } else {
//                Util.exibirToast(requireContext(), "Não ha mais itens para serem exibidos")
            }
        }?.addOnFailureListener() { error ->
            Util.exibirToast(requireContext(), error.message.toString())
        }
    }
    // END Firestore DataBase -------------------------------------------

    private fun initAdapter() {
        binding.rvClient.layoutManager = LinearLayoutManager(requireContext())
        binding.rvClient.setHasFixedSize(true)
        clientAdapter = ClientAdapter(requireContext(), clientlist, this) { client, select ->
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

    // Delete Client ---------------------------------------------------
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
    // END Delete Client -----------------------------------------------


// Menu-----------------------------------------------------------------
//    override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
//        menuInflater.inflate(R.menu.search, menu)
//    }
//
//    override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
//        return when (menuItem.itemId) {
//            R.id.action_search -> {
//                Toast.makeText(requireContext(), "ok", Toast.LENGTH_SHORT).show()
//                true
//            }
//            else -> true
//        }
//    }
// Menu-----------------------------------------------------------------
}



