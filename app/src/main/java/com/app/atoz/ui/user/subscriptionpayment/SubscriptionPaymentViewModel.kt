package com.app.atoz.ui.user.subscriptionpayment

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.app.atoz.models.RequestState
import com.app.atoz.models.UserHolder
import com.app.atoz.shareddata.base.BaseView
import com.app.atoz.shareddata.repo.SubscriptionRepo
import com.google.gson.JsonObject
import io.reactivex.disposables.CompositeDisposable
import javax.inject.Inject

class SubscriptionPaymentViewModel @Inject constructor(
    private val mSubscriptionRepo: SubscriptionRepo,
    private val mUserHolder: UserHolder
) : ViewModel() {

    private val mLDSubscriptionPurchase = MutableLiveData<RequestState<Any>>()

    fun getSubscriptionPurchaseObserver(): LiveData<RequestState<Any>> = mLDSubscriptionPurchase

    fun doBuySubscriptionPlan(
        razorPayPaymentID: String,
        subscriptionId: String,
        isInternetConnected: Boolean, baseView: BaseView,
        disposable: CompositeDisposable
    ) {
        val body = JsonObject()
        body.addProperty("user_id", mUserHolder.userId)
        body.addProperty("subscription_id", subscriptionId)
        body.addProperty("transaction_id", razorPayPaymentID)
        mSubscriptionRepo.doBuySubscriptionPlan(
            body,
            isInternetConnected,
            baseView,
            disposable,
            mLDSubscriptionPurchase
        )
    }
}