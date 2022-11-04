package com.clausfonseca.rosacha.view.model

import android.os.Parcelable
import com.clausfonseca.rosacha.view.helper.FirebaseHelper
import kotlinx.parcelize.Parcelize

@Parcelize

class AddSales(
    var id: String = "",
    var itens: String = "",  // list [iten, price]
    var price: String = "",
    var discount: String = "",
    var totalPrice: String = "",
    var client: String = "",
//    val salesOwner: String  // pegar usuario logado Firebase.Auth
//    var salesDate: String
) : Parcelable {
    //    gerar um id automático
    init {
        this.id = FirebaseHelper.getDatabase().push().key ?: ""
    }
}