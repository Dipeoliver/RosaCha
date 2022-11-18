package com.clausfonseca.rosacha.view.dashboard.client

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.clausfonseca.rosacha.databinding.FragmentListClientBinding
import com.clausfonseca.rosacha.view.adapter.ClientAdapter
import com.clausfonseca.rosacha.view.helper.FirebaseHelper
import com.clausfonseca.rosacha.view.model.Client
import com.clausfonseca.rosacha.view.model.Product
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener


class ListClientFragment : Fragment() {
    private var _binding: FragmentListClientBinding? = null
    private val binding get() = _binding!!
    private lateinit var clientAdapter: ClientAdapter
    private val clientlist = mutableListOf<Client>()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentListClientBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getTasks()
    }

    private fun getTasks() {
        FirebaseHelper
            .getDatabase()
            .child("Client")
//            .child(FirebaseHelper.getIdUser() ?: "")
            .child("Clients")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        clientlist.clear()
                        for (snap in snapshot.children) {
                            val client = snap.getValue(Client::class.java) as Client
                            clientlist.add(client)
                        }
                        binding.textinfo.text = ""
//                        productlist.reverse() // trazer a lista do mais novo para o antigo
                        initAdapter()
                    } else {
                        binding.textinfo.text = "Nehuma Tarefa encontrada"

                    }
                    binding.progressBar4.isVisible = false
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(requireContext(), "Error", Toast.LENGTH_SHORT).show()
                }
            })
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
//            ClientAdapter.SELECT_EDIT -> {
//                val action = HomeFragmentDirections
//                    .actionHomeFragmentToFormTaskFragment(task)
//                findNavController().navigate(action)
//            }
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
