package com.clausfonseca.rosacha.data.repository

import android.graphics.Bitmap
import android.util.Log
import com.clausfonseca.rosacha.domain.repository.ClientRepository
import com.clausfonseca.rosacha.model.ClientModel
import com.clausfonseca.rosacha.utils.Resource
import com.clausfonseca.rosacha.utils.Util
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

class ClientRepositoryImpl @Inject constructor(
    private val fireStore: FirebaseFirestore,
    private val firebaseStorage: FirebaseStorage
) : ClientRepository {
    private var lastResult: DocumentSnapshot? = null
    var nextquery: Query? = null

    override fun getUrlFile(dbClient: String, pictureName: String): Flow<Resource<Boolean>> = callbackFlow {

        try {
            trySend(Resource.Loading())
            val reference = fireStore.collection(dbClient).document(pictureName)
            reference.get().addOnSuccessListener { item ->
                if (item.exists()) {
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
            }
        } catch (e: Exception) {
            trySend(
                Resource.Error(e)
            )
        }
        awaitClose {
        }
    }

    override fun getUrlStorage(dbClient: String, pictureName: String, bitmap: Bitmap): Flow<Resource<String>> = callbackFlow {
        try {
            trySend(Resource.Loading())

            val baos = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 50, baos)
            val data = baos.toByteArray()

            val reference = firebaseStorage.reference.child(dbClient)
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

    override fun getClients(dbClient: String, clientList: MutableList<ClientModel>): Flow<Resource<MutableList<ClientModel>>> =
        callbackFlow {
            try {
                trySend(Resource.Loading())
                fireStore.collection(dbClient).orderBy("name").get().addOnSuccessListener { results ->
                                                                // tiramos o .limit(10)
                    if (results.size() > 0) {
//                        clientList.clear()

                        for (result in results) {
//                            val key = result.id // pegar o nome  da pasta do documento
                            val client = result.toObject(ClientModel::class.java)
                            clientList.add(client)
                        }
                        lastResult = results.documents[results.size() - 1]

                        nextquery = fireStore
                            .collection(dbClient)
                            .orderBy("phone")
                            .startAfter(lastResult)
                            .limit(10)




                        trySend(Resource.Success(clientList))
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

    override fun getMoreClients(
        dbClient: String,
        clientList: MutableList<ClientModel>
    ): Flow<Resource<MutableList<ClientModel>>> =
        callbackFlow {
            try {
//                val nextQuery = fireStore
//                    .collection(dbClient)
//                    .orderBy("name")
//                    .startAfter(lastResult)
//                    .limit(10)
                nextquery?.get()?.addOnSuccessListener { results ->
                    Log.d("****nextqueryClient", "${(nextquery?.get()?.addOnSuccessListener {})}")
                    if (results.size() > 0) {
                        Log.d("****ClientOK", "${nextquery?.get()}")

                        lastResult = results.documents[results.size() - 1]
                        fireStore.collection(dbClient).orderBy("phone").startAfter(lastResult).limit(10)

                        for (result in results) {
                            val clientModel = result.toObject(ClientModel::class.java)
                            clientList.add(clientModel)
                        }
                        trySend(Resource.Success(clientList))
//                            clientAdapter.notifyDataSetChanged()
                    } else {
                        Log.d("****Client", "${nextquery?.get()}")
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

    override fun insertClient(dbClient: String, clientModel: ClientModel): Flow<Resource<Boolean>> = callbackFlow {

        try {
            trySend(Resource.Loading())
            fireStore.collection(dbClient).document(clientModel.phone.toString())
                .set(clientModel).addOnCompleteListener {
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

    override fun removeImageClient(dbClient: String, id: String): Flow<Resource<Boolean>> = callbackFlow {
        try {
            trySend(Resource.Loading())
            val reference = firebaseStorage.reference.child(dbClient).child("${id}.jpg")

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

    override fun updateClient(dbClient: String, clientModel: ClientModel): Flow<Resource<Boolean>> = callbackFlow {
        try {

            val client = hashMapOf(
                "name" to clientModel.name,
                "email" to clientModel.email,
                "birthday" to clientModel.birthday,
                "clientDate" to clientModel.clientDate,
                "urlImagem" to clientModel.urlImagem,

                )

            trySend(Resource.Loading())
            fireStore.collection(dbClient).document(clientModel.phone.toString())
                .update(client as Map<String, Any>).addOnSuccessListener {

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

    override fun filterSearchClient(
        dbClient: String,
        fieldText: String,
        clientList: MutableList<ClientModel>
    ): Flow<Resource<MutableList<ClientModel>>> = callbackFlow {
        try {
            trySend(Resource.Loading())

            fireStore.collection(dbClient).orderBy("name").startAt(fieldText)
                .endAt(fieldText + "\uf8ff").limit(5).get().addOnSuccessListener { results ->
                    if (results.size() > 0) {
                        clientList.clear()
                        for (result in results) {
                            val clientModel1 = result.toObject(ClientModel::class.java)
                            clientList.add(clientModel1)
                        }
                        trySend(Resource.Success(clientList))
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

    override fun removeClient(dbClient: String, clientModel: ClientModel): Flow<Resource<Boolean>> = callbackFlow {
        try {
            trySend(Resource.Loading())

            val reference = fireStore.collection(dbClient)
            clientModel.phone?.let { it ->
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