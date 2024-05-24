package com.clausfonseca.rosacha.view.common

import com.clausfonseca.rosacha.model.ClientModel
import com.clausfonseca.rosacha.utils.SingleLiveEvent

class CommonModelState {
    val screenState = SingleLiveEvent<CommonState>()

    var dataUrl = false

    var url = ""

    var clientsResult: MutableList<ClientModel> = mutableListOf()

    sealed class CommonState {
        data class Loading(val isLoading: Boolean) : CommonState()
        data class SuccessStorageUrl(val data: String) : CommonState()
        data class Error(val message: String) : CommonState()
        data class InsertClient(val data: Boolean) : CommonState()
        data class DeleteClientError(val message: String) : CommonState()
        data class InsertClientError(val message: String) : CommonState()
        data class UpdateClientError(val message: String) : CommonState()


        data object FilterClientSuccess : CommonState()
        data object ReinsertClientSuccess : CommonState()
        data object GetClientsLoaded : CommonState()
        data object RemoveClientSuccess : CommonState()
        data object RemoveImageSuccess : CommonState()
        data object Success : CommonState()

    }
}