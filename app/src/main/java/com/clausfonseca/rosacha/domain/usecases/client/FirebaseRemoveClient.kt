package com.clausfonseca.rosacha.domain.usecases.client

import com.clausfonseca.rosacha.domain.repository.ClientRepository
import com.clausfonseca.rosacha.model.ClientModel
import javax.inject.Inject

class FirebaseRemoveClient @Inject constructor(
    private val repository: ClientRepository
) {
    operator fun invoke(dbClient: String, clientModel: ClientModel) = repository.removeClient(dbClient, clientModel)

}