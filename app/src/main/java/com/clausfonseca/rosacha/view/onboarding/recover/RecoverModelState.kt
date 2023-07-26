package com.clausfonseca.rosacha.view.onboarding.recover

import com.clausfonseca.rosacha.utils.SingleLiveEvent

class RecoverModelState {

    val screenState = SingleLiveEvent<RecoverState>()

    sealed class RecoverState {
        data class Loading(val isLoading: Boolean) : RecoverState()
        object Success : RecoverState()
        data class Error(val message: String) : RecoverState()
    }
}