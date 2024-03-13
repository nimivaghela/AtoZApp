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
import okhttp3.MultipartBody
import javax.inject.Inject

class ExistServiceRepo @Inject constructor(
    private val mApiEndPoint: ApiEndPoint,
    private val mUserHolder: UserHolder
) {

    fun getProvidersExistService(
        bodyData: JsonObject,
        isInternetConnected: Boolean,
        baseView: BaseView,
        disposable: CompositeDisposable,
        callback: MutableLiveData<RequestState<CategorySelectionResponseModel>>
    ) {

        if (!isInternetConnected) {
            callback.value = RequestState(error = ApiError(Config.NETWORK_ERROR, null, true))
        } else {
            mApiEndPoint.getProvidersExistServices(mUserHolder.authToken, bodyData)
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe { callback.value = RequestState(progress = true) }
                .subscribeWith(object : CallbackWrapper<AtoZResponseModel<CategorySelectionResponseModel>>(baseView) {
                    override fun onApiSuccess(response: AtoZResponseModel<CategorySelectionResponseModel>) {
                        callback.value = RequestState(progress = false, data = response)
                    }

                    override fun onApiError(e: Throwable?) {
                        callback.value = RequestState(progress = false)
                    }
                }).addTo(disposable)
        }
    }

    fun addDeleteProviderServices(
        bodyData: MultipartBody, isInternetConnected: Boolean, baseView: BaseView,
        disposable: CompositeDisposable, callback: MutableLiveData<RequestState<Any>>
    ) {
        if (!isInternetConnected) {
            callback.value = RequestState(error = ApiError(Config.NETWORK_ERROR, null, true))
        } else {
            mApiEndPoint.addDeleteService(mUserHolder.authToken, bodyData)
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe { callback.value = RequestState(progress = true) }
                .subscribeWith(object :
                    CallbackWrapper<AtoZResponseModel<Any>>(baseView) {
                    override fun onApiSuccess(response: AtoZResponseModel<Any>) {
                        callback.value = RequestState(progress = false, data = response)
                    }

                    override fun onApiError(e: Throwable?) {
                        callback.value = RequestState(progress = false)
                    }
                }).addTo(disposable)
        }
    }

    fun getChildServices(
        bodyData: JsonObject, isInternetConnected: Boolean, baseView: BaseView, disposable: CompositeDisposable,
        callback: MutableLiveData<RequestState<CategorySelectionResponseModel>>
    ) {
        if (!isInternetConnected) {
            callback.value = RequestState(error = ApiError(Config.NETWORK_ERROR, null, true))
        } else {
            mApiEndPoint.getChildServices(mUserHolder.authToken, bodyData)
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe { callback.value = RequestState(progress = true) }
                .subscribeWith(object : CallbackWrapper<AtoZResponseModel<CategorySelectionResponseModel>>(baseView) {
                    override fun onApiSuccess(response: AtoZResponseModel<CategorySelectionResponseModel>) {
                        callback.value = RequestState(progress = false, data = response)
                    }

                    override fun onApiError(e: Throwable?) {
                        callback.value = RequestState(progress = false)
                    }
                }).addTo(disposable)
        }
    }

    fun sendProviderPriceChangeRequest(
        bodyData: JsonObject, isInternetConnected: Boolean, baseView: BaseView, disposable: CompositeDisposable,
        callback: MutableLiveData<RequestState<Any>>
    ) {
        if (!isInternetConnected) {
            callback.value = RequestState(error = ApiError(Config.NETWORK_ERROR, null, true))
        } else {
            mApiEndPoint.providerPriceChangeRequest(mUserHolder.authToken, bodyData)
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe { callback.value = RequestState(progress = true) }
                .subscribeWith(object : CallbackWrapper<AtoZResponseModel<Any>>(baseView) {
                    override fun onApiSuccess(response: AtoZResponseModel<Any>) {
                        callback.value = RequestState(progress = true, data = response)
                    }

                    override fun onApiError(e: Throwable?) {
                        callback.value = RequestState(progress = false)
                    }
                }).addTo(disposable)
        }
    }
}