package com.app.atoz.ui.auth.changepassword

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.app.atoz.models.RequestState
import com.app.atoz.models.UserHolder
import com.app.atoz.shareddata.base.BaseView
import com.app.atoz.shareddata.repo.ChangePasswordRepo
import com.google.gson.JsonObject
import io.reactivex.disposables.CompositeDisposable
import javax.inject.Inject

class ChangePasswordViewModel @Inject constructor(
    private val mChangePasswordRepo: ChangePasswordRepo,
    private val mUserHolder: UserHolder
) : ViewModel() {
    private val mLDChangePasswordRequest = MutableLiveData<RequestState<Any>>()

    fun getChangePasswordRequest(): LiveData<RequestState<Any>> = mLDChangePasswordRequest

    fun doChangePassword(
        currentPassword: String,
        newPassword: String,
        isInternetConnected: Boolean,
        baseView: BaseView,
        disposable: CompositeDisposable
    ) {
        val body = JsonObject()
        body.addProperty("current_password", currentPassword)
        body.addProperty("password", newPassword)
        body.addProperty("id", mUserHolder.userId)

        mChangePasswordRepo.changePassword(body, isInternetConnected, baseView, disposable, mLDChangePasswordRequest)
    }
}