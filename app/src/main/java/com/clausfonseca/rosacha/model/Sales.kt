package com.clausfonseca.rosacha.model

data class Sales(
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
)
