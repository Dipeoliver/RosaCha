package com.clausfonseca.rosacha.data.repository

import com.clausfonseca.rosacha.domain.repository.AuthRepository
import com.clausfonseca.rosacha.utils.Resource
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import java.lang.Exception
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val auth: FirebaseAuth
) : AuthRepository {
    override fun firebaseSignIn(email: String, password: String): Flow<Resource<Boolean>> = flow {
        try {
            emit(Resource.Loading())
            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener {
                }.await()
            emit(Resource.Success(true))
        } catch (e: Exception) {
            emit(Resource.Error(e))
        }
    }
}