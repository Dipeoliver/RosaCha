package com.clausfonseca.rosacha.data.repository

import android.graphics.Bitmap
import com.clausfonseca.rosacha.domain.repository.ClientRepository
import com.clausfonseca.rosacha.utils.Resource
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
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
    override fun getUrlFile(pictureName: String): Flow<Resource<Boolean>> = flow {

        try {
            emit(Resource.Loading())
            val reference = fireStore.collection("@string/").document("$pictureName.jpg")
            reference.get().addOnSuccessListener { item ->
                if (item.exists()) {
                    CoroutineScope(Dispatchers.Main).launch {
                        withContext(Dispatchers.IO) {
                            emit(Resource.Success(true))
                        }
                    }
                } else {
                    CoroutineScope(Dispatchers.Main).launch {
                        withContext(Dispatchers.IO) {
                            emit(Resource.Success(false))
                        }
                    }
                }
            }
        } catch (e: Exception) {
            emit(
                Resource.Error(e)
            )
        }
    }

    override fun getUrlStorage(dbClient: String, pictureName: String, bitmap: Bitmap): Flow<Resource<String>> = flow {
        try {
            emit(Resource.Loading())

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
                        emit(Resource.Success(url))
                    }
                }
            }.addOnFailureListener { error ->
                CoroutineScope(Dispatchers.Main).launch {
                    withContext(Dispatchers.IO) {
                        emit(Resource.Success(error.toString()))
                    }
                }
            }

        } catch (e: Exception) {
            emit(
                Resource.Error(e)
            )
        }
    }
}