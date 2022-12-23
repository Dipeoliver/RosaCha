package com.clausfonseca.rosacha.view.dashboard

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class DashboardViewModel: ViewModel() {
    val homeVisibilityOb = MutableLiveData<Int>()
}