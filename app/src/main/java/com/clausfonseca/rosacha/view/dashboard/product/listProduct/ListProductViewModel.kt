package com.clausfonseca.rosacha.view.dashboard.product.listProduct

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.clausfonseca.rosacha.domain.usecases.product.ProductUseCases
import com.clausfonseca.rosacha.model.ProductModel
import com.clausfonseca.rosacha.utils.Resource
import com.clausfonseca.rosacha.view.common.CommonModelState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ListProductViewModel @Inject constructor(
    private val productUseCases: ProductUseCases
) : ViewModel() {
    val model = CommonModelState()

    fun removeProduct(dbProduct: String, productModel: ProductModel, position: Int) {
        viewModelScope.launch {
            productUseCases.removeProduct.invoke(dbProduct, productModel).collect {
                when (it) {
                    is Resource.Error -> {
                        model.screenState.value = CommonModelState.CommonState.Loading(false)
                        model.screenState.value =
                            CommonModelState.CommonState.DeleteProductError(it.exception?.message ?: "Unexpected error")
                    }

                    is Resource.Loading -> {
                        model.screenState.value = CommonModelState.CommonState.Loading(true)
                    }

                    is Resource.Success -> {
                        model.screenState.value = CommonModelState.CommonState.Loading(false)
                        model.screenState.value = CommonModelState.CommonState.RemoveProductSuccess(position)
                    }
                }
            }
        }
    }

    fun removeImageFireStorage(dbProduct: String, id: String) {
        viewModelScope.launch {
            productUseCases.removeImage.invoke(dbProduct, id).collect {
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

    fun getProducts(dbProduct: String, productList: MutableList<ProductModel>) {
        viewModelScope.launch {
            productUseCases.getProducts.invoke(dbProduct, productList).collect {
                when (it) {
                    is Resource.Error -> {
                        model.screenState.value = CommonModelState.CommonState.Loading(false)
                        model.screenState.value =
                            CommonModelState.CommonState.Error(it.exception?.message ?: "Error to loading products")
                    }

                    is Resource.Loading -> {
                        model.screenState.value = CommonModelState.CommonState.Loading(true)
                    }

                    is Resource.Success -> {
                        model.productsResult.clear()
                        model.productsResult.addAll(it.data ?: mutableListOf())
                        model.screenState.value = CommonModelState.CommonState.Loading(false)
                        model.screenState.value = CommonModelState.CommonState.GetProductsLoaded
                    }
                }
            }
        }
    }

    fun insertProduct(dbProduct: String, productModel: ProductModel) {
        viewModelScope.launch {
            productUseCases.insertProduct.invoke(dbProduct, productModel).collect {
                when (it) {
                    is Resource.Error -> {
                        model.screenState.value =
                            CommonModelState.CommonState.InsertProductError(it.exception?.message ?: "Unexpected error")
                    }

                    is Resource.Loading -> {
                    }

                    is Resource.Success -> {
                        model.dataUrl = it.data == true
                        model.screenState.value = CommonModelState.CommonState.ReinsertProductSuccess
                    }
                }
            }
        }
    }

    fun filterSearchProducts(dbProduct: String, fieldText: String, productList: MutableList<ProductModel>) {
        viewModelScope.launch {
            productUseCases.filterSearchProducts.invoke(dbProduct, fieldText, productList).collect {
                when (it) {
                    is Resource.Error -> {
                        model.screenState.value =
                            CommonModelState.CommonState.InsertProductError(it.exception?.message ?: "Unexpected error")
                    }

                    is Resource.Loading -> {
                    }

                    is Resource.Success -> {
                        model.productsResult.clear()
                        model.productsResult.addAll(it.data ?: mutableListOf())
                        model.screenState.value = CommonModelState.CommonState.FilterProductSuccess
                    }
                }
            }
        }
    }
}