package com.clausfonseca.rosacha.di

import com.clausfonseca.rosacha.data.repository.AuthRepositoryImpl
import com.clausfonseca.rosacha.data.repository.ClientRepositoryImpl
import com.clausfonseca.rosacha.data.repository.ProductRepositoryImpl
import com.clausfonseca.rosacha.domain.repository.AuthRepository
import com.clausfonseca.rosacha.domain.usecases.auth.AuthUseCases
import com.clausfonseca.rosacha.domain.usecases.auth.FirebaseRecoverPassword
import com.clausfonseca.rosacha.domain.usecases.auth.FirebaseRegisterUser
import com.clausfonseca.rosacha.domain.usecases.auth.FirebaseSignIn
import com.clausfonseca.rosacha.domain.usecases.auth.FirebaseSignOut
import com.clausfonseca.rosacha.domain.usecases.client.ClientUseCases
import com.clausfonseca.rosacha.domain.usecases.client.FirebaseFilterSearchClients
import com.clausfonseca.rosacha.domain.usecases.client.FirebaseGetClients
import com.clausfonseca.rosacha.domain.usecases.client.FirebaseGetMoreClients
import com.clausfonseca.rosacha.domain.usecases.client.FirebaseGetUrlClient
import com.clausfonseca.rosacha.domain.usecases.client.FirebaseInsertClient
import com.clausfonseca.rosacha.domain.usecases.client.FirebaseRemoveClient
import com.clausfonseca.rosacha.domain.usecases.client.FirebaseRemoveImageClient
import com.clausfonseca.rosacha.domain.usecases.client.FirebaseUpdateClient
import com.clausfonseca.rosacha.domain.usecases.client.StorageGetUrlClient
import com.clausfonseca.rosacha.domain.usecases.product.FirebaseFilterSearchProducts
import com.clausfonseca.rosacha.domain.usecases.product.FirebaseGetMoreProducts
import com.clausfonseca.rosacha.domain.usecases.product.FirebaseGetProducts
import com.clausfonseca.rosacha.domain.usecases.product.FirebaseGetUrlProduct
import com.clausfonseca.rosacha.domain.usecases.product.FirebaseInsertProduct
import com.clausfonseca.rosacha.domain.usecases.product.FirebaseRemoveImageProduct
import com.clausfonseca.rosacha.domain.usecases.product.FirebaseRemoveProduct
import com.clausfonseca.rosacha.domain.usecases.product.FirebaseUpdateProduct
import com.clausfonseca.rosacha.domain.usecases.product.ProductUseCases
import com.clausfonseca.rosacha.domain.usecases.product.StorageGetUrlProduct
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
        firebaseRegisterUser = FirebaseRegisterUser(repository = repository)
    )

    @Singleton
    @Provides
    fun provideClientUseCases(repository: ClientRepositoryImpl) = ClientUseCases(
        getUrlFile = FirebaseGetUrlClient(repository = repository),
        getStorageUrl = StorageGetUrlClient(repository = repository),
        getClients = FirebaseGetClients(repository = repository),
        getMoreClients = FirebaseGetMoreClients(repository = repository),
        insertClient = FirebaseInsertClient(repository = repository),
        updateClient = FirebaseUpdateClient(repository = repository),
        removeImage = FirebaseRemoveImageClient(repository = repository),
        removeClient = FirebaseRemoveClient(repository = repository),
        filterSearchClients = FirebaseFilterSearchClients(repository = repository)
    )

    @Singleton
    @Provides
    fun provideProductUseCases(repository: ProductRepositoryImpl) = ProductUseCases(
        getUrlFile = FirebaseGetUrlProduct(repository = repository),
        getStorageUrl = StorageGetUrlProduct(repository = repository),
        getProducts = FirebaseGetProducts(repository = repository),
        getMoreProducts = FirebaseGetMoreProducts(repository = repository),
        insertProduct = FirebaseInsertProduct(repository = repository),
        updateProduct = FirebaseUpdateProduct(repository = repository),
        removeImage = FirebaseRemoveImageProduct(repository = repository),
        removeProduct = FirebaseRemoveProduct(repository = repository),
        filterSearchProducts = FirebaseFilterSearchProducts(repository = repository)
    )
}