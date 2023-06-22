package com.clausfonseca.rosacha.model

import android.os.Parcel
import android.os.Parcelable
import com.clausfonseca.rosacha.data.firebase.FirebaseHelper


data class Product(
    var id: String? = "",
    var barcode: String? = "",
    var reference: String? = "",
    var description: String? = "",
    var quantity: Int? = 1,
    var brand: String? = "",
    var provider: String? = "",
    var size: String? = "",
    var color: String? = "",
    var costPrice: Double = 0.0,
    var salesPrice: Double = 0.0,
    var owner: String? = "",
    var productDate: String? = "",
    var urlImagem: String? = ""

) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readValue(Int::class.java.classLoader) as? Int,
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readDouble(),
        parcel.readDouble(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString()
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(id)
        parcel.writeString(barcode)
        parcel.writeString(reference)
        parcel.writeString(description)
        parcel.writeValue(quantity)
        parcel.writeString(brand)
        parcel.writeString(provider)
        parcel.writeString(size)
        parcel.writeString(color)
        parcel.writeDouble(costPrice)
        parcel.writeDouble(salesPrice)
        parcel.writeString(owner)
        parcel.writeString(productDate)
        parcel.writeString(urlImagem)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Product> {
        override fun createFromParcel(parcel: Parcel): Product {
            return Product(parcel)
        }

        override fun newArray(size: Int): Array<Product?> {
            return arrayOfNulls(size)
        }
    }
}

