package com.app.atoz.ui.provider.home

import androidx.lifecycle.MutableLiveData
import com.app.atoz.common.helper.CallbackWrapper
import com.app.atoz.models.ApiError
import com.app.atoz.models.AtoZResponseModel
import com.app.atoz.models.RequestState
import com.app.atoz.models.UserHolder
import com.app.atoz.models.providerhome.ProviderHomeModel
import com.app.atoz.shareddata.base.BaseView
import com.app.atoz.shareddata.endpoint.ApiEndPoint
import com.app.atoz.utils.Config
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import javax.inject.Inject

class ProviderRequestRepo @Inject constructor(
    private val mAppEndPoint: ApiEndPoint,
    private val mUserHolder: UserHolder
) {


    fun provideRequestData(
        isInternetConnected: Boolean,
        baseView: BaseView,
        disposable: CompositeDisposable,
        callback: MutableLiveData<RequestState<ProviderHomeModel>>
    ) {
        if (!isInternetConnected) {
            callback.value = RequestState(error = ApiError(Config.NETWORK_ERROR, null, true))
        } else {

            mAppEndPoint.providerRequest(mUserHolder.authToken)
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe { callback.value = RequestState(progress = true) }
                .subscribeWith(object : CallbackWrapper<AtoZResponseModel<ProviderHomeModel>>(baseView) {
                    override fun onApiError(e: Throwable?) {
                        callback.value = RequestState(progress = false)
                    }

                    override fun onApiSuccess(response: AtoZResponseModel<ProviderHomeModel>) {
                        callback.value = RequestState(progress = false, data = response)
                    }
                }).addTo(disposable)
        }
    }

}