package com.clausfonseca.rosacha.view.dashboard.client.listClient

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.clausfonseca.rosacha.domain.usecases.client.ClientUseCases
import com.clausfonseca.rosacha.model.ClientModel
import com.clausfonseca.rosacha.utils.Resource
import com.clausfonseca.rosacha.view.onboarding.CommonModelState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ListClientViewModel @Inject constructor(
    private val clientUseCases: ClientUseCases
) : ViewModel() {
    val model = CommonModelState()

    fun removeClient(dbClient: String, clientModel: ClientModel){
        viewModelScope.launch {
            clientUseCases.removeClient.invoke(dbClient,clientModel).collect {
                when (it) {
                    is Resource.Error -> {
                        model.screenState.value = CommonModelState.CommonState.Loading(false)
                        model.screenState.value = CommonModelState.CommonState.Error(it.exception?.message ?: "Unexpected error")
                    }

                    is Resource.Loading -> {
                        model.screenState.value = CommonModelState.CommonState.Loading(true)
                    }

                    is Resource.Success -> { it
//                        model.screenState.value = CommonModelState.CommonState.Loading(false)
                        model.screenState.value = CommonModelState.CommonState.RemoveClientSuccess
                    }
                }
            }
        }
    }

}