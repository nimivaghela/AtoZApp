package com.app.atoz.shareddata.repo

import androidx.lifecycle.MutableLiveData
import com.app.atoz.AppApplication
import com.app.atoz.R
import com.app.atoz.common.helper.CallbackWrapper
import com.app.atoz.models.*
import com.app.atoz.shareddata.base.BaseView
import com.app.atoz.shareddata.endpoint.ApiEndPoint
import com.app.atoz.utils.Config
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import javax.inject.Inject

class ContentPageRepo @Inject constructor(private val mAppEndPoint: ApiEndPoint) {

    fun getContentPage(
        pageName: String,
        isInternetConnected: Boolean,
        baseView: BaseView,
        disposable: CompositeDisposable,
        callback: MutableLiveData<RequestState<String>>
    ) {
        if (!isInternetConnected) {
            callback.value = RequestState(error = ApiError(Config.NETWORK_ERROR, null, true))
        } else {
            mAppEndPoint.getContentPage(pageName)
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe { callback.value = RequestState(progress = true) }
                .subscribeWith(object : CallbackWrapper<AtoZResponseModel<String>>(baseView) {
                    override fun onApiError(e: Throwable?) {
                        callback.value = RequestState(progress = false)
                    }

                    override fun onApiSuccess(response: AtoZResponseModel<String>) {
                        response.data?.let {
                            callback.value = RequestState(progress = false, data = response)
                        }

                    }
                }).addTo(disposable)
        }
    }

}