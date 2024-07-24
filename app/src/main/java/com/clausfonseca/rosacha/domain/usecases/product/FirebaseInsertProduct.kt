package com.clausfonseca.rosacha.domain.usecases.product

import com.clausfonseca.rosacha.domain.repository.ProductRepository
import com.clausfonseca.rosacha.model.ProductModel
import javax.inject.Inject

class FirebaseInsertProduct @Inject constructor(
    private val repository: ProductRepository
) {
    operator fun invoke(dbProduct: String, productModel: ProductModel) = repository.insertProduct(dbProduct, productModel)
}