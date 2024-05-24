package com.clausfonseca.rosacha.view.dashboard.client.listClient

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.clausfonseca.rosacha.domain.usecases.client.ClientUseCases
import com.clausfonseca.rosacha.model.ClientModel
import com.clausfonseca.rosacha.utils.Resource
import com.clausfonseca.rosacha.view.common.CommonModelState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ListClientViewModel @Inject constructor(
    private val clientUseCases: ClientUseCases
) : ViewModel() {
    val model = CommonModelState()

    fun removeClient(dbClient: String, clientModel: ClientModel) {
        viewModelScope.launch {
            clientUseCases.removeClient.invoke(dbClient, clientModel).collect {
                when (it) {
                    is Resource.Error -> {
                        model.screenState.value = CommonModelState.CommonState.Loading(false)
                        model.screenState.value =
                            CommonModelState.CommonState.DeleteClientError(it.exception?.message ?: "Unexpected error")
                    }

                    is Resource.Loading -> {
                        model.screenState.value = CommonModelState.CommonState.Loading(true)
                    }

                    is Resource.Success -> {
                        model.screenState.value = CommonModelState.CommonState.Loading(false)
                        model.screenState.value = CommonModelState.CommonState.RemoveClientSuccess
                    }
                }
            }
        }
    }

    fun removeImageFireStorage(dbClient: String, id: String) {
        viewModelScope.launch {
            clientUseCases.removeImage.invoke(dbClient, id).collect {
                when (it) {
                    is Resource.Error -> {
                        model.screenState.value = CommonModelState.CommonState.Loading(false)
                        model.screenState.value =
                            CommonModelState.CommonState.Error(it.exception?.message ?: "Error deleting image")
                    }

                    is Resource.Loading -> {
                    }

                    is Resource.Success -> {
                        model.screenState.value = CommonModelState.CommonState.Loading(false)
                        model.screenState.value = CommonModelState.CommonState.RemoveImageSuccess
                    }
                }
            }
        }
    }

    fun getClients(dbClient: String, clientList: MutableList<ClientModel>) {
        viewModelScope.launch {
            clientUseCases.getClients.invoke(dbClient, clientList).collect {
                when (it) {
                    is Resource.Error -> {
                        model.screenState.value = CommonModelState.CommonState.Loading(false)
                        model.screenState.value =
                            CommonModelState.CommonState.Error(it.exception?.message ?: "Error to loading clients")
                    }

                    is Resource.Loading -> {
                        model.screenState.value = CommonModelState.CommonState.Loading(true)
                    }

                    is Resource.Success -> {
                        model.clientsResult.clear()
                        model.clientsResult.addAll(it.data ?: mutableListOf())
                        model.screenState.value = CommonModelState.CommonState.Loading(false)
                        model.screenState.value = CommonModelState.CommonState.GetClientsLoaded
                    }
                }
            }
        }
    }

    fun insertClient(dbClient: String, clientModel: ClientModel) {
        viewModelScope.launch {
            clientUseCases.insertClient.invoke(dbClient, clientModel).collect {
                when (it) {
                    is Resource.Error -> {
                        model.screenState.value =
                            CommonModelState.CommonState.InsertClientError(it.exception?.message ?: "Unexpected error")
                    }

                    is Resource.Loading -> {
                    }

                    is Resource.Success -> {
                        model.dataUrl = it.data == true
                        model.screenState.value = CommonModelState.CommonState.ReinsertClientSuccess
                    }
                }
            }
        }
    }

    fun filterSearchClients(dbClient: String, fieldText: String, clientList: MutableList<ClientModel>) {
        viewModelScope.launch {
            clientUseCases.filterSearchClients.invoke(dbClient, fieldText, clientList).collect {
                when (it) {
                    is Resource.Error -> {
                        model.screenState.value =
                            CommonModelState.CommonState.InsertClientError(it.exception?.message ?: "Unexpected error")
                    }

                    is Resource.Loading -> {
                    }

                    is Resource.Success -> {
                        model.clientsResult.clear()
                        model.clientsResult.addAll(it.data ?: mutableListOf())
                        model.screenState.value = CommonModelState.CommonState.FilterClientSuccess
                    }
                }
            }
        }
    }
}