package com.example.rosachaclausfonseca.presentation.dashboard.client

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class ClientViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "Diego"
    }
    // receber dados para visualizar na tela
    val text: LiveData<String> = _text
}