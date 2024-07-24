package com.clausfonseca.rosacha.domain.usecases.client

import com.clausfonseca.rosacha.domain.repository.ClientRepository
import javax.inject.Inject

class FirebaseGetUrlClient  @Inject constructor(
    private val repository: ClientRepository
) {
    operator fun invoke(dbClient: String, fileUrl: String) = repository.getUrlFile(dbClient, fileUrl)
}