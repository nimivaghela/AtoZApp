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

class MyOrderRepo @Inject constructor(private val mApiEndPoint: ApiEndPoint, private val mUserHolder: UserHolder) {


    fun getUserOrderStatusList(
        orderStatus: String,
        isInternetConnected: Boolean,
        baseView: BaseView,
        disposable: CompositeDisposable,
        callback: MutableLiveData<RequestState<UserServiceStatusList>>
    ) {
        if (!isInternetConnected) {
            callback.value = RequestState(error = ApiError(Config.NETWORK_ERROR, null, true))
        } else {
            mApiEndPoint.getServiceList(mUserHolder.authToken, orderStatus)
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe { callback.value = RequestState(progress = true) }
                .subscribeWith(object : CallbackWrapper<AtoZResponseModel<UserServiceStatusList>>(baseView) {
                    override fun onApiSuccess(response: AtoZResponseModel<UserServiceStatusList>) {
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

    fun getUserOrderDetail(
        orderId: String,
        isInternetConnected: Boolean,
        baseView: BaseView,
        disposable: CompositeDisposable,
        callback: MutableLiveData<RequestState<OrderDetailModel>>
    ) {
        if (!isInternetConnected) {
            callback.value = RequestState(error = ApiError(Config.NETWORK_ERROR, null, true))
        } else {
            mApiEndPoint.getOrderDetail(mUserHolder.authToken, orderId)
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


    fun getProviderOrderStatusList(
        orderStatus: String,
        isInternetConnected: Boolean,
        baseView: BaseView,
        disposable: CompositeDisposable,
        callback: MutableLiveData<RequestState<UserServiceStatusList>>
    ) {
        if (!isInternetConnected) {
            callback.value = RequestState(error = ApiError(Config.NETWORK_ERROR, null, true))
        } else {
            mApiEndPoint.getProviderServiceList(mUserHolder.authToken, orderStatus)
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe { callback.value = RequestState(progress = true) }
                .subscribeWith(object : CallbackWrapper<AtoZResponseModel<UserServiceStatusList>>(baseView) {
                    override fun onApiSuccess(response: AtoZResponseModel<UserServiceStatusList>) {
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

    fun editOrderDetails(
        bodyData: JsonObject,
        isInternetConnected: Boolean,
        baseView: BaseView,
        disposable: CompositeDisposable,
        callback: MutableLiveData<RequestState<Any>>
    ) {
        if (!isInternetConnected) {
            callback.value = RequestState(error = ApiError(Config.NETWORK_ERROR, null, true))
        } else {
            mApiEndPoint.editOrderDetails(mUserHolder.authToken, bodyData)
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

    fun uploadBill(
        multipartBody: MultipartBody,
        isInternetConnected: Boolean,
        baseView: BaseView,
        disposable: CompositeDisposable,
        callback: MutableLiveData<RequestState<List<OrderDetailModel.ServiceRequest.ServiceRequestImage>>>
    ) {
        if (!isInternetConnected) {
            callback.value = RequestState(error = ApiError(Config.NETWORK_ERROR, null, true))
        } else {
            mApiEndPoint.uploadBill(mUserHolder.authToken, multipartBody)
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe { callback.value = RequestState(progress = true) }
                .subscribeWith(object :
                    CallbackWrapper<AtoZResponseModel<List<OrderDetailModel.ServiceRequest.ServiceRequestImage>>>(
                        baseView
                    ) {
                    override fun onApiSuccess(response: AtoZResponseModel<List<OrderDetailModel.ServiceRequest.ServiceRequestImage>>) {
                        callback.value = RequestState(progress = false, data = response)
                    }

                    override fun onApiError(e: Throwable?) {
                        callback.value = RequestState(progress = false)
                    }
                }).addTo(disposable)
        }
    }

    fun verifyCouponCode(
        body: JsonObject, isInternetConnected: Boolean, baseView: BaseView, disposable: CompositeDisposable,
        callback: MutableLiveData<RequestState<CouponCode>>
    ) {
        if (!isInternetConnected) {
            callback.value = RequestState(error = ApiError(Config.NETWORK_ERROR, null, true))
        } else {
            mApiEndPoint.verifyCouponCode(mUserHolder.authToken, body)
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe { callback.value = RequestState(progress = true) }
                .subscribeWith(object : CallbackWrapper<AtoZResponseModel<CouponCode>>(baseView) {
                    override fun onApiSuccess(response: AtoZResponseModel<CouponCode>) {
                        callback.value = RequestState(progress = false, data = response)
                    }

                    override fun onApiError(e: Throwable?) {
                        callback.value = RequestState(progress = false)
                    }
                }).addTo(disposable)
        }
    }

    fun getOtpForCall(
        bodyData:JsonObject,
        isInternetConnected: Boolean, baseView: BaseView, disposable: CompositeDisposable,
        callback: MutableLiveData<RequestState<CallModel>>
    ) {
        if (!isInternetConnected) {
            callback.value = RequestState(error = ApiError(Config.NETWORK_ERROR, null, true))
        } else {
            mApiEndPoint.getOtpForCall(mUserHolder.authToken, bodyData)
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe { callback.value = RequestState(progress = true) }
                .subscribeWith(object : CallbackWrapper<AtoZResponseModel<CallModel>>(baseView) {
                    override fun onApiSuccess(response: AtoZResponseModel<CallModel>) {
                        callback.value = RequestState(progress = false, data = response)
                    }

                    override fun onApiError(e: Throwable?) {
                        callback.value = RequestState(progress = false)
                    }
                }).addTo(disposable)
        }
    }
}