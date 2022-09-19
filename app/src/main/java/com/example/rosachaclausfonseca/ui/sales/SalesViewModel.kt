package com.example.rosachaclausfonseca.ui.sales

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class SalesViewModel : ViewModel() {
    private val _text = MutableLiveData<String>().apply {
        value = "This is Sales Fragment"
    }
    val text: LiveData<String> = _text
}