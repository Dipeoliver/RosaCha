package com.clausfonseca.rosacha.di

import com.clausfonseca.rosacha.data.repository.AuthRepositoryImpl
import com.clausfonseca.rosacha.domain.repository.AuthRepository
import com.clausfonseca.rosacha.domain.usecases.AuthUseCases
import com.clausfonseca.rosacha.domain.usecases.FirebaseSignIn
import com.clausfonseca.rosacha.domain.usecases.FirebaseSignOut
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RosaChaModule {
    @Singleton
    @Provides
    fun provideFirebaseAuthentication(): FirebaseAuth {
        return FirebaseAuth.getInstance()
    }

    @Singleton
    @Provides
    fun provideFirebaseFirestore(): FirebaseFirestore {
        return FirebaseFirestore.getInstance()
    }

    @Singleton
    @Provides
    fun provideFirebaseFirebaseStorage(): FirebaseStorage {
        return FirebaseStorage.getInstance()
    }



    @Singleton
    @Provides
    fun providesAuthenticationRepository(auth: FirebaseAuth): AuthRepository {
        return AuthRepositoryImpl(auth = auth)
    }

    @Singleton
    @Provides
    fun provideAuthUseCases(repository: AuthRepositoryImpl) = AuthUseCases(
        firebaseSignIn = FirebaseSignIn(repository = repository),
        firebaseSignOut = FirebaseSignOut(repository = repository)
    )
}