package com.example.rosachaclausfonseca.ui.price

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class PriceViewModel : ViewModel() {
    private val _text = MutableLiveData<String>().apply {
        value = "This is Price Fragment"
    }
    val text: LiveData<String> = _text
}