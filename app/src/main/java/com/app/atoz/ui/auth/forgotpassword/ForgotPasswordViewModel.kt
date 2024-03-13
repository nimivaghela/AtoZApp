package com.app.atoz.ui.auth.forgotpassword

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.app.atoz.models.RequestState
import com.app.atoz.shareddata.base.BaseView
import com.app.atoz.shareddata.repo.ForgotPasswordRepo
import com.google.gson.JsonObject
import io.reactivex.disposables.CompositeDisposable
import javax.inject.Inject

class ForgotPasswordViewModel @Inject constructor(private val mForgotPasswordRepo: ForgotPasswordRepo) : ViewModel() {

    private val mLDForgotPasswordRequest = MutableLiveData<RequestState<Any>>()

    fun getForgotPasswordRequest() = mLDForgotPasswordRequest

    fun doForgotPassword(
        isInternetConnected: Boolean,
        baseView: BaseView,
        disposable: CompositeDisposable,
        email: String
    ) {
        val forgotPasswordJson = JsonObject()
        forgotPasswordJson.addProperty("email", email)

        mForgotPasswordRepo.forgotPassword(
            forgotPasswordJson,
            isInternetConnected,
            baseView,
            disposable,
            mLDForgotPasswordRequest
        )
    }
}