package com.clausfonseca.rosacha.data.repository

import android.graphics.Bitmap
import com.clausfonseca.rosacha.domain.repository.ProductRepository
import com.clausfonseca.rosacha.model.ProductModel
import com.clausfonseca.rosacha.utils.Resource
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ProductRepositoryImpl @Inject constructor(
    private val fireStore: FirebaseFirestore,
    private val firebaseStorage: FirebaseStorage
) : ProductRepository{
    override fun getUrlFile(dbProduct: String, pictureName: String): Flow<Resource<Boolean>> {
        TODO("Not yet implemented")
    }

    override fun getUrlStorage(dbProduct: String, pictureName: String, bitmap: Bitmap): Flow<Resource<String>> {
        TODO("Not yet implemented")
    }

    override fun getProducts(dbProduct: String, productModelList: MutableList<ProductModel>): Flow<Resource<MutableList<ProductModel>>> {
        TODO("Not yet implemented")
    }

    override fun getMoreProducts(dbProduct: String, productModelList: MutableList<ProductModel>): Flow<Resource<MutableList<ProductModel>>> {
        TODO("Not yet implemented")
    }

    override fun insertProduct(dbProduct: String, productModel: ProductModel): Flow<Resource<Boolean>> {
        TODO("Not yet implemented")
    }

    override fun removeImageProduct(dbProduct: String, id: String): Flow<Resource<Boolean>> {
        TODO("Not yet implemented")
    }

    override fun removeProduct(dbProduct: String, productModel: ProductModel): Flow<Resource<Boolean>> {
        TODO("Not yet implemented")
    }

    override fun updateProduct(dbProduct: String, productModel: ProductModel): Flow<Resource<Boolean>> {
        TODO("Not yet implemented")
    }

    override fun filterSearchProduct(
        dbProduct: String,
        fieldText: String,
        productModelList: MutableList<ProductModel>
    ): Flow<Resource<MutableList<ProductModel>>> {
        TODO("Not yet implemented")
    }

}
