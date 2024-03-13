package com.app.atoz.shareddata.repo

import androidx.lifecycle.MutableLiveData
import com.app.atoz.AppApplication
import com.app.atoz.R
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

class SignInRepo @Inject constructor(private val mAppEndPoint: ApiEndPoint, private var mUserHolder: UserHolder) {

    fun signIn(
        body: JsonObject,
        isInternetConnected: Boolean,
        baseView: BaseView,
        disposable: CompositeDisposable,
        callback: MutableLiveData<RequestState<SignUpSignInResponse>>
    ) {
        if (!isInternetConnected) {
            callback.value = RequestState(error = ApiError(Config.NETWORK_ERROR, null, true))
        } else {

            mAppEndPoint.signIn(body = body)
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe { callback.value = RequestState(progress = true) }
                .subscribeWith(object : CallbackWrapper<AtoZResponseModel<SignUpSignInResponse>>(baseView) {
                    override fun onApiError(e: Throwable?) {
                        callback.value = RequestState(progress = false)
                    }

                    override fun onApiSuccess(response: AtoZResponseModel<SignUpSignInResponse>) {
                        response.data?.let {
                            try {
                                /**
                                 * store user data in shared preference
                                 */
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

    fun socialLogin(
        body: SocialLogin,
        isInternetConnected: Boolean,
        baseView: BaseView,
        disposable: CompositeDisposable,
        callback: MutableLiveData<RequestState<SignUpSignInResponse>>
    ) {
        if (!isInternetConnected) {
            callback.value = RequestState(error = ApiError(Config.NETWORK_ERROR, null, true))
        } else {

            mAppEndPoint.socialLogin(body = body)
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
                        }
                        callback.value = RequestState(progress = false, data = response)
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

}