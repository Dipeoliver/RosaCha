package com.clausfonseca.rosacha.domain.usecases.product

import com.clausfonseca.rosacha.domain.repository.ProductRepository
import javax.inject.Inject

class FirebaseRemoveImageProduct @Inject constructor(
    private val repository: ProductRepository
){
    operator fun invoke(dbProduct: String, id: String) = repository.removeImageProduct(dbProduct,id)
}