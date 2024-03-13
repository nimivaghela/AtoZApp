package com.app.atoz.shareddata.repo

import androidx.lifecycle.MutableLiveData
import com.app.atoz.common.helper.CallbackWrapper
import com.app.atoz.models.*
import com.app.atoz.shareddata.base.BaseView
import com.app.atoz.shareddata.endpoint.ApiEndPoint
import com.app.atoz.utils.Config
import com.google.gson.JsonObject
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import timber.log.Timber
import javax.inject.Inject

class VerificationRepo @Inject constructor(private val mApiEndPoint: ApiEndPoint, private val mUserHolder: UserHolder) {
    fun verification(
        body: JsonObject,
        isInternetConnected: Boolean,
        baseView: BaseView,
        disposable: CompositeDisposable,
        callback: MutableLiveData<RequestState<VerificationResponse>>
    ) {
        if (!isInternetConnected) {
            callback.value = RequestState(error = ApiError(Config.NETWORK_ERROR, null, true))
        } else {

            mApiEndPoint.verifyPhone(mUserHolder.authToken, body)
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe { callback.value = RequestState(progress = true) }
                .subscribeWith(object : CallbackWrapper<AtoZResponseModel<VerificationResponse>>(baseView) {
                    override fun onApiError(e: Throwable?) {
                        callback.value = RequestState(progress = false)
                    }

                    override fun onApiSuccess(response: AtoZResponseModel<VerificationResponse>) {
                        Timber.d("APi Call success for verification")
                        response.data?.let {
                            /**
                             * store user data in shared preference
                             */
                            mUserHolder.isVerified = response.data?.isVerified ?: true
                        }
                        callback.value = RequestState(progress = false, data = response)
                    }
                }).addTo(disposable)
        }
    }
}