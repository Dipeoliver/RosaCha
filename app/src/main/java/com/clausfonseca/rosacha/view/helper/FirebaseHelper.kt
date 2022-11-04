package com.clausfonseca.rosacha.view.helper


import com.clausfonseca.rosacha.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class FirebaseHelper {
    companion object {

        // retornar a referencia do banco de dados
        fun getDatabase() = FirebaseDatabase.getInstance().reference

        // recuperar a instancia de autenticacao do usuario
        private fun getAuth() = FirebaseAuth.getInstance()

        // recuperar o Id do usuario que esta logado
        fun getIdUser() = getAuth().uid

        //verificar se o usuario esta autenticado no app
        fun isAutenticated() = getAuth().currentUser != null

        // pegar a msg de erro que vem do firebase
        fun validError(error: String): Int {
            return when {
                error.contains("There is no user record corresponding to this identifier") -> {
                    R.string.account_not_registered_register_fragment
                }error.contains("The email address is badly formatted") -> {
                    R.string.invalid_email_register_fragment
                }error.contains("The password is invalid or the user does not have a password") -> {
                    R.string.invalid_password_register_fragment
                }error.contains("The email address is already in use by another account") -> {
                    R.string.email_in_use_register_fragment
                }error.contains("The given password is invalid") -> {
                    R.string.strong_password_register_fragment
                }
                else -> {
                    R.string.error_generic
                }
            }
        }
    }
}