package com.app.atoz.ui.user.subscriptionpayment

import android.app.Activity
import android.content.Intent
import android.view.View
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import com.app.atoz.AppApplication
import com.app.atoz.R
import com.app.atoz.common.extentions.resToast
import com.app.atoz.common.helper.RxHelper
import com.app.atoz.databinding.ActivitySubscriptionPaymentBinding
import com.app.atoz.models.SubscriptionModel
import com.app.atoz.models.UserHolder
import com.app.atoz.shareddata.base.BaseActivity
import com.app.atoz.utils.Config
import com.google.gson.Gson
import com.razorpay.Checkout
import com.razorpay.PaymentResultListener
import org.json.JSONObject
import timber.log.Timber
import javax.inject.Inject

class SubscriptionPaymentActivity : BaseActivity(), PaymentResultListener {

    companion object {
        private const val KEY_SUBSCRIPTION_MODEL = "SubscriptionModel"
        const val SUBSCRIPTION_PAYMENT_ACTIVITY = 8000

        fun start(context: Activity, subscriptionModel: SubscriptionModel) {
            val subscriptionIntent = Intent(context, SubscriptionPaymentActivity::class.java)
                .putExtra(KEY_SUBSCRIPTION_MODEL, Gson().toJson(subscriptionModel))
            context.startActivityForResult(subscriptionIntent, SUBSCRIPTION_PAYMENT_ACTIVITY)
        }
    }

    private val mViewModel: SubscriptionPaymentViewModel by lazy {
        ViewModelProviders
            .of(this, viewModelFactory)[SubscriptionPaymentViewModel::class.java]
    }

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    @Inject
    lateinit var mUserHolder: UserHolder

    private lateinit var mSubscriptionModel: SubscriptionModel

    private lateinit var mBinding: ActivitySubscriptionPaymentBinding

    override fun getResource(): Int = R.layout.activity_subscription_payment

    override fun initView() {
        (application as AppApplication).mComponent.inject(this)
        mBinding = getBinding()
        setToolbar(mBinding.paymentToolbar.toolbar, getString(R.string.text_subscription_payment), true)
        initData()
        initObserver()
        Checkout.preload(applicationContext)
    }

    private fun initObserver() {
        mViewModel.getSubscriptionPurchaseObserver().observe(this, Observer { requestState ->
            showLoadingIndicator(requestState.progress)
            requestState.data?.let {
                Timber.d("Data rendered ")
                setResult(Activity.RESULT_OK)
                finish()
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
            }
        })
    }

    private fun showLoadingIndicator(isShow: Boolean) {
        mBinding.progressBar.visibility = if (isShow) View.VISIBLE else View.GONE
    }

    private fun initData() {
        mSubscriptionModel =
            Gson().fromJson(intent.getStringExtra(KEY_SUBSCRIPTION_MODEL), SubscriptionModel::class.java)
        mBinding.subscription = mSubscriptionModel

    }

    override fun handleListener() {
        RxHelper.onClick(mBinding.btnProceedToPay, mDisposable) {
            razorPayPayment()
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
        Timber.d("Razor pay Payment Response $razorpayPaymentID")
        razorpayPaymentID?.let {
            mViewModel.doBuySubscriptionPlan(
                it,
                mSubscriptionModel.subscriptionId,
                isInternetConnected,
                this,
                mDisposable
            )
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
            mSubscriptionModel.subscriptionName.also {
                options.put("description", "Subscription plan : $it")
            }

            options.put("currency", "INR")

            /**
             * Amount is always passed in PAISE
             * Eg: "500" = Rs 5.00
             */
            mSubscriptionModel.subscriptionPrice.also {
                val totalBill = it * 100
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
}