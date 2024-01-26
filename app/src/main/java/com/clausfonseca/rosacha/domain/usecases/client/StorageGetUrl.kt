package com.clausfonseca.rosacha.domain.usecases.client

import android.graphics.Bitmap
import com.clausfonseca.rosacha.domain.repository.ClientRepository
import javax.inject.Inject

class StorageGetUrl @Inject constructor(
    private val repository: ClientRepository
) {
    operator fun invoke(dbClient: String, pictureName: String, bitmap: Bitmap) = repository.getUrlStorage(dbClient,pictureName,bitmap)

}