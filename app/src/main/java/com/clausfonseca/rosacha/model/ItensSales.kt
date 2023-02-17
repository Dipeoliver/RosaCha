package com.clausfonseca.rosacha.model

import android.os.Parcel
import android.os.Parcelable
import com.clausfonseca.rosacha.data.firebase.FirebaseHelper

class ItensSales(
    var id: String? = "",
    var barcode: String? = "",
    var description: String? = "",
    var salesPrice: Double? = 0.0,

    ) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readValue(Double::class.java.classLoader) as? Double
    ) {
    }

    init {
        this.id = FirebaseHelper.getDatabase().push().key ?: ""
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(id)
        parcel.writeString(barcode)
        parcel.writeString(description)
        parcel.writeValue(salesPrice)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<ItensSales> {
        override fun createFromParcel(parcel: Parcel): ItensSales {
            return ItensSales(parcel)
        }

        override fun newArray(size: Int): Array<ItensSales?> {
            return arrayOfNulls(size)
        }
    }
}