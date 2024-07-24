package com.clausfonseca.rosacha.domain.usecases.client

data class ClientUseCases (
    val getUrlFile: FirebaseGetUrlClient,
    val getStorageUrl: StorageGetUrlClient,
    val getClients: FirebaseGetClients,
    val getMoreClients: FirebaseGetMoreClients,
    val insertClient: FirebaseInsertClient,
    val updateClient: FirebaseUpdateClient,
    val removeImage : FirebaseRemoveImageClient,
    val removeClient : FirebaseRemoveClient,
    val filterSearchClients : FirebaseFilterSearchClients,

    )