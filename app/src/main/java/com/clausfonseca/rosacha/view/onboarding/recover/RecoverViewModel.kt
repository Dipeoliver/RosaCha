package com.clausfonseca.rosacha.view.onboarding.recover

import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.clausfonseca.rosacha.data.firebase.FirebaseHelper
import com.clausfonseca.rosacha.domain.usecases.AuthUseCases
import com.clausfonseca.rosacha.utils.Resource
import com.clausfonseca.rosacha.view.onboarding.login.LoginModelState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RecoverViewModel  @Inject constructor(
    private val authUseCases: AuthUseCases
) : ViewModel() {

    val model = RecoverModelState()

    fun recoverPassword(email: String){
        viewModelScope.launch {
            authUseCases.firebaseRecoverPassword.invoke(email).collect {

                when (it) {
                    is Resource.Error ->{
                        model.screenState.value = RecoverModelState.RecoverState.Loading(false)
                        model.screenState.value = RecoverModelState.RecoverState.Error(it.exception?.message ?: "Unexpected error")
                    }
                    is Resource.Loading -> {
                        model.screenState.value = RecoverModelState.RecoverState.Loading(true)
                    }
                    is Resource.Success -> {
                        model.screenState.value = RecoverModelState.RecoverState.Loading(false)
                        model.screenState.value = RecoverModelState.RecoverState.Success
                    }
                }
            }
        }
    }
}