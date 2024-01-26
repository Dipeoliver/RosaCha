package com.clausfonseca.rosacha.model

import android.os.Parcel
import android.os.Parcelable
import com.clausfonseca.rosacha.data.firebase.FirebaseHelper

class ClientModel(
    var id: String? = "",
    var name: String? = "",
    var phone: String? = "",
    var email: String? = "",
    var birthday: String? = "",
    var clientDate: String? = "",
    var urlImagem: String? = ""

) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString()
    ) {
    }

    //    gerar um id automático
    init {
        this.id = FirebaseHelper.getDatabase().push().key ?: ""
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(id)
        parcel.writeString(name)
        parcel.writeString(phone)
        parcel.writeString(email)
        parcel.writeString(birthday)
        parcel.writeString(clientDate)
        parcel.writeString(urlImagem)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<ClientModel> {
        override fun createFromParcel(parcel: Parcel): ClientModel {
            return ClientModel(parcel)
        }

        override fun newArray(size: Int): Array<ClientModel?> {
            return arrayOfNulls(size)
        }
    }
}