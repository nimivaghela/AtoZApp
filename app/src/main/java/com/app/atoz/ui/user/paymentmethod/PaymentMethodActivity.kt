package com.app.atoz.ui.user.paymentmethod

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.view.View
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import com.app.atoz.AppApplication
import com.app.atoz.R
import com.app.atoz.common.extentions.resToast
import com.app.atoz.common.helper.RxHelper
import com.app.atoz.databinding.ActivityPaymentMethodBinding
import com.app.atoz.db.database.AppDatabase
import com.app.atoz.models.ActiveSubscriptionPlan
import com.app.atoz.models.PaymentDue
import com.app.atoz.models.UserHolder
import com.app.atoz.shareddata.base.BaseActivity
import com.app.atoz.ui.main.MainActivity
import com.app.atoz.utils.Config
import com.razorpay.Checkout
import com.razorpay.PaymentResultListener
import org.json.JSONObject
import timber.log.Timber
import javax.inject.Inject

class PaymentMethodActivity : BaseActivity(), PaymentResultListener {

    companion object {
        private const val KEY_REQUEST_ID = "KeyRequestId"
        private const val KEY_AMOUNT = "KeyAmount"
        private const val KEY_ORDER_ID = "KeyOrderId"
        private const val KEY_PROVIDER_ENTERED_AMOUNT = "KeyProviderEnteredAmount"
        private const val KEY_AGENT_SERVICE_CHARGE = "KeyAgentServiceCharge"
        private const val KEY_DISCOUNT_AMOUNT = "KeyDiscountAmount"
        private const val KEY_DISCOUNT_PERCENTAGE = "KeyDiscountPercentage"
        private const val KEY_DISCOUNT_COUPON_ID = "KeyCouponId"
        private const val KEY_SUBSCRIPTION_DISCOUNT = "KeySubscriptionDiscount"

        fun start(
            context: Context,
            orderId: String,
            requestId: String,
            amount: String,
            providerEnteredAmount: String,
            agentServiceCharge: String,
            discountAmount: Float?,
            discountPercentage: Int?,
            discountCouponId: String?,
            subscriptionDiscount: Float
        ) {
            val paymentIntent = Intent(context, PaymentMethodActivity::class.java)
                .putExtra(KEY_REQUEST_ID, requestId)
                .putExtra(KEY_ORDER_ID, orderId)
                .putExtra(KEY_AMOUNT, amount)
                .putExtra(KEY_PROVIDER_ENTERED_AMOUNT, providerEnteredAmount)
                .putExtra(KEY_AGENT_SERVICE_CHARGE, agentServiceCharge)
                .putExtra(KEY_SUBSCRIPTION_DISCOUNT, subscriptionDiscount)

            discountAmount?.let {
                paymentIntent.putExtra(KEY_DISCOUNT_AMOUNT, it)
            }
            discountPercentage?.let {
                paymentIntent.putExtra(KEY_DISCOUNT_PERCENTAGE, it)
            }
            discountCouponId?.let {
                paymentIntent.putExtra(KEY_DISCOUNT_COUPON_ID, it)
            }

            context.startActivity(paymentIntent)
        }
    }

    override fun getResource(): Int = R.layout.activity_payment_method

    private val mViewModel: PaymentMethodViewModel by lazy {
        ViewModelProviders
            .of(this, viewModelFactory)[PaymentMethodViewModel::class.java]
    }

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    @Inject
    lateinit var mUserHolder: UserHolder

    private lateinit var mBinding: ActivityPaymentMethodBinding

    override fun initView() {
        (application as AppApplication).mComponent.inject(this)
        mBinding = getBinding()
        setToolbar(mBinding.paymentToolbar.toolbar, getString(R.string.text_payment_method), true)
        initData()
        initObserver()
        Checkout.preload(applicationContext)
    }

