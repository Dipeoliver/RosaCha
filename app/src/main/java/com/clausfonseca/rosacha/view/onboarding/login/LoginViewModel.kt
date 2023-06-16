package com.clausfonseca.rosacha.view.onboarding.login

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.clausfonseca.rosacha.domain.usecases.AuthUseCases
import com.clausfonseca.rosacha.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authUseCases: AuthUseCases
) : ViewModel() {

    val model = LoginModelState()

//    var screenState: State<Response<Nothing>> = _screenState


    fun signIn (email: String, password: String){
        viewModelScope.launch {
            authUseCases.firebaseSignIn.invoke(email,password).collect {
                when (it) {
                    is Resource.Error ->{
                        model.screenState.value = LoginModelState.LoginState.Loading(false)
                        model.screenState.value = LoginModelState.LoginState.Error(it.exception?.message ?: "Unexpected error")

                    }
                    is Resource.Loading -> {
                        model.screenState.value = LoginModelState.LoginState.Loading(true)
                    }
                    is Resource.Success -> {
                        model.screenState.value = LoginModelState.LoginState.Loading(false)
                        model.screenState.value = LoginModelState.LoginState.Success
                    }
                }
            }
        }
    }
}