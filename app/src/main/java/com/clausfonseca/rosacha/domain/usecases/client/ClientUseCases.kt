package com.clausfonseca.rosacha.domain.usecases.client

data class ClientUseCases (
    val getUrlFile: FirebaseGetUrl,
    val getStorageUrl: StorageGetUrl,
    val insertClient: FirebaseInsertClient,
)