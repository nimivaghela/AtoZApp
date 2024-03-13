package com.app.atoz.ui.user.subscription

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.app.atoz.models.ActiveSubscriptionPlan
import com.app.atoz.models.RequestState
import com.app.atoz.models.SubscriptionResponse
import com.app.atoz.shareddata.base.BaseView
import com.app.atoz.shareddata.repo.SubscriptionRepo
import io.reactivex.disposables.CompositeDisposable
import javax.inject.Inject

class SubscriptionViewModel @Inject constructor(
    private val mSubscriptionRepo: SubscriptionRepo
) : ViewModel() {

    private val mLDSubscriptionList = MutableLiveData<RequestState<SubscriptionResponse>>()
    private val mLDActiveSubscription = MutableLiveData<RequestState<ActiveSubscriptionPlan>>()

    fun getSubscriptionListRequest(): LiveData<RequestState<SubscriptionResponse>> = mLDSubscriptionList

    fun getSubscriptionList(isInternetConnected: Boolean, baseView: BaseView, disposable: CompositeDisposable) {
        mSubscriptionRepo.getSubscriptionList(isInternetConnected, baseView, disposable, mLDSubscriptionList)
    }

    fun getActiveSubscriptionObserver(): LiveData<RequestState<ActiveSubscriptionPlan>> = mLDActiveSubscription

    fun getActiveSubscriptionData(isInternetConnected: Boolean, disposable: CompositeDisposable) {
        mSubscriptionRepo.getActiveSubscriptionPlan(isInternetConnected, null, disposable, mLDActiveSubscription)
    }
}