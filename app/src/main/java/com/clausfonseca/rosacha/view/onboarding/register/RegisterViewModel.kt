package com.clausfonseca.rosacha.view.onboarding.register

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.clausfonseca.rosacha.domain.usecases.auth.AuthUseCases
import com.clausfonseca.rosacha.utils.Resource
import com.clausfonseca.rosacha.view.onboarding.CommonModelState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val authUseCases: AuthUseCases
) : ViewModel() {

    val model = CommonModelState()
    fun registerUser(email: String, password: String){
        viewModelScope.launch {
            authUseCases.firebaseRegisterUser.invoke(email,password).collect {

                when (it) {
                    is Resource.Error ->{
                        model.screenState.value = CommonModelState.CommonState.Loading(false)
                        model.screenState.value = CommonModelState.CommonState.Error(it.exception?.message ?: "Unexpected error")
                    }
                    is Resource.Loading -> {
                        model.screenState.value = CommonModelState.CommonState.Loading(true)
                    }
                    is Resource.Success -> {
                        model.screenState.value = CommonModelState.CommonState.Loading(false)
                        model.screenState.value = CommonModelState.CommonState.Success
                    }
                }
            }
        }
    }
}