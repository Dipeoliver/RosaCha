package com.clausfonseca.rosacha.view.dashboard.product.addProduct

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.clausfonseca.rosacha.domain.usecases.product.ProductUseCases
import com.clausfonseca.rosacha.model.ClientModel
import com.clausfonseca.rosacha.model.ProductModel
import com.clausfonseca.rosacha.utils.Resource
import com.clausfonseca.rosacha.view.common.CommonModelState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AddProductViewModel @Inject constructor(
    private val productUseCases: ProductUseCases
) : ViewModel() {
    val model = CommonModelState()

    fun getUrlFile(dbProduct: String, pictureName: String) {
        viewModelScope.launch {
            productUseCases.getUrlFile.invoke(dbProduct, pictureName).collect {
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
                        model.screenState.value = CommonModelState.CommonState.Success
                    }
                }
            }
        }
    }

    fun getUrlStorage (dbProduct: String, pictureName: String, bitmap: Bitmap){
        viewModelScope.launch {
            productUseCases.getStorageUrl.invoke(dbProduct,pictureName,bitmap).collect {
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

    fun insertProduct(dbProduct: String, productModel: ProductModel) {
        viewModelScope.launch {
            productUseCases.insertProduct.invoke(dbProduct,productModel).collect {
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
                        model.screenState.value = CommonModelState.CommonState.InsertProductSuccess(data = it.data ?: false)
                    }
                }
            }
        }
    }
}