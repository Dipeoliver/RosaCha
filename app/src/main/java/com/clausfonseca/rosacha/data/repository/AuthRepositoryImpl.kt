package com.clausfonseca.rosacha.data.repository

import com.clausfonseca.rosacha.domain.repository.AuthRepository
import com.clausfonseca.rosacha.utils.Resource
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
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

    override fun firebaseSignOut(): Flow<Resource<Boolean>> = flow {
        try {
            emit(Resource.Loading())
            auth.signOut()
            emit(Resource.Success(true))
        } catch (e: Exception) {
            emit(
                Resource.Error(e)
            )
        }
    }

    override fun firebaseRecoverPassword(email: String): Flow<Resource<Boolean>> = flow {
        try{
            emit(Resource.Loading())
            auth.sendPasswordResetEmail(email)
            emit(Resource.Success(true))
        }catch (e: Exception){
            emit(
                Resource.Error(e)
            )
        }
    }

    override fun firebaseRegisterUser(email: String, password: String): Flow<Resource<Boolean>> = flow{
        try{
            emit(Resource.Loading())
            auth.createUserWithEmailAndPassword(email, password)
            emit(Resource.Success(true))
        }catch (e: Exception){
            emit(
                Resource.Error(e)
            )
        }
    }
}