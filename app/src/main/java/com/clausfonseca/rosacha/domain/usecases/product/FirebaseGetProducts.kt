package com.clausfonseca.rosacha.domain.usecases.product

import com.clausfonseca.rosacha.domain.repository.ProductRepository
import com.clausfonseca.rosacha.model.ProductModel
import javax.inject.Inject

class FirebaseGetProducts @Inject constructor(
    private val repository: ProductRepository
) {
    operator fun invoke(
        dbProduct: String,
        productList: MutableList<ProductModel>
    ) =
        repository.getProducts(dbProduct, productList)
}