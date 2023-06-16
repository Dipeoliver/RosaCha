package com.clausfonseca.rosacha.view.onboarding.login

import com.clausfonseca.rosacha.utils.SingleLiveEvent


class LoginModelState {

    val screenState = SingleLiveEvent<LoginState>()

    sealed class LoginState {
        data class Loading(val isLoading: Boolean) : LoginState()
        object Success : LoginState()
        data class Error(val message: String) : LoginState()
    }
}