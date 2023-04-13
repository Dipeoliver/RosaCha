package com.clausfonseca.rosacha.view.dashboard.client

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.net.Uri
import android.os.Bundle
import android.text.Html
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.clausfonseca.rosacha.R
import com.clausfonseca.rosacha.databinding.FragmentClientListBinding
import com.clausfonseca.rosacha.model.Client
import com.clausfonseca.rosacha.utils.DialogProgress
import com.clausfonseca.rosacha.utils.Swipe.SwipeGesture
import com.clausfonseca.rosacha.utils.Util
import com.clausfonseca.rosacha.view.adapter.ClientAdapter
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage

class ListClientFragment : Fragment(), ClientAdapter.LastItemRecyclerView,
    ClientAdapter.ClickClient {

    private lateinit var binding: FragmentClientListBinding
    private lateinit var clientAdapter: ClientAdapter
    private val clientlist = mutableListOf<Client>()
    private val viewModel: AddClientViewModel by viewModels()

    private lateinit var firebaseStorage: FirebaseStorage
    private var dbClients: String = ""
    var db: FirebaseFirestore? = null
    var nextquery: Query? = null
    var isFilterOn = false

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
        dbClients = getString(R.string.db_client)
        firebaseStorage = Firebase.storage
        initListeners()
        initAdapter()
        getClients()
        searchClient()
        onBackPressed()
    }

    override fun lastItemRecyclerView(isShow: Boolean) {
        if (isFilterOn)
        else getMoreClients()
    }

    private fun onBackPressed() {
        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    val uri = Uri.parse("android-app://com.clausfonseca.rosacha/home_fragment")
                    findNavController().navigate(uri)
                }
            })
    }

    private fun initListeners() {
        binding.fabAddClient.setOnClickListener {
            val uri = Uri.parse("android-app://com.clausfonseca.rosacha/fragment_add_client")
            findNavController().navigate(uri)
        }
    }

    private fun selectedClient(client: Client) {
        findNavController().navigate(
            ClientFragmentDirections.actionClientFragmentToEditClientFragment(
                client
            )
        )
//        findNavController().navigate(ClientFragmentDirections.actionFragmentClientToFragmentEdit(client))

//        val args = Bundle()
//        args.putParcelable("client", client)
//        findNavController().navigate(R.id.action_fragment_client_to_fragment_edit, args)
    }

    private fun initAdapter() {
        binding.rvClient.layoutManager = LinearLayoutManager(requireContext())
        binding.rvClient.setHasFixedSize(true)
        clientAdapter = ClientAdapter(requireContext(), clientlist, this, this) { client, select ->
            optionSelect(client, select)
        }
        binding.rvClient.adapter = clientAdapter
        swipeToGesture(binding.rvClient)
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
        builder.setTitle(Html.fromHtml("<font color='#F92391'>" + getString(R.string.attention) + "</font>"));

        //set message for alert dialog
//        builder.setMessage(Html.fromHtml("<font color='#FB2391'>Realmente deseja excluir o cliente: ${client.name}</font>"));
        builder.setMessage(getString(R.string.want_delete_client) + " " + client.name)
        builder.setIcon(R.drawable.baseline_warning_24)

        //performing positive action
        builder.setPositiveButton(getString(R.string.yes)) { _, _ ->
            deleteClient(client)
        }
//        //performing cancel action
//        builder.setNeutralButton("Cancel"){dialogInterface , which ->
//            Toast.makeText(applicationContext,"clicked cancel\n operation cancel",Toast.LENGTH_LONG).show()
//        }
        //performing negative action
        builder.setNegativeButton(getString(R.string.no)) { dialogInterface, _ ->
            dialogInterface.dismiss()
        }
        // Create the AlertDialog
        val alertDialog: AlertDialog = builder.create()
        // Set other dialog properties
        alertDialog.setCancelable(false)
        alertDialog.show()
    }

    private fun swipeToGesture(itemRv: RecyclerView?) {
        val swipeGesture = object : SwipeGesture(requireContext()) {
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                var actionBtnTapped = false
                try {
                    when (direction) {
                        ItemTouchHelper.LEFT -> {

                            val client = clientlist[position]
                            clientlist.removeAt(position)
                            clientAdapter.notifyItemRemoved(position)

                            deleteClient(client)
                            clientAdapter.notifyDataSetChanged()

                            val snackBar = Snackbar.make(
                                binding.rvClient, getString(R.string.item_deleted_client), 5000
                            ).addCallback(object : BaseTransientBottomBar.BaseCallback<Snackbar>() {
                                override fun onDismissed(transientBottomBar: Snackbar?, event: Int) {
                                    super.onDismissed(transientBottomBar, event)
                                }

                                override fun onShown(transientBottomBar: Snackbar?) {
                                    transientBottomBar?.setAction(getString(R.string.undo_client)) {
//                                        clientlist.add(position, client)
                                        clientlist.clear()
                                        insertClient(client)
                                        getClients()
//                                        clientAdapter.notifyItemInserted(position)
//                                        clientAdapter.notifyDataSetChanged()
                                        actionBtnTapped = true
                                    }
                                    super.onShown(transientBottomBar)
                                }
                            }).apply {

                                animationMode = Snackbar.ANIMATION_MODE_FADE

                            }
                            snackBar.setActionTextColor(
                                ContextCompat.getColor(
                                    requireContext(),
                                    R.color.pink,

                                    )
                            )
                            snackBar.show()

                        }


                        ItemTouchHelper.RIGHT -> {
                            val clientPosition = clientlist[position]
                            selectedClient(clientPosition)
                        }
                    }
                } catch (e: Exception) {
                    Toast.makeText(requireContext(), e.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
        val touchHelper = ItemTouchHelper(swipeGesture)
        touchHelper.attachToRecyclerView(itemRv)

    }

    // Filter  -----------------------------------------------------------
    private fun searchClient() {
        binding.svClient.inputType = InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS + InputType.TYPE_CLASS_TEXT
        binding.svClient.setOnQueryTextListener(object : SearchView.OnQueryTextListener,
            android.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
//                Log.d("Diego-onQueryTextSubmit", query.toString())
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                isFilterOn = true
                filterSearchClient(newText.toString())
//                Log.d("Diego-onQueryTextChange", newText.toString())
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

    @SuppressLint("NotifyDataSetChanged")
    private fun filterSearchClient(newText: String) {
        db!!.collection(dbClients).orderBy("name").startAt(newText)
            .endAt(newText + "\uf8ff")?.limit(5)?.get()?.addOnSuccessListener { results ->
                if (results.size() > 0) {
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

    // Firestore DataBase --------------------------------------------------

    private fun insertClient(client: Client) {
        viewModel.db.collection(dbClients).document(client.phone.toString())
            .set(client).addOnCompleteListener {
//                Util.exibirToast(requireContext(), getString(R.string.add_success_client))
            }.addOnFailureListener {
                Util.exibirToast(requireContext(), getString(R.string.error_save_client))
            }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun getClients() {
        val dialogProgress = DialogProgress()
        dialogProgress.show(childFragmentManager, "0")

        db!!.collection(dbClients).orderBy("name").limit(10).get().addOnSuccessListener { results ->
            dialogProgress.dismiss()

            if (results.size() > 0) {
                clientlist.clear()

                // result é uma lista
                for (result in results) {
                    val key = result.id // pegar o nome  da pasta do documento
                    val client = result.toObject(Client::class.java)
                    clientlist.add(client)
                }
                // pegar ultimo item da query
                val lastresult = results.documents[results.size() - 1]
                nextquery =
                    db!!.collection(dbClients).orderBy("name").startAfter(lastresult).limit(10)

                clientAdapter.notifyDataSetChanged()

            } else {
                dialogProgress.dismiss()
                Util.exibirToast(requireContext(), getString(R.string.no_list_client))
            }
        }.addOnFailureListener { error ->
            dialogProgress.dismiss()

            Util.exibirToast(requireContext(), getString(R.string.error_show_client) + ":" + error.message.toString())
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun getMoreClients() {
        nextquery?.get()?.addOnSuccessListener { results ->

            // o if e para verificar se chegou o fim da lista
            if (results.size() > 0) {
                // pegar ultimo item da query
                val lastresult = results.documents[results.size() - 1]

                nextquery =
                    db!!.collection(dbClients).orderBy("name").startAfter(lastresult).limit(10)

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

    private fun deleteClient(client: Client) {
        val reference = db!!.collection(dbClients)
        client.phone?.let {
            reference.document(it).delete().addOnCompleteListener() { task ->
                if (task.isSuccessful) {
//                    client.phone?.let { it1 -> removeImage(it1) }
//                    Util.exibirToast(requireContext(), getString(R.string.information_delete_client))
                    getClients()
                } else {
                    Util.exibirToast(
                        requireContext(),
                        getString(R.string.error_delete_client) + ":" + task.exception.toString()
                    )
                }
            }
        }
    }

    fun removeImage(id: String) {
        val reference = firebaseStorage.reference.child(dbClients).child("${id}.jpg")
        reference.delete().addOnSuccessListener { task ->
        }.addOnFailureListener { error ->
            Util.exibirToast(
                requireContext(),
                getString(R.string.error_delete_image) + ":" + error.message.toString()
            )
        }
    }

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



