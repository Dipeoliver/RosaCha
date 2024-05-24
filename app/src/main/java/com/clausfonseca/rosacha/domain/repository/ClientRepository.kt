package com.clausfonseca.rosacha.domain.repository

import android.graphics.Bitmap
import com.clausfonseca.rosacha.model.ClientModel
import com.clausfonseca.rosacha.utils.Resource
import kotlinx.coroutines.flow.Flow

interface ClientRepository {

    fun getUrlFile(dbClient: String, pictureName: String): Flow<Resource<Boolean>>

    fun getUrlStorage(dbClient: String, pictureName: String, bitmap: Bitmap): Flow<Resource<String>>

    fun getClients(dbClient: String, clientList: MutableList<ClientModel>): Flow<Resource<MutableList<ClientModel>>>

    fun insertClient(dbClient: String, clientModel: ClientModel): Flow<Resource<Boolean>>

    fun removeImageClient(dbClient: String, id: String): Flow<Resource<Boolean>>

    fun removeClient(dbClient: String, clientModel: ClientModel): Flow<Resource<Boolean>>

    fun updateClient(dbClient: String, clientModel: ClientModel): Flow<Resource<Boolean>>

    fun filterSearchClient(dbClient: String, fieldText: String, clientList: MutableList<ClientModel>): Flow<Resource<MutableList<ClientModel>>>
}