package com.clausfonseca.rosacha.domain.repository

import android.graphics.Bitmap
import com.clausfonseca.rosacha.R
import com.clausfonseca.rosacha.model.ClientModel
import com.clausfonseca.rosacha.utils.Resource
import com.clausfonseca.rosacha.utils.Util
import com.google.android.gms.common.api.Response
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow

interface ClientRepository {


    fun getUrlFile(pictureName: String): Flow<Resource<Boolean>>

    fun getUrlStorage(dbClient: String, pictureName: String, bitmap: Bitmap): Flow<Resource<String>>

    fun insertClient(dbClient: String, clientModel: ClientModel): Flow<Resource<Boolean>>

//    removeImage
//    updateClient
//        firebaseStorage = Firebase.storage
//    recoverClient
}