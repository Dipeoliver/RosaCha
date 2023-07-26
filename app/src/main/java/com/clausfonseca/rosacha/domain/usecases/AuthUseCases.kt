package com.clausfonseca.rosacha.domain.usecases

data class AuthUseCases(
    val firebaseSignIn: FirebaseSignIn,
    val firebaseSignOut: FirebaseSignOut,
    val firebaseRecoverPassword: FirebaseRecoverPassword
)
