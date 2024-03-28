package com.clausfonseca.rosacha.data.repository

import android.graphics.Bitmap
import com.clausfonseca.rosacha.R
import com.clausfonseca.rosacha.domain.repository.ClientRepository
import com.clausfonseca.rosacha.model.ClientModel
import com.clausfonseca.rosacha.utils.Resource
import com.clausfonseca.rosacha.utils.Util
import com.clausfonseca.rosacha.utils.extencionFunctions.getDbClient
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import javax.inject.Inject

class ClientRepositoryImpl @Inject constructor(
    private val fireStore: FirebaseFirestore,
    private val firestorage: FirebaseStorage
) : ClientRepository {


    // implementar o select do nome  para buscar no firebase se a foto existe e salvar o url no cadastro
// do usuario.
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

            val reference = firestorage.reference.child(dbClient)
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
            val reference = firestorage.reference.child(dbClient).child("${id}.jpg")

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

}