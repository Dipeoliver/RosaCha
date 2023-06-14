package com.clausfonseca.rosacha.domain.repository
import kotlinx.coroutines.flow.Flow
import com.clausfonseca.rosacha.utils.Response

interface AuthRepository {
    fun firebaseSignIn(email : String, password : String) : Flow<Response<Boolean>>

}