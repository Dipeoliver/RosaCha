package com.clausfonseca.rosacha.data.repository

import android.graphics.Bitmap
import com.clausfonseca.rosacha.domain.repository.ProductRepository
import com.clausfonseca.rosacha.model.ProductModel
import com.clausfonseca.rosacha.utils.Resource
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
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
) : ProductRepository {
    private var lastResult: DocumentSnapshot? = null
    private var nextQuery: Query? = null
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
        } catch (e: Exception) {
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

    override fun getProducts(
        dbProduct: String,
        productModelList: MutableList<ProductModel>
    ): Flow<Resource<MutableList<ProductModel>>> =
        callbackFlow {
            try {
                trySend(Resource.Loading())
                fireStore.collection(dbProduct).orderBy("description").get().addOnSuccessListener { results ->
                    // tiramos o .limit(10)
                    if (results.size() > 0) {
//                        clientList.clear()

                        for (result in results) {
//                            val key = result.id // pegar o nome  da pasta do documento
                            val product1 = result.toObject(ProductModel::class.java)
                            productModelList.add(product1)
                        }
                        lastResult = results.documents[results.size() - 1]
                        nextQuery = fireStore
                            .collection(dbProduct)
                            .orderBy("description")
                            .startAfter(lastResult)
                            .limit(10)
                        trySend(Resource.Success(productModelList))
                    } else {
                        trySend(Resource.Success(mutableListOf()))
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

    override fun getMoreProducts(
        dbProduct: String,
        productModelList: MutableList<ProductModel>
    ): Flow<Resource<MutableList<ProductModel>>> =callbackFlow {
        try {
            nextQuery?.get()?.addOnSuccessListener { results ->
                if (results.size() > 0) {
                    lastResult = results.documents[results.size() - 1]
                    fireStore.collection(dbProduct).orderBy("description").startAfter(lastResult).limit(10)

                    for (result in results) {
                        val productModel1 = result.toObject(ProductModel::class.java)
                        productModelList.add(productModel1)
                    }
                    trySend(Resource.Success(productModelList))
//                            clientAdapter.notifyDataSetChanged()
                } else {
                    trySend(Resource.Success(mutableListOf()))
                }
            }?.addOnFailureListener() { error ->
                trySend(
                    Resource.Error(error)
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

    override fun removeImageProduct(dbProduct: String, id: String): Flow<Resource<Boolean>> = callbackFlow {
        try {
            trySend(Resource.Loading())
            val reference = firebaseStorage.reference.child(dbProduct).child("${id}.jpg")

            reference.delete().addOnSuccessListener { task ->
                trySend(Resource.Success(true)).isSuccess
            }.addOnFailureListener { error ->
                trySend(
                    Resource.Error(error)
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


    override fun updateProduct(dbProduct: String, selectedProductModel: ProductModel): Flow<Resource<Boolean>> = callbackFlow {
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
    ): Flow<Resource<MutableList<ProductModel>>> = callbackFlow {
        try {
            trySend(Resource.Loading())

            fireStore.collection(dbProduct).orderBy("name").startAt(fieldText)
                .endAt(fieldText + "\uf8ff").limit(5).get().addOnSuccessListener { results ->
                    if (results.size() > 0) {
                        productModelList.clear()
                        for (result in results) {
                            val productModel1 = result.toObject(ProductModel::class.java)
                            productModelList.add(productModel1)
                        }
                        trySend(Resource.Success(productModelList))
                    }
                }.addOnFailureListener { error ->
                    trySend(
                        Resource.Error(error)
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

    override fun removeProduct(dbProduct: String, productModel: ProductModel): Flow<Resource<Boolean>> = callbackFlow {
        try {
            trySend(Resource.Loading())

            val reference = fireStore.collection(dbProduct)
            productModel.barcode?.let { it ->
                reference.document(it).delete().addOnCompleteListener() { task ->

                    if (task.isSuccessful) {
                        trySend(Resource.Success(true)).isSuccess
                    }
                }.addOnFailureListener { error ->
                    trySend(
                        Resource.Error(error)
                    )
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

}
