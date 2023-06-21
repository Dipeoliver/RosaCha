package com.clausfonseca.rosacha.domain.usecases

import com.clausfonseca.rosacha.domain.repository.AuthRepository
import javax.inject.Inject

class FirebaseSignOut @Inject constructor(
    private val repository: AuthRepository
){
    operator fun invoke() = repository.firebaseSignOut()
}