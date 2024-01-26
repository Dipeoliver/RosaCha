package com.clausfonseca.rosacha.view.onboarding.recover

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.clausfonseca.rosacha.domain.usecases.auth.AuthUseCases
import com.clausfonseca.rosacha.utils.Resource
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

                    else -> {}
                }
            }
        }
    }
}