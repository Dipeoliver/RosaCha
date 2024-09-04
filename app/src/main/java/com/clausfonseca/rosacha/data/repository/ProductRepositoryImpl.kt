package com.clausfonseca.rosacha.data.repository

import android.graphics.Bitmap
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target
import com.clausfonseca.rosacha.R
import com.clausfonseca.rosacha.domain.repository.ProductRepository
import com.clausfonseca.rosacha.model.ProductModel
import com.clausfonseca.rosacha.utils.Resource
import com.clausfonseca.rosacha.utils.Util
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import javax.inject.Inject

class ProductRepositoryImpl @Inject constructor(
    private val fireStore: FirebaseFirestore,
    private val firebaseStorage: FirebaseStorage
) : ProductRepository{
    override fun getUrlFile(dbProduct: String, pictureName: String): Flow<Resource<Boolean>> = callbackFlow {

        try {
            trySend(Resource.Loading())
            val reference = fireStore.collection(dbProduct).document(pictureName)
            reference.get().addOnSuccessListener { item ->
                if (item.exists()) {
                    trySend(Resource.Success(true)).isSuccess
                } else {
                    CoroutineScope(Dispatchers.Main).launch {
                        withContext(Dispatchers.IO) {
                            trySend(Resource.Success(false)).isSuccess
                        }
                    }
                }
            }
        }catch (e: Exception) {
            trySend(
                Resource.Error(e)
            )
        }
        awaitClose {
        }

    }

    override fun getUrlStorage(dbProduct: String, pictureName: String, bitmap: Bitmap): Flow<Resource<String>> = callbackFlow {
        try {
            trySend(Resource.Loading())

            val baos = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 50, baos)
            val data = baos.toByteArray()

            val reference = firebaseStorage.reference.child(dbProduct)
                .child("$pictureName.jpg")

            val uploadTask = reference.putBytes(data)

            uploadTask.continueWithTask { task ->
                if (!task.isSuccessful) {
                    task.exception.let { it ->
                        throw it!!
                    }
                }
                reference.downloadUrl
            }.addOnSuccessListener { task ->
                val url = task.toString()
                CoroutineScope(Dispatchers.Main).launch {
                    withContext(Dispatchers.IO) {
                        trySend(Resource.Success(url)).isSuccess
                    }
                }
            }.addOnFailureListener { error ->
                CoroutineScope(Dispatchers.Main).launch {
                    withContext(Dispatchers.IO) {
                        trySend(Resource.Success(error.toString())).isSuccess
                    }
                }
            }

        } catch (e: Exception) {
            trySend(
                Resource.Error(e)
            )
        }
        awaitClose {
        }
    }

    override fun getProducts(dbProduct: String, productModelList: MutableList<ProductModel>): Flow<Resource<MutableList<ProductModel>>> {
        TODO("Not yet implemented")
    }

    override fun getMoreProducts(dbProduct: String, productModelList: MutableList<ProductModel>): Flow<Resource<MutableList<ProductModel>>> {
        TODO("Not yet implemented")
    }

    override fun insertProduct(dbProduct: String, productModel: ProductModel): Flow<Resource<Boolean>> = callbackFlow {
        try {
            trySend(Resource.Loading())
            fireStore.collection(dbProduct).document(productModel.barcode.toString())
                .set(productModel).addOnCompleteListener {
                    if (it.isSuccessful || it.isComplete) {
                        CoroutineScope(Dispatchers.Main).launch {
                            withContext(Dispatchers.IO) {
                                trySend(Resource.Success(true)).isSuccess
                            }
                        }
                    } else {
                        CoroutineScope(Dispatchers.Main).launch {
                            withContext(Dispatchers.IO) {
                                trySend(Resource.Success(false)).isSuccess
                            }
                        }
                    }
                }.addOnFailureListener {
                    trySend(
                        Resource.Error(it)
                    )
                }
        } catch (e: Exception) {
            trySend(
                Resource.Error(e)
            )
        }
        awaitClose {
        }
    }

    override fun removeImageProduct(dbProduct: String, id: String): Flow<Resource<Boolean>> {
        TODO("Not yet implemented")
    }

    override fun removeProduct(dbProduct: String, productModel: ProductModel): Flow<Resource<Boolean>> {
        TODO("Not yet implemented")
    }

    override fun updateProduct(dbProduct: String, selectedProductModel: ProductModel): Flow<Resource<Boolean>>  = callbackFlow{
        try {
            val product = hashMapOf(
                // posso fazer update de apenas 1 campo se necessário
                "reference" to selectedProductModel.reference,
                "description" to selectedProductModel.description,
                "quantity" to selectedProductModel.quantity,
                "brand" to selectedProductModel.brand,
                "provider" to selectedProductModel.provider,
                "size" to selectedProductModel.size,
                "color" to selectedProductModel.color,
                "costPrice" to selectedProductModel.costPrice,
                "salesPrice" to selectedProductModel.salesPrice,
                "productDate" to selectedProductModel.productDate,
                "urlImagem" to selectedProductModel.urlImagem,
                "owner" to selectedProductModel.owner,
                "id" to selectedProductModel.id
            )

            trySend(Resource.Loading())
            fireStore.collection(dbProduct).document(selectedProductModel.barcode.toString())
                .update(product as Map<String, Any>).addOnSuccessListener {

                    CoroutineScope(Dispatchers.Main).launch {
                        withContext(Dispatchers.IO) {
                            trySend(Resource.Success(true)).isSuccess
                        }

                    }
                }.addOnFailureListener {
                    trySend(
                        Resource.Error(it)
                    )
                }
        } catch (e: Exception) {
            trySend(
                Resource.Error(e)
            )
        }
        awaitClose {
        }
    }

    override fun filterSearchProduct(
        dbProduct: String,
        fieldText: String,
        productModelList: MutableList<ProductModel>
    ): Flow<Resource<MutableList<ProductModel>>> {
        TODO("Not yet implemented")
    }

}
