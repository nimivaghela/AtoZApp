package com.app.atoz.shareddata.repo

import androidx.lifecycle.MutableLiveData
import com.app.atoz.common.helper.CallbackWrapper
import com.app.atoz.models.*
import com.app.atoz.shareddata.base.BaseView
import com.app.atoz.shareddata.endpoint.ApiEndPoint
import com.app.atoz.utils.Config
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import okhttp3.MultipartBody
import javax.inject.Inject

class JobRepository @Inject constructor(private val mApiEndPoint: ApiEndPoint, private val mUserHolder: UserHolder) {

    fun getJobDetail(
        orderStatus: String,
        isInternetConnected: Boolean,
        baseView: BaseView,
        disposable: CompositeDisposable,
        callback: MutableLiveData<RequestState<OrderDetailModel>>
    ) {
        if (!isInternetConnected) {
            callback.value = RequestState(error = ApiError(Config.NETWORK_ERROR, null, true))
        } else {
            mApiEndPoint.getJobDetail(mUserHolder.authToken, orderStatus)
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe { callback.value = RequestState(progress = true) }
                .subscribeWith(object : CallbackWrapper<AtoZResponseModel<OrderDetailModel>>(baseView) {
                    override fun onApiSuccess(response: AtoZResponseModel<OrderDetailModel>) {
                        response.data.let {
                            callback.value = RequestState(progress = false, data = response)
                        }
                    }

                    override fun onApiError(e: Throwable?) {
                        callback.value = RequestState(progress = false)
                    }
                }).addTo(disposable)
        }
    }

    fun getChangeOrderStatus(
        requestChangeInput: RequestChangeInput,
        isInternetConnected: Boolean,
        baseView: BaseView,
        disposable: CompositeDisposable,
        callback: MutableLiveData<RequestState<Any>>
    ) {
        if (!isInternetConnected) {
            callback.value = RequestState(error = ApiError(Config.NETWORK_ERROR, null, true))
        } else {
            mApiEndPoint.changeRequestStatus(mUserHolder.authToken, requestChangeInput)
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe { callback.value = RequestState(progress = true) }
                .subscribeWith(object : CallbackWrapper<AtoZResponseModel<Any>>(baseView) {
                    override fun onApiSuccess(response: AtoZResponseModel<Any>) {
                        response.data.let {
                            callback.value = RequestState(progress = false, data = response)
                        }
                    }

                    override fun onApiError(e: Throwable?) {
                        callback.value = RequestState(progress = false)
                    }

                }).addTo(disposable)
        }
    }


    fun completeJob(
        multipartBody: MultipartBody,
        isInternetConnected: Boolean,
        baseView: BaseView,
        disposable: CompositeDisposable,
        callback: MutableLiveData<RequestState<Any>>
    ) {
        if (!isInternetConnected) {
            callback.value = RequestState(error = ApiError(Config.NETWORK_ERROR, null, true))
        } else {
            mApiEndPoint.completeJob(mUserHolder.authToken, multipartBody)
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe { callback.value = RequestState(progress = true) }
                .subscribeWith(object : CallbackWrapper<AtoZResponseModel<Any>>(baseView) {
                    override fun onApiSuccess(response: AtoZResponseModel<Any>) {
                        response.data.let {
                            callback.value = RequestState(progress = false, data = response)
                        }
                    }

                    override fun onApiError(e: Throwable?) {
                        callback.value = RequestState(progress = false)
                    }

                }).addTo(disposable)
        }
    }

}