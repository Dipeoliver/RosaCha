package com.clausfonseca.rosacha.ui.model

import android.os.Parcelable
import com.clausfonseca.rosacha.ui.helper.FirebaseHelper
import kotlinx.parcelize.Parcelize

@Parcelize
data class AddProduct(
    var id: String = "",
    var barcode: String = "",
    var reference: String = "",
    var description: String = "",
    var brand: String = "",
    var provider: String = "",
    var size: String = "",
    var color: String = "",
    var cost_price: Double = 0.0,
    var sales_price: Double = 0.0,
    var owner: Int = 0,
//    var productDate: String
) : Parcelable {
    //    gerar um id automático
    init {
        this.id = FirebaseHelper.getDatabase().push().key ?: ""
    }
}
