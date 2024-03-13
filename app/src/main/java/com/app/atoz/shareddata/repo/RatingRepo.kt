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
import javax.inject.Inject

class RatingRepo @Inject constructor(
    private val mApiEndPoint: ApiEndPoint,
    private val mUserHolder: UserHolder
) {
    fun getUserProviderRatingsComment(
        providerUserId: String,
        isInternetConnected: Boolean,
        baseView: BaseView,
        disposable: CompositeDisposable,
        callback: MutableLiveData<RequestState<ProviderUserRatingResponse>>
    ) {
        if (!isInternetConnected) {
            callback.value = RequestState(error = ApiError(Config.NETWORK_ERROR, null, true))
        } else {

            mApiEndPoint.getUserProviderRatingsComment(mUserHolder.authToken, providerUserId)
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe { callback.value = RequestState(progress = true) }
                .subscribeWith(object : CallbackWrapper<AtoZResponseModel<ProviderUserRatingResponse>>(baseView) {
                    override fun onApiError(e: Throwable?) {
                        callback.value = RequestState(progress = false)
                    }

                    override fun onApiSuccess(response: AtoZResponseModel<ProviderUserRatingResponse>) {
                        callback.value = RequestState(progress = false, data = response)
                    }
                }).addTo(disposable)
        }
    }

    fun sendRating(
        bodyData: JsonObject, isInternetConnected: Boolean, baseView: BaseView,
        disposable: CompositeDisposable, callback: MutableLiveData<RequestState<Any>>
    ) {
        if (!isInternetConnected) {
            callback.value = RequestState(error = ApiError(Config.NETWORK_ERROR, null, true))
        } else {
            mApiEndPoint.sendRating(mUserHolder.authToken, bodyData)
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe { callback.value = RequestState(progress = true) }
                .subscribeWith(object : CallbackWrapper<AtoZResponseModel<Any>>(baseView) {
                    override fun onApiSuccess(response: AtoZResponseModel<Any>) {
                        callback.value = RequestState(progress = false, data = response)
                    }

                    override fun onApiError(e: Throwable?) {
                        callback.value = RequestState(progress = false)
                    }
                }).addTo(disposable)
        }
    }

    fun getUserProviderPersonalRatingComments(
        userId: String,
        isInternetConnected: Boolean,
        baseView: BaseView,
        disposable: CompositeDisposable,
        callback: MutableLiveData<RequestState<ArrayList<UserRatingModel>>>
    ) {
        if (!isInternetConnected) {
            callback.value = RequestState(error = ApiError(Config.NETWORK_ERROR, null, true))
        } else {
            mApiEndPoint.getUserProviderPersonalRatingsComment(mUserHolder.authToken, userId)
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe { callback.value = RequestState(progress = true) }
                .subscribeWith(object : CallbackWrapper<AtoZResponseModel<ArrayList<UserRatingModel>>>(baseView) {
                    override fun onApiSuccess(response: AtoZResponseModel<ArrayList<UserRatingModel>>) {
                        callback.value = RequestState(progress = false, data = response)
                    }

                    override fun onApiError(e: Throwable?) {
                        callback.value = RequestState(progress = false)
                    }
                }).addTo(disposable)
        }
    }
}