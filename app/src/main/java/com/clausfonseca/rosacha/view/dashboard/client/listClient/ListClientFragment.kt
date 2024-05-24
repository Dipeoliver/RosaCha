package com.clausfonseca.rosacha.view.dashboard.client.listClient

import android.annotation.SuppressLint
import android.net.Uri
import android.os.Bundle
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
import com.clausfonseca.rosacha.model.ClientModel
import com.clausfonseca.rosacha.utils.DialogProgress
import com.clausfonseca.rosacha.utils.Swipe.SwipeGesture
import com.clausfonseca.rosacha.utils.Util
import com.clausfonseca.rosacha.utils.extencionFunctions.getDbClient
import com.clausfonseca.rosacha.view.adapter.ClientAdapter
import com.clausfonseca.rosacha.view.dashboard.client.ClientFragmentDirections
import com.clausfonseca.rosacha.view.common.CommonModelState
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ListClientFragment : Fragment(), ClientAdapter.LastItemRecyclerView,
    ClientAdapter.ClickClient {

    private lateinit var binding: FragmentClientListBinding
    private lateinit var clientAdapter: ClientAdapter
    private val clientlist = mutableListOf<ClientModel>()
    private val viewModel: ListClientViewModel by viewModels()
    private val dialogProgress = DialogProgress()

    private var dbClients: String = ""
    var db: FirebaseFirestore? = null
    var nextquery: Query? = null
    var isFilterOn = false
    var client = ClientModel()

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
        initListeners()
        initAdapter()
        viewModel.getClients(getDbClient(requireContext()), clientlist)
        searchClient()
        onBackPressed()
        configureObservables()
    }

    override fun lastItemRecyclerView(isShow: Boolean) {
        if (isFilterOn)
        else getMoreClients()                 // $$$$$$  colocar viewModel 17/05
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

    private fun selectedClient(clientModel: ClientModel) {
        findNavController().navigate(
            ClientFragmentDirections.actionClientFragmentToEditClientFragment(
                clientModel
            )
        )
    }

    private fun initAdapter() {
        binding.rvClient.layoutManager = LinearLayoutManager(requireContext())
        binding.rvClient.setHasFixedSize(true)
        clientAdapter = ClientAdapter(requireContext(), clientlist, this, this) { client, select ->
        }
        binding.rvClient.adapter = clientAdapter
        swipeToGesture(binding.rvClient)
    }

    private fun swipeToGesture(itemRv: RecyclerView?) {
        val swipeGesture = object : SwipeGesture(requireContext()) {
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                var actionBtnTapped = false
                try {
                    when (direction) {
                        ItemTouchHelper.LEFT -> {

                            client = clientlist[position]
                            clientlist.removeAt(position)
                            clientAdapter.notifyItemRemoved(position)

                            viewModel.removeClient(dbClient = getDbClient(requireContext()), clientModel = client)

                            val snackBar = Snackbar.make(
                                binding.rvClient, getString(R.string.item_deleted_client), 5000
                            ).addCallback(object : BaseTransientBottomBar.BaseCallback<Snackbar>() {
                                override fun onDismissed(transientBottomBar: Snackbar?, event: Int) {
                                    super.onDismissed(transientBottomBar, event)
                                }

                                override fun onShown(transientBottomBar: Snackbar?) {
                                    transientBottomBar?.setAction(getString(R.string.undo_client)) {
                                        clientlist.clear()
                                        viewModel.insertClient(getDbClient(requireContext()), client)
                                        viewModel.getClients(getDbClient(requireContext()), clientlist)
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
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                isFilterOn = true
                viewModel.filterSearchClients(getDbClient(requireContext()), newText ?: "", clientlist)
//                filterSearchClient(newText.toString())    // $$$$$$  colocar viewModel 17/05
                return true
            }
        })
        binding.svClient.setOnCloseListener(object : SearchView.OnCloseListener,
            android.widget.SearchView.OnCloseListener {
            override fun onClose(): Boolean {
                binding.svClient.onActionViewCollapsed()
                viewModel.getClients(getDbClient(requireContext()), clientlist)
                isFilterOn = false
                return true
            }
        })
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun getMoreClients() {
        nextquery?.get()?.addOnSuccessListener { results ->

            // o if e para verificar se chegou o fim da lista
            if (results.size() > 0) {
                // pegar ultimo item da query
                val lastResult = results.documents[results.size() - 1]

                nextquery =
                    db!!.collection(dbClients).orderBy("name").startAfter(lastResult).limit(10)

                for (result in results) {
                    val clientModel = result.toObject(ClientModel::class.java)
                    clientlist.add(clientModel)
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

    private fun configureObservables() {
        viewModel.model.screenState.observe(viewLifecycleOwner) {
            handleState(it)
        }
    }

    private fun handleState(state: CommonModelState.CommonState?) {
        when (state) {
            is CommonModelState.CommonState.Loading -> {
                if (state.isLoading) dialogProgress.show(childFragmentManager, "0")
                else dialogProgress.dismiss()
            }

            is CommonModelState.CommonState.RemoveClientSuccess -> {
                viewModel.removeImageFireStorage(getDbClient(requireContext()), client.phone.toString())
                viewModel.getClients(getDbClient(requireContext()), clientlist)
            }

            is CommonModelState.CommonState.DeleteClientError -> {
                Util.exibirToast(
                    requireContext(),
                    getString(R.string.error_delete_client) + ":" + state.message
                )
            }

            is CommonModelState.CommonState.InsertClientError -> {
                Util.exibirToast(requireContext(), getString(R.string.error_save_client))
            }

            is CommonModelState.CommonState.GetClientsLoaded, CommonModelState.CommonState.FilterClientSuccess -> {
                clientlist.clear()
                clientlist.addAll(viewModel.model.clientsResult)
                clientAdapter.notifyDataSetChanged()
            }

            else -> {
            }
        }
    }
}




