package com.clausfonseca.rosacha.utils.pdf

import com.clausfonseca.rosacha.model.ItensSales

data class PdfDetails(
    val invoiceNumber: String,
    val costumerName: String,
    val date: String,
    val subTotal: Double,
    val discount: Double,
    val total: Double,
    val moneyPaid:Double,
    val qtyParcel:Int,
    val parcelValue:Double,
    val itemDetailsList: List<ItensSales>
)