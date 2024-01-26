package com.clausfonseca.rosacha.di

import com.clausfonseca.rosacha.data.repository.AuthRepositoryImpl
import com.clausfonseca.rosacha.data.repository.ClientRepositoryImpl
import com.clausfonseca.rosacha.domain.repository.AuthRepository
import com.clausfonseca.rosacha.domain.usecases.auth.AuthUseCases
import com.clausfonseca.rosacha.domain.usecases.auth.FirebaseRecoverPassword
import com.clausfonseca.rosacha.domain.usecases.auth.FirebaseRegisterUser
import com.clausfonseca.rosacha.domain.usecases.auth.FirebaseSignIn
import com.clausfonseca.rosacha.domain.usecases.auth.FirebaseSignOut
import com.clausfonseca.rosacha.domain.usecases.client.ClientUseCases
import com.clausfonseca.rosacha.domain.usecases.client.FirebaseGetUrl
import com.clausfonseca.rosacha.domain.usecases.client.StorageGetUrl
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
    fun provideFirebaseFireStore(): FirebaseFirestore {
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
        firebaseSignOut = FirebaseSignOut(repository = repository),
        firebaseRecoverPassword = FirebaseRecoverPassword(repository = repository),
        firebaseRegisterUser =  FirebaseRegisterUser(repository= repository)
    )

    @Singleton
    @Provides
    fun provideClientUseCases(repository: ClientRepositoryImpl) = ClientUseCases(
        getUrlFile = FirebaseGetUrl(repository = repository),
        getStorageUrl = StorageGetUrl(repository = repository),
    )

}