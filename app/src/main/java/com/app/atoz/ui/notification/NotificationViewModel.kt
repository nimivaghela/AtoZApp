package com.app.atoz.ui.notification

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.app.atoz.models.RequestState
import com.app.atoz.models.notification.NotificationModel
import com.app.atoz.shareddata.base.BaseView
import com.app.atoz.shareddata.repo.NotificationRepo
import io.reactivex.disposables.CompositeDisposable
import javax.inject.Inject

class NotificationViewModel @Inject constructor(private val notificationRepo: NotificationRepo) : ViewModel() {
    private val mHomeDataRequest = MutableLiveData<RequestState<NotificationModel>>()


    fun getNotificationObserver(): LiveData<RequestState<NotificationModel>> {
        return mHomeDataRequest
    }

    fun populateNotificationData(
            isInternetConnected: Boolean,
            baseView: BaseView,
            disposable: CompositeDisposable
    ) {
        notificationRepo.provideNotificationData(isInternetConnected, baseView, disposable, mHomeDataRequest)
    }
}
