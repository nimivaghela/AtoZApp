package com.app.atoz.ui.user.paymentmethod

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.app.atoz.models.PaymentDue
import com.app.atoz.models.RequestState
import com.app.atoz.models.UserHolder
import com.app.atoz.shareddata.base.BaseView
import com.app.atoz.shareddata.repo.PaymentRepo
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import io.reactivex.disposables.CompositeDisposable
import javax.inject.Inject

class PaymentMethodViewModel @Inject constructor(
    private val mPaymentRepo: PaymentRepo,
    private val mUserHolder: UserHolder
) : ViewModel() {

    var isCashPaymentSelected: Boolean = false
    var paymentDue: PaymentDue? = null
    private var mRequestId: String? = null
    private var mTotalAmount: String? = null
    var mOrderId: String? = null

    var mProviderEnteredAmount: String? = null
    var mAgentServiceCharge: String? = null
    var mDiscountAmount: String? = null
    var mDiscountPercentage: String? = null
    var mDiscountCouponId: String? = null
    var mSubscriptionDiscount: Float = 0f

    private val mLDPaymentRequest = MutableLiveData<RequestState<Any>>()

    fun setProviderEnteredAmount(billAmount: String) {
        mProviderEnteredAmount = billAmount
    }

    fun setAgentServiceCharge(serviceCharge: String) {
        mAgentServiceCharge = serviceCharge
    }

    fun setDiscountAmount(discountAmount: String) {
        mDiscountAmount = discountAmount
    }

    fun setDiscountPercentage(discountPercentage: String) {
        mDiscountPercentage = discountPercentage
    }

    fun setDiscountCouponId(discountCouponId: String) {
        mDiscountCouponId = discountCouponId
    }

    fun setRequestId(requestId: String) {
        mRequestId = requestId
    }

    fun setOrderId(orderId: String?) {
        mOrderId = orderId
    }

    fun getRequestId() = mRequestId

    fun setTotalAmount(totalAmount: String) {
        mTotalAmount = totalAmount
    }

    fun setSubscriptionDiscount(subscriptionDiscount: Float) {
        mSubscriptionDiscount = subscriptionDiscount
    }

    fun getTotalAmount() = mTotalAmount

    fun getPaymentRequest(): LiveData<RequestState<Any>> = mLDPaymentRequest

    fun doCashPayment(
        subscriptionUserId: Int?,
        isInternetConnected: Boolean,
        baseView: BaseView,
        disposable: CompositeDisposable
    ) {
        callTransactionApi(
            subscriptionUserId, null, null, true, false,
            isInternetConnected, baseView, disposable
        )
    }

    fun callTransactionApi(
        subscriptionUserId: Int?,
        transactionId: String?,
        description: String?,
        isCashTransaction: Boolean,
        isPaymentSuccess: Boolean,
        isInternetConnected: Boolean,
        baseView: BaseView,
        disposable: CompositeDisposable
    ) {

        val transactionArray = JsonArray()
        val transactionItem = JsonObject()

        transactionItem.addProperty("user_id", mUserHolder.userId)
        transactionItem.addProperty("request_id", mRequestId)
        transactionItem.addProperty("amount", mTotalAmount)

        if (isCashTransaction) {
            transactionItem.addProperty("status", 1)
            transactionItem.addProperty("transaction_type", 1)
        } else {
            transactionId?.also {
                transactionItem.addProperty("transaction_id", it)
            }
            description?.also {
                transactionItem.addProperty("description", it)
            }
            transactionItem.addProperty("status", if (isPaymentSuccess) 2 else 3)
            transactionItem.addProperty("transaction_type", 2)
        }

        subscriptionUserId?.let {
            transactionItem.addProperty("subscription_discount_amt", mSubscriptionDiscount)
            transactionItem.addProperty("subscription_user_id", it)
        }

        mProviderEnteredAmount?.let {
            transactionItem.addProperty("bill_amount", it)
        }
        mAgentServiceCharge?.let {
            transactionItem.addProperty("agent_amount", it)
        }
        mDiscountAmount?.let {
            transactionItem.addProperty("discount_amount", it)
        }
        mDiscountPercentage?.let {
            transactionItem.addProperty("discount_per", it)
        }
        mDiscountCouponId?.let {
            transactionItem.addProperty("coupon_id", it)
        }
        transactionArray.add(transactionItem)

        val body = JsonObject()
        body.add("transaction_detail", transactionArray)
        mPaymentRepo.doOrderPayment(body, isInternetConnected, baseView, disposable, mLDPaymentRequest)
    }

    fun callOnlineTransactionApiFromService(
        paymentList: List<PaymentDue>,
        isInternetConnected: Boolean,
        disposable: CompositeDisposable
    ) {
        val body = JsonObject()
        val transactionArray = JsonArray()
        for (paymentItem in paymentList) {
            val transactionItem = JsonObject()

            paymentItem.apply {
                transactionItem.addProperty("user_id", userId)
                transactionItem.addProperty("request_id", requestId)
                transactionItem.addProperty("amount", amount)
                transactionItem.addProperty("transaction_id", transactionId)
                transactionItem.addProperty("description", description)
                transactionItem.addProperty("status", paymentStatus)
                transactionItem.addProperty("transaction_type", transactionType)

                subscriptionUserId?.let {
                    transactionItem.addProperty("subscription_discount_amt", subscriptionDiscountAmount)
                    transactionItem.addProperty("subscription_user_id", it)
                }

                providerEnteredAmount?.let {
                    transactionItem.addProperty("bill_amount", it)
                }
                agentServiceCharge?.let {
                    transactionItem.addProperty("agent_amount", it)
                }
                discountAmount?.let {
                    transactionItem.addProperty("discount_amount", it)
                }
                discountPercentage?.let {
                    transactionItem.addProperty("discount_per", it)
                }
                discountCouponId?.let {
                    transactionItem.addProperty("coupon_id", it)
                }


                transactionArray.add(transactionItem)
            }
            body.add("transaction_detail", transactionArray)
        }

        mPaymentRepo.doOrderPayment(body, isInternetConnected, null, disposable, mLDPaymentRequest)
    }
}