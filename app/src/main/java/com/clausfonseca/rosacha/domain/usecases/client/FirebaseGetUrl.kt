package com.clausfonseca.rosacha.domain.usecases.client

import com.clausfonseca.rosacha.domain.repository.ClientRepository
import com.clausfonseca.rosacha.model.ClientModel
import javax.inject.Inject

class FirebaseGetUrl  @Inject constructor(
    private val repository: ClientRepository
) {
    operator fun invoke(fileUrl: String) = repository.getUrlFile(fileUrl)
}