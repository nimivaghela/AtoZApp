package com.app.atoz.shareddata.repo

import androidx.lifecycle.MutableLiveData
import com.app.atoz.common.helper.CallbackWrapper
import com.app.atoz.models.*
import com.app.atoz.models.notification.NotificationModel
import com.app.atoz.shareddata.base.BaseView
import com.app.atoz.shareddata.endpoint.ApiEndPoint
import com.app.atoz.utils.Config
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import javax.inject.Inject

class NotificationRepo @Inject constructor(private val mAppEndPoint: ApiEndPoint, private val mUserHolder: UserHolder) {

    fun provideNotificationData(
            isInternetConnected: Boolean,
            baseView: BaseView,
            disposable: CompositeDisposable,
            callback: MutableLiveData<RequestState<NotificationModel>>
    ) {
        if (!isInternetConnected) {
            callback.value = RequestState(error = ApiError(Config.NETWORK_ERROR, null, true))
        } else {

            mAppEndPoint.notificationList(mUserHolder.authToken, mUserHolder.userId)
                    .observeOn(AndroidSchedulers.mainThread())
                    .doOnSubscribe { callback.value = RequestState(progress = true) }
                    .subscribeWith(object : CallbackWrapper<AtoZResponseModel<NotificationModel>>(baseView) {
                        override fun onApiError(e: Throwable?) {
                            callback.value = RequestState(progress = false)
                        }

                        override fun onApiSuccess(response: AtoZResponseModel<NotificationModel>) {
                            callback.value = RequestState(progress = false, data = response)
                        }
                    }).addTo(disposable)
        }
    }

}