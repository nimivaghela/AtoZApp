package com.app.atoz.ui.user.address

import androidx.lifecycle.MutableLiveData
import com.app.atoz.common.helper.CallbackWrapper
import com.app.atoz.models.ApiError
import com.app.atoz.models.AtoZResponseModel
import com.app.atoz.models.RequestState
import com.app.atoz.models.UserHolder
import com.app.atoz.models.address.AddressListItem
import com.app.atoz.models.address.AddressModel
import com.app.atoz.models.address.AddressRequestModel
import com.app.atoz.shareddata.base.BaseView
import com.app.atoz.shareddata.endpoint.ApiEndPoint
import com.app.atoz.utils.Config
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import javax.inject.Inject

class AddressRepo @Inject constructor(private val mAppEndPoint: ApiEndPoint, private val mUserHolder: UserHolder) {


    fun provideAddress(
        isInternetConnected: Boolean,
        baseView: BaseView,
        disposable: CompositeDisposable,
        callback: MutableLiveData<RequestState<AddressModel>>
    ) {
        if (!isInternetConnected) {
            callback.value = RequestState(error = ApiError(Config.NETWORK_ERROR, null, true))
        } else {

            mAppEndPoint.getAddress(mUserHolder.authToken)
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe { callback.value = RequestState(progress = true) }
                .subscribeWith(object : CallbackWrapper<AtoZResponseModel<AddressModel>>(baseView) {
                    override fun onApiError(e: Throwable?) {
                        callback.value = RequestState(progress = false)
                    }

                    override fun onApiSuccess(response: AtoZResponseModel<AddressModel>) {
                        callback.value = RequestState(progress = false, data = response)
                    }
                }).addTo(disposable)
        }
    }

    fun addAddress(
        isInternetConnected: Boolean,
        baseView: BaseView,
        disposable: CompositeDisposable,
        callback: MutableLiveData<RequestState<AddressListItem>>,
        addressRequestModel: AddressRequestModel
    ) {
        if (!isInternetConnected) {
            callback.value = RequestState(error = ApiError(Config.NETWORK_ERROR, null, true))
        } else {

            mAppEndPoint.addAddress(mUserHolder.authToken, addressRequestModel)
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe { callback.value = RequestState(progress = true) }
                .subscribeWith(object : CallbackWrapper<AtoZResponseModel<AddressListItem>>(baseView) {
                    override fun onApiError(e: Throwable?) {
                        callback.value = RequestState(progress = false)
                    }

                    override fun onApiSuccess(response: AtoZResponseModel<AddressListItem>) {
                        callback.value = RequestState(progress = false, data = response)
                    }
                }).addTo(disposable)
        }
    }

    fun deleteAddress(
        isInternetConnected: Boolean,
        baseView: BaseView,
        disposable: CompositeDisposable,
        callback: MutableLiveData<RequestState<AddressListItem>>,
        addressItem: AddressListItem
    ) {
        if (!isInternetConnected) {
            callback.value = RequestState(error = ApiError(Config.NETWORK_ERROR, null, true))
        } else {

            addressItem.id?.let {
                mAppEndPoint.deleteAddress(mUserHolder.authToken, it)
                    .observeOn(AndroidSchedulers.mainThread())
                    .doOnSubscribe { callback.value = RequestState(progress = true) }
                    .subscribeWith(object : CallbackWrapper<AtoZResponseModel<AddressListItem>>(baseView) {
                        override fun onApiError(e: Throwable?) {
                            callback.value = RequestState(progress = false)
                        }

                        override fun onApiSuccess(response: AtoZResponseModel<AddressListItem>) {
                            response.data = addressItem
                            callback.value = RequestState(progress = false, data = response)
                        }
                    }).addTo(disposable)
            }
        }
    }

}