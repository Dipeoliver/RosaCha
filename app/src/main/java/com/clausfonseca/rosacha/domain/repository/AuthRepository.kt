package com.clausfonseca.rosacha.domain.repository
import kotlinx.coroutines.flow.Flow
import com.clausfonseca.rosacha.utils.Resource

interface AuthRepository {
    fun firebaseSignIn(email : String, password : String) : Flow<Resource<Boolean>>
    fun firebaseSignOut() : Flow<Resource<Boolean>>
    fun firebaseRecoverPassword(email : String) : Flow<Resource<Boolean>>
    fun firebaseRegisterUser(email: String, password: String): Flow<Resource<Boolean>>

}