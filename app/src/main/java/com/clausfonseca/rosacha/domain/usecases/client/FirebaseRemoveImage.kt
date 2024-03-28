package com.clausfonseca.rosacha.domain.usecases.client

import com.clausfonseca.rosacha.domain.repository.ClientRepository
import javax.inject.Inject

class FirebaseRemoveImage @Inject constructor(
    private val repository: ClientRepository
){
    operator fun invoke(dbClient: String, id: String) = repository.removeImageClient(dbClient,id)
}