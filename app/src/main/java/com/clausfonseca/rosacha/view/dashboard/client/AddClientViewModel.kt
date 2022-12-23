package com.clausfonseca.rosacha.view.dashboard.client

import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.FirebaseFirestore

class AddClientViewModel : ViewModel() {
    val db = FirebaseFirestore.getInstance()
}