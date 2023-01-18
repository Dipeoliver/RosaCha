package com.clausfonseca.rosacha.view.dashboard.client

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.clausfonseca.rosacha.databinding.FragmentClientAddBinding
import com.clausfonseca.rosacha.model.Client
import com.clausfonseca.rosacha.utils.mask.DateMask
import com.clausfonseca.rosacha.utils.mask.PhoneMask
import com.clausfonseca.rosacha.utils.mask.PhoneNumberFormatType
import com.clausfonseca.rosacha.utils.mask.validateEmailRegex
import java.lang.ref.WeakReference
import java.text.SimpleDateFormat
import java.util.*


class AddClientFragment : Fragment() {

    private lateinit var binding: FragmentClientAddBinding
    private val viewModel: AddClientViewModel by viewModels()

    private lateinit var client: Client
    private var newClient: Boolean = true


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentClientAddBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.edtNameClient.requestFocus()
        initListeners()
        configureComponents()
    }

    override fun onDestroyView() {
        super.onDestroyView()
    }

    private fun configureComponents() {
        //Mask to Phone
        val country = PhoneNumberFormatType.PT_BR // OR PhoneNumberFormatType.PT_BR
        val phoneFormatter = PhoneMask(WeakReference(binding.edtPhoneClient), country)
        binding.edtPhoneClient.addTextChangedListener(phoneFormatter)
//        binding.edtPhoneClient.addTextChangedListener(DateMask.mask(binding.edtPhoneClient, DateMask.FORMAT_FONE))

        //Mask to Date
        binding.edtBirthdayClient.addTextChangedListener(DateMask.mask(binding.edtBirthdayClient, DateMask.FORMAT_DATE))
    }

    private fun initListeners() {
        binding.btnAddClient.setOnClickListener {
            validateData()
        }

        binding.btnBack.setOnClickListener {
            val uri = Uri.parse("android-app://com.clausfonseca.rosacha/client_fragment")
            findNavController().navigate(uri)
        }
    }



    private fun validateData() {

        val phone = binding.edtPhoneClient.text.toString()
        val name = binding.edtNameClient.text.toString().trim()
        val birthday = binding.edtBirthdayClient.text.toString().trim()

        val email = binding.edtEmailClient.text.toString().trim()
        if (!email.validateEmailRegex(email)) {
            Toast.makeText(context, "Erro de validação do email", Toast.LENGTH_SHORT).show()
        } else {
            if (name.isNotEmpty() && phone.length > 13) {
//                binding.progressBar.isVisible = true

                client = Client()
                val date = Calendar.getInstance().time
                val dateTimeFormat = SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.getDefault())
                val clientDate = dateTimeFormat.format(date)

                client.name = name.uppercase()
                client.phone = phone
                client.email = email.lowercase()
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
    }

    private fun insertClient() {
        viewModel.db.collection("Clients").document(client.id)
            .set(client).addOnCompleteListener {
                Toast.makeText(
                    requireContext(),
                    "Cliente adicionado com sucesso",
                    Toast.LENGTH_SHORT
                ).show()
                cleaner()
//                binding.progressBar.isVisible = false
            }.addOnFailureListener {
                Toast.makeText(requireContext(), "Erro ao salvar Cliente", Toast.LENGTH_SHORT)
                    .show()
            }
    }

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