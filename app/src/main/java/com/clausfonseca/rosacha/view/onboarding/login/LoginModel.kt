package com.clausfonseca.rosacha.view.onboarding.login

import androidx.lifecycle.MutableLiveData
import com.clausfonseca.rosacha.utils.Response
import com.clausfonseca.rosacha.utils.SingleLiveEvent


class LoginModel {

    val screenState = SingleLiveEvent<LoginState>()

    sealed class LoginState {
        object Loading : LoginState()
        object Success : LoginState()
        data class Error(val message: String) : LoginState()
    }
}