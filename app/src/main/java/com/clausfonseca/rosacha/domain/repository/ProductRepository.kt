package com.clausfonseca.rosacha.domain.repository

import android.graphics.Bitmap
import com.clausfonseca.rosacha.model.ProductModel
import com.clausfonseca.rosacha.utils.Resource
import kotlinx.coroutines.flow.Flow

interface ProductRepository {
    fun getUrlFile(dbProduct: String, pictureName: String): Flow<Resource<Boolean>>

    fun getUrlStorage(dbProduct: String, pictureName: String, bitmap: Bitmap): Flow<Resource<String>>

    fun getProducts(dbProduct: String, productModelList: MutableList<ProductModel>): Flow<Resource<MutableList<ProductModel>>>

    fun getMoreProducts(
        dbProduct: String,
        productModelList: MutableList<ProductModel>
    ): Flow<Resource<MutableList<ProductModel>>>

    fun insertProduct(dbProduct: String, productModel: ProductModel): Flow<Resource<Boolean>>

    fun removeImageProduct(dbProduct: String, id: String): Flow<Resource<Boolean>>

    fun removeProduct(dbProduct: String, productModel: ProductModel): Flow<Resource<Boolean>>

    fun updateProduct(dbProduct: String, productModel: ProductModel): Flow<Resource<Boolean>>

    fun filterSearchProduct(
        dbProduct: String,
        fieldText: String,
        productModelList: MutableList<ProductModel>
    ): Flow<Resource<MutableList<ProductModel>>>
}