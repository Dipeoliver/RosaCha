package com.clausfonseca.rosacha.utils.pdf

import com.clausfonseca.rosacha.model.ItensSales
import java.util.Date


data class PdfDetails(
    val invoiceNumber: String,
    val costumerName: String,
    val date: String,
    val subTotal: Double,
    val discount: Double,
    val total: Double,
    val moneyPaid:Double,
    val itemDetailsList: List<ItensSales>
)