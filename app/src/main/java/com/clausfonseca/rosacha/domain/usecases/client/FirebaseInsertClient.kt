package com.clausfonseca.rosacha.domain.usecases.client

import com.clausfonseca.rosacha.domain.repository.ClientRepository
import com.clausfonseca.rosacha.model.ClientModel
import javax.inject.Inject

class FirebaseInsertClient @Inject constructor(
    private val repository: ClientRepository
) {
    operator fun invoke(dbClient: String, clientModel: ClientModel) = repository.insertClient(dbClient, clientModel)
}