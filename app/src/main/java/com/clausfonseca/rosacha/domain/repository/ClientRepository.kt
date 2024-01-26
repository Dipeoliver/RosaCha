package com.clausfonseca.rosacha.domain.repository

import android.graphics.Bitmap
import com.clausfonseca.rosacha.utils.Resource
import com.google.android.gms.common.api.Response
import kotlinx.coroutines.flow.Flow

interface ClientRepository {


    fun getUrlFile(pictureName: String) : Flow<Resource<Boolean>>

    fun getUrlStorage(dbClient: String, pictureName: String, bitmap: Bitmap) : Flow<Resource<String>>
}