package com.clausfonseca.rosacha.model

import android.os.Parcel
import android.os.Parcelable

class Sales(
    var id: String? = "",
    var itens: MutableList<ItensSales>? = mutableListOf(),  // list [iten, price]
    var price: Double = 0.0,
    var discount: Double = 0.0,
    var paid: Double = 0.0,
    var totalPrice: Double = 0.0,
    var client: String? = "",
    var salesOwner: String? = "",  // pegar usuario logado Firebase.Auth
    var salesDate: String? = "",
    var month: Int? = 0,
    var year: Int? = 0,
    var qtyParcel: Int? = 0,
    var parcelDate: String? = "",
    var parceled: Double = 0.0,
): Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString(),
        TODO("itens"),
        parcel.readDouble(),
        parcel.readDouble(),
        parcel.readDouble(),
        parcel.readDouble(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readValue(Int::class.java.classLoader) as? Int,
        parcel.readValue(Int::class.java.classLoader) as? Int,
        parcel.readValue(Int::class.java.classLoader) as? Int,
        parcel.readString(),
        parcel.readDouble()
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(id)
        parcel.writeDouble(price)
        parcel.writeDouble(discount)
        parcel.writeDouble(paid)
        parcel.writeDouble(totalPrice)
        parcel.writeString(client)
        parcel.writeString(salesOwner)
        parcel.writeString(salesDate)
        parcel.writeValue(month)
        parcel.writeValue(year)
        parcel.writeValue(qtyParcel)
        parcel.writeString(parcelDate)
        parcel.writeDouble(parceled)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Sales> {
        override fun createFromParcel(parcel: Parcel): Sales {
            return Sales(parcel)
        }

        override fun newArray(size: Int): Array<Sales?> {
            return arrayOfNulls(size)
        }
    }
}
