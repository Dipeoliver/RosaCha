package com.clausfonseca.rosacha.domain.usecases.product

class ProductUseCases(
    val getUrlFile: FirebaseGetUrlProduct,
    val getStorageUrl: StorageGetUrlProduct,
    val getProducts: FirebaseGetProducts,
    val getMoreProducts: FirebaseGetMoreProducts,
    val insertProduct: FirebaseInsertProduct,
    val updateProduct: FirebaseUpdateProduct,
    val removeImage: FirebaseRemoveImageProduct,
    val removeProduct: FirebaseRemoveProduct,
    val filterSearchProducts: FirebaseFilterSearchProducts,

    )