package com.clausfonseca.rosacha.domain.usecases.client

import com.clausfonseca.rosacha.domain.repository.ClientRepository
import javax.inject.Inject

class FirebaseGetUrl  @Inject constructor(
    private val repository: ClientRepository
) {
    operator fun invoke(fileUrl: String) = repository.getUrlFile(fileUrl)
}