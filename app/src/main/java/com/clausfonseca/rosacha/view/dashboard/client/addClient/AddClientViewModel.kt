package com.clausfonseca.rosacha.view.dashboard.client.addClient

import android.graphics.Bitmap
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
class AddClientViewModel @Inject constructor(
    private val clientUseCases: ClientUseCases
) : ViewModel() {
    val model = CommonModelState()

    fun getFileUrl(fileUrl: String) {
        viewModelScope.launch {
            clientUseCases.getUrlFile.invoke(fileUrl).collect {
                when (it) {
                    is Resource.Error -> {
                        model.screenState.value = CommonModelState.CommonState.Loading(false)
                        model.screenState.value = CommonModelState.CommonState.Error(it.exception?.message ?: "Unexpected error")
                    }

                    is Resource.Loading -> {
                        model.screenState.value = CommonModelState.CommonState.Loading(true)
                    }

                    is Resource.Success -> { it
                      model.dataUrl = it.data == true
//                        model.screenState.value = CommonModelState.CommonState.Loading(false)
                        model.screenState.value = CommonModelState.CommonState.Success
                    }
                }
            }
        }
    }

    fun getUrlStorage (dbClient: String, pictureName: String, bitmap: Bitmap){
        viewModelScope.launch {
            clientUseCases.getStorageUrl.invoke(dbClient,pictureName,bitmap).collect {
                when (it) {
                    is Resource.Error -> {
                        model.screenState.value = CommonModelState.CommonState.Loading(false)
                        model.screenState.value = CommonModelState.CommonState.Error(it.exception?.message ?: "Unexpected error")
                    }

                    is Resource.Loading -> {
//                        model.screenState.value = CommonModelState.CommonState.Loading(true)
                    }

                    is Resource.Success -> { it
                        model.url = it.data?: ""
//                        model.screenState.value = CommonModelState.CommonState.Loading(false)
                        model.screenState.value = CommonModelState.CommonState.SuccessStorageUrl(it.data?:"")
                    }
                }
            }
        }
    }

    fun insertClient(dbClient: String, clientModel: ClientModel) {
        viewModelScope.launch {
            clientUseCases.insertClient.invoke(dbClient,clientModel).collect {
                when (it) {
                    is Resource.Error -> {
                        model.screenState.value = CommonModelState.CommonState.Loading(false)
                        model.screenState.value = CommonModelState.CommonState.Error(it.exception?.message ?: "Unexpected error")
                    }

                    is Resource.Loading -> {
//                        model.screenState.value = CommonModelState.CommonState.Loading(true)
                        // As linhas comentadas são porque segue um fluxo de acesso ao banco e não recriar o loading mais de
//                        uma vez
                    }

                    is Resource.Success -> { it
                        model.dataUrl = it.data == true
                        model.screenState.value = CommonModelState.CommonState.Loading(false)
                        model.screenState.value = CommonModelState.CommonState.InsertClient(data = it.data ?: false)
                    }
                }
            }
        }
    }

}














