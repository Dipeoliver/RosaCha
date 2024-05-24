package com.clausfonseca.rosacha.domain.usecases.client

import com.clausfonseca.rosacha.domain.repository.ClientRepository
import com.clausfonseca.rosacha.model.ClientModel
import javax.inject.Inject

class FirebaseFilterSearchClients @Inject constructor(
    private val repository: ClientRepository
) {
    operator fun invoke(
        dbClient: String,
        fieldText: String,
        clientList: MutableList<ClientModel>
    ) = repository.filterSearchClient(dbClient, fieldText,clientList)
}