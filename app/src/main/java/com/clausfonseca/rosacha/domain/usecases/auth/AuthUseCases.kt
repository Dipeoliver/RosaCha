package com.clausfonseca.rosacha.domain.usecases.auth

data class AuthUseCases(
    val firebaseSignIn: FirebaseSignIn,
    val firebaseSignOut: FirebaseSignOut,
    val firebaseRecoverPassword: FirebaseRecoverPassword,
    val firebaseRegisterUser: FirebaseRegisterUser
)
