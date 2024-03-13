package com.app.atoz.shareddata.repo

import androidx.lifecycle.MutableLiveData
import com.app.atoz.AppApplication
import com.app.atoz.R
import com.app.atoz.common.helper.CallbackWrapper
import com.app.atoz.models.*
import com.app.atoz.shareddata.base.BaseView
import com.app.atoz.shareddata.endpoint.ApiEndPoint
import com.app.atoz.utils.Config
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import okhttp3.MultipartBody
import timber.log.Timber
import javax.inject.Inject

class SignUpRepo @Inject constructor(private val mAppEndPoint: ApiEndPoint, private var mUserHolder: UserHolder) {

    fun signUp(
        multipartBody: MultipartBody,
        isInternetConnected: Boolean,
        baseView: BaseView,
        disposable: CompositeDisposable,
        callback: MutableLiveData<RequestState<SignUpSignInResponse>>
    ) {
        if (!isInternetConnected) {
            callback.value = RequestState(error = ApiError(Config.NETWORK_ERROR, null, true))
        } else {

            mAppEndPoint.signUp(body = multipartBody)
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe { callback.value = RequestState(progress = true) }
                .subscribeWith(object : CallbackWrapper<AtoZResponseModel<SignUpSignInResponse>>(baseView) {
                    override fun onApiError(e: Throwable?) {
                        callback.value = RequestState(progress = false)
                    }

                    override fun onApiSuccess(response: AtoZResponseModel<SignUpSignInResponse>) {
                        response.data?.let {
                            /**
                             * store user data in shared preference
                             */
                            try {
                                mUserHolder.storeDate(
                                    it.token,
                                    it.userType != "user",
                                    it.userId,
                                    it.email,
                                    it.firstName,
                                    it.lastName,
                                    it.phone,
                                    it.image,
                                    it.imageThumb,
                                    it.address,
                                    it.latitude,
                                    it.longitude,
                                    it.certificateUrl,
                                    it.isVerified,
                                    it.facebookId,
                                    it.googleId,
                                    it.status
                                )
                            } catch (e: Exception) {
                                e.printStackTrace()
                                callback.value = RequestState(
                                    error = ApiError(
                                        Config.CUSTOM_ERROR,
                                        AppApplication.instance.getString(R.string.text_error_internal_server),
                                        true
                                    )
                                )
                            }
                        }
                        callback.value = RequestState(progress = false, data = response)
                    }
                }).addTo(disposable)
        }
    }

    fun phoneEmailExist(
        data: JsonObject, isInternetConnected: Boolean,
        baseView: BaseView,
        disposable: CompositeDisposable,
        callback: MutableLiveData<RequestState<PhoneEmailExistResponseModel>>
    ) {
        if (!isInternetConnected) {
            callback.value = RequestState(error = ApiError(Config.NETWORK_ERROR, null, true))
        } else {
            mAppEndPoint.checkPhoneEmailExist(data)
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe { callback.value = RequestState(progress = true) }
                .subscribeWith(object : CallbackWrapper<AtoZResponseModel<PhoneEmailExistResponseModel>>(baseView) {
                    override fun onApiError(e: Throwable?) {
                        callback.value = RequestState(progress = false)
                    }

                    override fun onApiSuccess(response: AtoZResponseModel<PhoneEmailExistResponseModel>) {
                        callback.value = RequestState(progress = false, data = response)
                    }
                }).addTo(disposable)
        }
    }

    fun provideCities(
        isInternetConnected: Boolean,
        baseView: BaseView?,
        disposable: CompositeDisposable,
        callback: MutableLiveData<RequestState<CityModel>>
    ) {
        if (!isInternetConnected) {
            callback.value = RequestState(error = ApiError(Config.NETWORK_ERROR, null, true))
        } else {

            mAppEndPoint.getCities()
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe { callback.value = RequestState(progress = true) }
                .subscribeWith(object : CallbackWrapper<AtoZResponseModel<CityModel>>(baseView) {
                    override fun onApiError(e: Throwable?) {
                        callback.value = RequestState(progress = false)
                    }

                    override fun onApiSuccess(response: AtoZResponseModel<CityModel>) {
                        callback.value = RequestState(progress = false, data = response)
                    }
                }).addTo(disposable)
        }
    }

    fun getCategoryList(
        body: JsonObject,
        isInternetConnected: Boolean,
        baseView: BaseView,
        disposable: CompositeDisposable,
        callback: MutableLiveData<RequestState<CategorySelectionResponseModel>>
    ) {

        if (!isInternetConnected) {
            callback.value = RequestState(error = ApiError(Config.NETWORK_ERROR, null, true))
        } else {
            mAppEndPoint.getCategoryWithServiceList(body)
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

    fun doCheckIsSocialIDExist(
        body: JsonObject,
        isInternetConnected: Boolean,
        baseView: BaseView,
        disposable: CompositeDisposable,
        callback: MutableLiveData<RequestState<IsSocialMediaExistResponse>>
    ) {
        if (!isInternetConnected) {
            callback.value = RequestState(error = ApiError(Config.NETWORK_ERROR, null, true))
        } else {

            mAppEndPoint.checkIsSocialIDExist(body = body)
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe { callback.value = RequestState(progress = true) }
                .subscribeWith(object : CallbackWrapper<AtoZResponseModel<IsSocialMediaExistResponse>>(baseView) {
                    override fun onApiError(e: Throwable?) {
                        callback.value = RequestState(progress = false)
                    }

                    override fun onApiSuccess(response: AtoZResponseModel<IsSocialMediaExistResponse>) {

                        callback.value = RequestState(progress = false, data = response)
                    }
                }).addTo(disposable)
        }
    }


    fun updateFCMToken(
        multipartBody: MultipartBody,
        isInternetConnected: Boolean, disposable: CompositeDisposable,
        callback: MutableLiveData<RequestState<ProfileResponseModel>>?
    ) {
        if (!isInternetConnected) {
            callback?.value = RequestState(error = ApiError(Config.NETWORK_ERROR, null, true))
        } else {
            mAppEndPoint.editProfile(mUserHolder.authToken, multipartBody)
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe { callback?.value = RequestState(progress = true) }
                .subscribeWith(object :
                    CallbackWrapper<AtoZResponseModel<ProfileResponseModel>>(null) {
                    override fun onApiSuccess(response: AtoZResponseModel<ProfileResponseModel>) {
                        Timber.i("FCM token update")
                    }

                    override fun onApiError(e: Throwable?) {
                        callback?.value = RequestState(progress = false)
                    }
                }).addTo(disposable)
        }


    }

    fun fetchTimerValue(
        isInternetConnected: Boolean, disposable: CompositeDisposable,
        callback: MutableLiveData<RequestState<JsonElement>>?
    ) {
        if (!isInternetConnected) {
            callback?.value = RequestState(error = ApiError(Config.NETWORK_ERROR, null, true))
        } else {
            mAppEndPoint.getTimerValue(mUserHolder.authToken)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(object :
                    CallbackWrapper<AtoZResponseModel<JsonElement>>(null) {
                    override fun onApiSuccess(response: AtoZResponseModel<JsonElement>) {
                        response.data?.asJsonObject?.get("value")?.asString?.let {
                            mUserHolder.timer = Integer.parseInt(it)
                        }
                    }

                    override fun onApiError(e: Throwable?) {
                    }
                }).addTo(disposable)
        }
    }
}