package com.clausfonseca.rosacha.domain.usecases.product

import android.graphics.Bitmap
import com.clausfonseca.rosacha.domain.repository.ProductRepository
import javax.inject.Inject

class StorageGetUrlProduct @Inject constructor(
    private val repository: ProductRepository
) {
    operator fun invoke(dbProduct: String, pictureName: String, bitmap: Bitmap) = repository.getUrlStorage(dbProduct,pictureName,bitmap)

}