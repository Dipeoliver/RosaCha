package com.clausfonseca.rosacha.domain.usecases.client

import com.clausfonseca.rosacha.domain.repository.ClientRepository
import com.clausfonseca.rosacha.model.ClientModel
import javax.inject.Inject

class FirebaseGetClients @Inject constructor(
    private val repository: ClientRepository
) {
    operator fun invoke(
        dbClient: String,
        clientList: MutableList<ClientModel>
    ) =
        repository.getClients(dbClient, clientList)
}