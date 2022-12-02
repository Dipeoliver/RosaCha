package com.clausfonseca.rosacha.view.dashboard.client

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import com.clausfonseca.rosacha.databinding.FragmentAddClientBinding
import com.clausfonseca.rosacha.view.dashboard.HomeFragment
import com.clausfonseca.rosacha.view.helper.FirebaseHelper
import com.clausfonseca.rosacha.view.model.Client
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

class AddClientFragment : Fragment() {

    private val args: AddClientFragmentArgs by navArgs()

    private var _binding: FragmentAddClientBinding? = null
    private val binding get() = _binding!!
    private lateinit var client: Client
    private var newClient: Boolean = true

    private val db = FirebaseFirestore.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentAddClientBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.edtNameClient.requestFocus()
        initListeners()
//        getArgs()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun initListeners() {
        binding.btnAddClient.setOnClickListener {
            validateData()
        }
    }

    private fun validateData() {
        val name = binding.edtNameClient.text.toString().trim()
        val phone = binding.edtPhoneClient.text.toString().trim()
        val email = binding.edtEmailClient.text.toString().trim()
        val birthday = binding.edtBirthdayClient.text.toString().trim()

        if (name.isNotEmpty() && phone.isNotEmpty()) {
            binding.progressBar.isVisible = true

            client = Client()
            val date = Calendar.getInstance().time
            val dateTimeFormat = SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.getDefault())
            val clientDate = dateTimeFormat.format(date)

            client.name = name
            client.phone = phone
            client.email = email
            client.birthday = birthday
            client.clientDate = clientDate

            insertClient()
        } else {
            Toast.makeText(
                requireContext(),
                "Preencher os campos Obrigatórios",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    private fun insertClient() {
        db.collection("Clients").document(client.id)
            .set(client).addOnCompleteListener {
                Toast.makeText(
                    requireContext(),
                    "Cliente adicionado com sucesso",
                    Toast.LENGTH_SHORT
                ).show()
                cleaner()
                binding.progressBar.isVisible = false
            }.addOnFailureListener {
                Toast.makeText(requireContext(), "Erro ao salvar Cliente", Toast.LENGTH_SHORT)
                    .show()
            }
    }

    private fun insertClient_RealtimeDatabase() {
        FirebaseHelper
            .getDatabase()
            .child("Client")
//            .child(FirebaseHelper.getIdUser() ?: "") // id do usuario
            .child("Clients") // id do usuario
            .child(client?.id ?: "")
            .setValue(client)
            .addOnCompleteListener { task ->
                if (newClient) { // nova tarefa
                    if (task.isSuccessful) {
                        Toast.makeText(
                            requireContext(),
                            "Client  Salvo com sucesso",
                            Toast.LENGTH_SHORT
                        ).show()
                        cleaner()
                        binding.progressBar.isVisible = false
                    } else { // iditando tarefa
                        binding.progressBar.isVisible = false
                        Toast.makeText(
                            requireContext(),
                            "Tarefa Atualizada com Sucesso",
                            Toast.LENGTH_SHORT
                        )
                    }
                } else {
                    binding.progressBar.isVisible = false

                    Toast.makeText(requireContext(), "Erro ao salvar Tarefa", Toast.LENGTH_SHORT)
                        .show()
                }
            }.addOnFailureListener {
                binding.progressBar.isVisible = false
                Toast.makeText(requireContext(), "Erro ao salvar Tarefa", Toast.LENGTH_SHORT).show()
            }
    }

//    private fun getArgs() {
//        args.let {
//            if (it.clientArguments != null) {
//                client = it.clientArguments
//            }
//
//            if (args.clientArguments != null) configTask()
//        }
//    }
//
//    private fun configTask() {
//        newClient = false
////        binding.txtTollbar.text = "Editando uma tarefa"
//        binding.edtNameClient.setText(client?.name)
//        binding.edtPhoneClient.setText(client?.phone)
//        binding.edtEmailClient.setText(client?.email)
//        binding.edtBirthdayClient.setText(client?.birthday)
//    }

    private fun cleaner() {
        binding.apply {
            edtNameClient.text.clear()
            edtPhoneClient.text.clear()
            edtEmailClient.text.clear()
            edtBirthdayClient.text.clear()
            edtNameClient.requestFocus()
        }
    }
}