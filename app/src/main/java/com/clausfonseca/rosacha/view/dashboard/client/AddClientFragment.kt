package com.clausfonseca.rosacha.view.dashboard.client

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import com.clausfonseca.rosacha.databinding.FragmentAddClientBinding
import com.clausfonseca.rosacha.view.helper.FirebaseHelper
import com.clausfonseca.rosacha.view.model.AddClient

class AddClientFragment : Fragment() {

    private var _binding: FragmentAddClientBinding? = null
    private val binding get() = _binding!!

    private lateinit var addClient: AddClient


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentAddClientBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun validateData() {
        binding.btnAddClient.setOnClickListener {
            val name = binding.edtNameClient.text.toString().trim()
            val phone = binding.edtPhoneClient.text.toString().trim()
            val email = binding.edtEmailClient.text.toString().trim()
            val birthday = binding.edtBirthdayClient.text.toString().trim()

            if (name.isNotEmpty() && phone.isNotEmpty()) {
                binding.progressBar.isVisible = true

//                val date = Calendar.getInstance().time
//                var dateTimeFormat = SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.getDefault())
//                val productDate = dateTimeFormat.format(date)

                addClient.name = name
                addClient.phone = phone
                addClient.email = email
                addClient.birthday = birthday

//                addProduct.productDate = productDate
                insertClient()
            } else {
                Toast.makeText(
                    requireContext(),
                    "Preencher os campos Obrigatórios",
                    Toast.LENGTH_LONG
                ).show()
            }

        }
    }

    private fun insertClient() {
        FirebaseHelper
            .getDatabase()
            .child("addProduct")
            .child(FirebaseHelper.getIdUser() ?: "") // id do usuario
            .child(addClient.id)
            .setValue(addClient)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(
                        requireContext(),
                        "Client  Salvo com sucesso",
                        Toast.LENGTH_SHORT
                    ).show()
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
}