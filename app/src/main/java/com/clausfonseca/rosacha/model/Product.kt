package com.clausfonseca.rosacha.model

import android.os.Parcelable
import com.clausfonseca.rosacha.data.firebase.FirebaseHelper
import kotlinx.parcelize.Parcelize

@Parcelize
data class Product(
    var id: String = "",
    var barcode: String = "",
    var reference: String = "",
    var description: String = "",
    var brand: String = "",
    var provider: String = "",
    var size: String = "",
    var color: String = "",
    var costPrice: Double = 0.0,
    var salesPrice: Double = 0.0,
    var owner: String ="",
    var productDate: String ="",
    var urlImagem: String= ""

) : Parcelable {
    //    gerar um id automático
    init {
        this.id = FirebaseHelper.getDatabase().push().key ?: ""
    }
}
