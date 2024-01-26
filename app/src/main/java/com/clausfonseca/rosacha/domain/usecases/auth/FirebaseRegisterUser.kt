package com.clausfonseca.rosacha.domain.usecases.auth

import com.clausfonseca.rosacha.domain.repository.AuthRepository
import javax.inject.Inject

class FirebaseRegisterUser @Inject constructor(
    private val repository: AuthRepository
){
    operator fun invoke(email: String, password: String) = repository.firebaseRegisterUser(email,password)

}