package com.clausfonseca.rosacha.domain.usecases.client

import com.clausfonseca.rosacha.domain.repository.ClientRepository
import com.clausfonseca.rosacha.model.ClientModel
import com.google.firebase.firestore.DocumentSnapshot
import javax.inject.Inject

class FirebaseGetMoreClients @Inject constructor(
    private val repository: ClientRepository
) {
    operator fun invoke(
        dbClient: String,
        clientList: MutableList<ClientModel>
    ) =
        repository.getMoreClients(dbClient, clientList)
}