package com.clausfonseca.rosacha.utils.pdf

import com.clausfonseca.rosacha.model.ItensSales
import com.clausfonseca.rosacha.model.Sales

data class PdfDetails(
    val invoiceNumber: String,
    val sales: Sales
)