package com.clausfonseca.rosacha.domain.usecases.product

import com.clausfonseca.rosacha.domain.repository.ProductRepository
import com.clausfonseca.rosacha.model.ProductModel
import javax.inject.Inject

class FirebaseRemoveProduct @Inject constructor(
    private val repository: ProductRepository
) {
    operator fun invoke(dbProduct: String, productModel: ProductModel) = repository.removeProduct(dbProduct, productModel)

}