    private fun initObserver() {
        mViewModel.getPaymentRequest().observe(this, Observer { requestState ->
            showLoadingIndicator(requestState.progress)
            requestState.data?.let {
                Timber.d("Data rendered ")
                MainActivity.start(this@PaymentMethodActivity)
                try {
                    if (mViewModel.paymentDue != null) {
                        Timber.d("PaymentDue Room Deleted the payment data")
                        AppDatabase.getInstance(this).paymentDueDao().deletePaymentDue(mViewModel.paymentDue!!)

                        val paymentList = AppDatabase.getInstance(this).paymentDueDao().getAllPaymentDue()
                        Timber.d("PaymentDue Room paymentDue list ${paymentList.size}")
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            requestState.error?.let { errorObj ->
                Timber.d("Successfully Error showing")
                when (errorObj.errorState) {
                    Config.NETWORK_ERROR ->
                        displayMessage(getString(R.string.text_error_network))
                    else ->
                        errorObj.customMessage
                            ?.let { displayMessage(it) }
                }
                MainActivity.start(this@PaymentMethodActivity)
            }
        })
    }

    private fun showLoadingIndicator(isShow: Boolean) {
        mBinding.progressBar.visibility = if (isShow) View.VISIBLE else View.GONE
    }

    @SuppressLint("SetTextI18n")
    private fun initData() {

        mViewModel.setRequestId(intent.getStringExtra(KEY_REQUEST_ID)!!)
        mViewModel.setTotalAmount(intent.getStringExtra(KEY_AMOUNT)!!)
        mViewModel.setOrderId(intent.getStringExtra(KEY_ORDER_ID)!!)
        mViewModel.setProviderEnteredAmount(intent.getStringExtra(KEY_PROVIDER_ENTERED_AMOUNT)!!)
        mViewModel.setAgentServiceCharge(intent.getStringExtra(KEY_AGENT_SERVICE_CHARGE)!!)

        if (intent.hasExtra(KEY_DISCOUNT_AMOUNT)) {
            mViewModel.setDiscountAmount(intent.getFloatExtra(KEY_DISCOUNT_AMOUNT, 0f).toString())
        }

        if (intent.hasExtra(KEY_DISCOUNT_PERCENTAGE)) {
            mViewModel.setDiscountPercentage(intent.getIntExtra(KEY_DISCOUNT_PERCENTAGE, 0).toString())
        }

        if (intent.hasExtra(KEY_DISCOUNT_COUPON_ID)) {
            mViewModel.setDiscountCouponId(intent.getStringExtra(KEY_DISCOUNT_COUPON_ID)!!)
        }
        mViewModel.setSubscriptionDiscount(intent.getFloatExtra(KEY_SUBSCRIPTION_DISCOUNT, 0f))

        mBinding.tvPriceValue.text = "${Config.RUPEES_SYMBOL} ${mViewModel.getTotalAmount()}"
    }

    override fun handleListener() {
        mBinding.chbOnlinePayment.setOnCheckedChangeListener(null)
        mBinding.chbCash.setOnCheckedChangeListener(null)

        RxHelper.onClick(mBinding.chbOnlinePayment, mDisposable) {
            mBinding.chbOnlinePayment.isChecked = true
            mBinding.chbCash.isChecked = false
            mViewModel.isCashPaymentSelected = false
        }

        RxHelper.onClick(mBinding.chbCash, mDisposable) {
            mBinding.chbCash.isChecked = true
            mBinding.chbOnlinePayment.isChecked = false
            mViewModel.isCashPaymentSelected = true
        }

        RxHelper.onClick(mBinding.btnProceedToPay, mDisposable) {
            if (mViewModel.isCashPaymentSelected) {
                var subscriptionUserId: Int? = null
                if (mViewModel.mSubscriptionDiscount != 0f) {
                    val activeSubscriptionPlan: List<ActiveSubscriptionPlan>? =
                        AppDatabase.getInstance(this).activeSubscriptionPlanDao().getActiveSubscription()

                    subscriptionUserId = activeSubscriptionPlan?.get(0)?.id
                }
                mViewModel.doCashPayment(subscriptionUserId, isInternetConnected, this, mDisposable)
            } else {
                razorPayPayment()
            }
        }
    }

    private fun razorPayPayment() {
        val checkout = Checkout()
        checkout.setImage(R.drawable.ic_logo_splash)

        /**
         * Pass your payment options to the Razorpay Checkout as a JSONObject
         */
        try {
            val options = JSONObject()

            /**
             * Merchant Name
             * eg: ACME Corp || HasGeek etc.
             */
            options.put("name", "URELDERSON SERVICES PVT LTD")

            /**
             * Description can be anything
             * eg: Order #123123
             * Invoice Payment
             * etc.
             */
            mViewModel.mOrderId?.also {
                options.put("description", "Order #$it")
            }

            options.put("currency", "INR")

            /**
             * Amount is always passed in PAISE
             * Eg: "500" = Rs 5.00
             */
            mViewModel.getTotalAmount()?.also {
                val totalBill = it.toFloat() * 100
                options.put("amount", totalBill)
            }

            val preFill = JSONObject()
            preFill.put("email", mUserHolder.email)
            preFill.put("contact", mUserHolder.phone)

            options.put("prefill", preFill)

            checkout.open(this, options)
        } catch (e: Exception) {
            Timber.d("Error in starting razorPay checkout :${e.printStackTrace()}")
            displayMessage(getString(R.string.text_error_internal_server))
        }

    }

    override fun displayMessage(message: String) {
        resToast(message)
    }

    override fun onPaymentError(code: Int, response: String?) {
        when (code) {
            Checkout.NETWORK_ERROR -> {
                Timber.d("Network error $response")
                displayMessage(getString(R.string.text_error_network))
            }
            Checkout.INVALID_OPTIONS -> {
                Timber.d("Invalid option $response")
                displayMessage(getString(R.string.text_error_invalid_payment))
            }
            Checkout.PAYMENT_CANCELED -> {
                Timber.d("Payment cancel $response")
            }
            Checkout.TLS_ERROR -> {
                Timber.d("Payment TLSv1 error $response")
                displayMessage(getString(R.string.text_error_payment_not_supported))
            }
        }
    }

    override fun onPaymentSuccess(razorpayPaymentID: String?) {
        val description: String? = mViewModel.mOrderId?.let {
            "Order #$it"
        }

        Timber.d("Payment success $razorpayPaymentID")

        var subscriptionUserId: Int? = null
        if (mViewModel.mSubscriptionDiscount != 0f) {
            val activeSubscriptionPlan: List<ActiveSubscriptionPlan>? =
                AppDatabase.getInstance(this).activeSubscriptionPlanDao().getActiveSubscription()

            subscriptionUserId = activeSubscriptionPlan?.get(0)?.id
        }

        /**
         * for pay online success
         */
        try {
            mViewModel.paymentDue = PaymentDue(
                mViewModel.getRequestId() ?: "",
                mUserHolder.userId!!,
                mViewModel.getTotalAmount() ?: "",
                razorpayPaymentID ?: "",
                description ?: "",
                2,
                2,
                mViewModel.mProviderEnteredAmount,
                mViewModel.mAgentServiceCharge,
                mViewModel.mDiscountAmount,
                mViewModel.mDiscountPercentage,
                mViewModel.mDiscountCouponId,
                subscriptionUserId,
                mViewModel.mSubscriptionDiscount
            )
            AppDatabase.getInstance(this).paymentDueDao().insertPaymentDue(mViewModel.paymentDue!!)

            val paymentList = AppDatabase.getInstance(this).paymentDueDao().getAllPaymentDue()
            Timber.d("PaymentDue Room paymentDue list ${paymentList.size}")
        } catch (e: Exception) {
            e.printStackTrace()
        }

        mViewModel.callTransactionApi(
            subscriptionUserId,
            razorpayPaymentID, description, false, true,
            isInternetConnected, this, mDisposable
        )
    }
}