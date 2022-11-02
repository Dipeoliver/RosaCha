package com.clausfonseca.rosacha.ui.model

import android.os.Parcelable
import com.clausfonseca.rosacha.ui.helper.FirebaseHelper
import kotlinx.parcelize.Parcelize

@Parcelize
class AddClient (
    var id: String = "",
    var name: String = "",
    var phone: String = "",
    var email: String = "",
    var birthday: String = ""
//    var clientDate: String
) : Parcelable {
    //    gerar um id automático
    init {
        this.id = FirebaseHelper.getDatabase().push().key ?: ""
    }
}