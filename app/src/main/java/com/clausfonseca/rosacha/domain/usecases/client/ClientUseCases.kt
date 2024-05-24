package com.clausfonseca.rosacha.domain.usecases.client

data class ClientUseCases (
    val getUrlFile: FirebaseGetUrl,
    val getStorageUrl: StorageGetUrl,
    val getClients: FirebaseGetClients,
    val insertClient: FirebaseInsertClient,
    val updateClient: FirebaseUpdateClient,
    val removeImage : FirebaseRemoveImage,
    val removeClient : FirebaseRemoveClient,
    val filterSearchClients : FirebaseFilterSearchClients,

)