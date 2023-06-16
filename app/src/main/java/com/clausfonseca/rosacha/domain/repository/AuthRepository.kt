package com.clausfonseca.rosacha.domain.repository
import kotlinx.coroutines.flow.Flow
import com.clausfonseca.rosacha.utils.Resource

interface AuthRepository {
    fun firebaseSignIn(email : String, password : String) : Flow<Resource<Boolean>>

}