package com.clausfonseca.rosacha.domain.usecases.product

import com.clausfonseca.rosacha.domain.repository.ProductRepository
import com.clausfonseca.rosacha.model.ProductModel
import javax.inject.Inject

class FirebaseFilterSearchProducts @Inject constructor(
    private val repository: ProductRepository
) {
    operator fun invoke(
        dbProduct: String,
        fieldText: String,
        productModelList: MutableList<ProductModel>
    ) = repository.filterSearchProduct(dbProduct, fieldText, productModelList)
}