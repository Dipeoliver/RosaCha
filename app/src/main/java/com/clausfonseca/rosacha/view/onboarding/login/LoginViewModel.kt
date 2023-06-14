package com.clausfonseca.rosacha.view.onboarding.login

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.clausfonseca.rosacha.domain.usecases.AuthUseCases
import com.clausfonseca.rosacha.utils.Response
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authUseCases: AuthUseCases
) : ViewModel() {

    val screenState =  MutableLiveData<Response<Boolean>>()

//    var screenState: State<Response<Nothing>> = _screenState


    fun signIn (email: String, password: String){
        viewModelScope.launch {
            authUseCases.firebaseSignIn.invoke(email,password).collect{
                screenState.postValue(it)
            }
        }
    }
}