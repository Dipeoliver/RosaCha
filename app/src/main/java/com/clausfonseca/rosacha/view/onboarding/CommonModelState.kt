package com.clausfonseca.rosacha.view.onboarding

import com.clausfonseca.rosacha.utils.SingleLiveEvent

class CommonModelState {
    val screenState = SingleLiveEvent<CommonState>()

    var dataUrl = false

    var url =""

    sealed class CommonState {
        data class Loading(val isLoading: Boolean) : CommonState()
        object Success : CommonState()

        data class SuccessStorageUrl(val data: String): CommonState()
        data class Error(val message: String) : CommonState()


    }
}