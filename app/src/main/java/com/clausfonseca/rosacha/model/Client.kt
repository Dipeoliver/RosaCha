package com.clausfonseca.rosacha.model

import android.os.Parcelable
import com.clausfonseca.rosacha.data.firebase.FirebaseHelper
import kotlinx.parcelize.Parcelize

@Parcelize
class Client(
    var id: String = "",
    var name: String = "",
    var phone: String = "",
    var email: String = "",
    var birthday: String = "",
    var clientDate: String ="",
    var urlImagem: String= ""

) : Parcelable {
    //    gerar um id automático
    init {
        this.id = FirebaseHelper.getDatabase().push().key ?: ""
    }
}