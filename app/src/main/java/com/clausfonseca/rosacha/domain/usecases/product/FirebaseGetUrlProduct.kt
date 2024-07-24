package com.clausfonseca.rosacha.domain.usecases.product

import com.clausfonseca.rosacha.domain.repository.ProductRepository
import javax.inject.Inject

class FirebaseGetUrlProduct @Inject constructor(
    private val repository: ProductRepository
) {
    operator fun invoke(dbProduct: String, fileUrl: String) = repository.getUrlFile(dbProduct, fileUrl)
}