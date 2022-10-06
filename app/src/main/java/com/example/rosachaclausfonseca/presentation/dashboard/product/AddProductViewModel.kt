package com.example.rosachaclausfonseca.presentation.dashboard.product

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class AddProductViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "This is Inventory Fragment"
    }
    val text: LiveData<String> = _text
}