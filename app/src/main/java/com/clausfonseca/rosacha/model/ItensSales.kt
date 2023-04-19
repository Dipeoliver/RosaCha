package com.clausfonseca.rosacha.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class ItensSales(
    var id: String? = "",
    var barcode: String? = "",
    var description: String? = "",
    var salesPrice: Double? = 0.0,

    ) : Parcelable
