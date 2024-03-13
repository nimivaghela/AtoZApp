package com.app.atoz.ui.myorders.orderdetails

import android.annotation.SuppressLint
import android.app.Activity
import android.content.DialogInterface
import android.content.Intent
import android.text.TextUtils
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import com.app.atoz.AppApplication
import com.app.atoz.R
import com.app.atoz.common.extentions.*
import com.app.atoz.common.helper.ImagePickerHelper
import com.app.atoz.common.helper.RxHelper
import com.app.atoz.databinding.ActivityOrderDetailsBinding
import com.app.atoz.db.database.AppDatabase
import com.app.atoz.models.*
import com.app.atoz.shareddata.base.BaseActivity
import com.app.atoz.ui.main.MainActivity
import com.app.atoz.ui.myorders.MyOrdersViewModel
import com.app.atoz.ui.myorders.MyOrdersViewModel.Companion.TYPE_ORDER_BILL_UPLOADED
import com.app.atoz.ui.myorders.MyOrdersViewModel.Companion.TYPE_ORDER_CANCELLED
import com.app.atoz.ui.myorders.MyOrdersViewModel.Companion.TYPE_ORDER_COMPLETED
import com.app.atoz.ui.myorders.MyOrdersViewModel.Companion.TYPE_ORDER_CONFIRMED
import com.app.atoz.ui.myorders.MyOrdersViewModel.Companion.TYPE_ORDER_INPROGRESS
import com.app.atoz.ui.myorders.MyOrdersViewModel.Companion.TYPE_ORDER_PAYMENT_DONE
import com.app.atoz.ui.myorders.MyOrdersViewModel.Companion.TYPE_ORDER_PENDING
import com.app.atoz.ui.myorders.orderdetails.editorderdetails.EditOrderDetailsDialog
import com.app.atoz.ui.provider.jobdetails.JobBillAdapter
import com.app.atoz.ui.provider.jobdetails.jobdoneotp.JobDoneOTPDialog
import com.app.atoz.ui.provider.ratecustomer.ProviderOtherUserProfileActivity
import com.app.atoz.ui.provider.ratecustomer.ProviderOtherUserProfileActivity.Companion.KEY_CALL_FROM_DIALOG
import com.app.atoz.ui.provider.ratecustomer.ProviderOtherUserProfileActivity.Companion.KEY_PROVIDER_TO_USER_RATE
import com.app.atoz.ui.provider.ratecustomer.ProviderOtherUserProfileActivity.Companion.KEY_RATING_SUBMITTED
import com.app.atoz.ui.user.paymentmethod.PaymentMethodActivity
import com.app.atoz.ui.user.rateprovider.UserProviderProfileActivity
import com.app.atoz.ui.user.rateprovider.UserProviderProfileActivity.Companion.KEY_USER_TO_PROVIDER_RATING
import com.app.atoz.ui.viewimage.ViewImageActivity
import com.app.atoz.ui.viewimage.ViewImageActivity.Companion.KEY_VIEW_IMAGE
import com.app.atoz.ui.viewrating.ViewRatingActivity
import com.app.atoz.utils.Config
import com.app.atoz.utils.ValidationUtils
import com.google.android.material.snackbar.Snackbar
import io.reactivex.disposables.CompositeDisposable
import timber.log.Timber
import java.io.File
import javax.inject.Inject

class OrderDetailsActivity : BaseActivity(), ImagePickerHelper.ImagePickerListener,
    JobBillAdapter.OnItemClickListener, JobDoneOTPDialog.OTPStatus {

    companion object {
        const val KEY_ORDER_DETAILS_ACTIVITY = 2000
        const val KEY_ORDER_DATA = "OrderData"
        fun start(context: FragmentActivity, orderId: String) {
            context.startActivityForResult(
                Intent(context, OrderDetailsActivity::class.java)
                    .putExtra(KEY_ORDER_DATA, orderId), KEY_ORDER_DETAILS_ACTIVITY
            )
        }
    }

    private lateinit var mImagePickerHelpers: ImagePickerHelper
    private lateinit var mBillAdapter: JobBillAdapter
    @Inject
    lateinit var mViewModelFactory: ViewModelProvider.Factory

    private var needToRefreshBackScreen = false

    private val mViewModel: MyOrdersViewModel by lazy {
        ViewModelProviders.of(this, mViewModelFactory)[MyOrdersViewModel::class.java]
    }

    override fun getResource(): Int = R.layout.activity_order_details


    private lateinit var mBinding: ActivityOrderDetailsBinding
    @Inject
    lateinit var mUserHolder: UserHolder

    override fun initView() {
        (application as AppApplication).mComponent.inject(this)
        mBinding = getBinding()
        setToolbar(mBinding.orderDetailsToolbar.toolbar, getString(R.string.text_order_details), true)
        initObserver()
        loadOrderDetail()

        mImagePickerHelpers = ImagePickerHelper(this@OrderDetailsActivity, this)

    }

    override fun onBackPressed() {
        if (needToRefreshBackScreen) {
            setResult(Activity.RESULT_OK)
            finish()
        } else {
            super.onBackPressed()
        }
    }

    override fun handleListener() {
        RxHelper.onClick(mBinding.ivUserProfile, mDisposable) {
            mViewModel.getOrderDetail().value?.data?.data?.serviceRequest?.user?.image?.apply {
                ViewImageActivity.start(this@OrderDetailsActivity, this)
            }
        }

        RxHelper.onClick(mBinding.btnCancelService, mDisposable) {
            mViewModel.doRequestChangeStatus(
                RequestChangeInput(TYPE_ORDER_CANCELLED, mViewModel.mServiceId),
                isInternetConnected,
                this,
                disposable = mDisposable
            )
        }

        RxHelper.onClick(mBinding.btnRateAndReview, mDisposable) {
            launchRatingScreen(false)
        }

        RxHelper.onClick(mBinding.ibCall, mDisposable)
        {
            mViewModel.getOtpForCall(isInternetConnected, this, mDisposable)
        }

        /**
         * only for providers
         */
        RxHelper.onClick(mBinding.btnUploadBill, mDisposable) {
            if (checkValidation()) {
                showUploadBillPriceDialog()
            }
        }

        RxHelper.onClick(mBinding.btnCompleteJob, mDisposable) {
            hideKeyboard()
            showJobDoneOtpDialog(mViewModel.mServiceId)
        }

        RxHelper.onClick(mBinding.ibMap, mDisposable) {
            mViewModel.getOrderDetail().value?.data?.data?.serviceRequest?.userAddress?.let {
                if (it.latitude != null && it.longitude != null)
                    showMapForNavigation(it.latitude, it.longitude)
            }
        }

        RxHelper.onClick(mBinding.ibEditOrderDetails, mDisposable) {
            mViewModel.mLDOrderDetail.value?.data?.data?.serviceRequest?.let {
                if (it.id != null && it.note != null)
                    showEditOrderDetailsDialog(it.id.toString(), it.note.toString())
            }
        }

        /**
         * only for user
         */
        RxHelper.onClick(mBinding.btnProceedToPay, mDisposable) {
            mViewModel.mLDOrderDetail.value?.data?.data?.serviceRequest?.let {
                var subscriptionDiscount = 0f
                val serviceTotal = mViewModel.getServiceTotal(it.requestedItemList)
                val activeSubscriptionPlan: List<ActiveSubscriptionPlan>? =
                    AppDatabase.getInstance(this).activeSubscriptionPlanDao().getActiveSubscription()

                if (activeSubscriptionPlan != null && activeSubscriptionPlan.isNotEmpty()
                    && activeSubscriptionPlan[0].serviceIds.contains(it.subCategoryId.toString())
                ) {
                    subscriptionDiscount = (serviceTotal * activeSubscriptionPlan[0].discount / 100)
                }

                val totalAmount: String = String.format(
                    "%.2f",
                    mViewModel.getTotalAmount(it.totalBillAmount, serviceTotal, subscriptionDiscount)
                ).removeUnnecessaryDecimalPoint()

                var providerEnteredBillAmount = "0.00"
                if (it.totalBillAmount != null) {
                    providerEnteredBillAmount = it.totalBillAmount!!
                }
                if (it.orderId != null && it.id != null) {
                    PaymentMethodActivity.start(
                        this, it.orderId, it.id.toString(), totalAmount,
                        providerEnteredBillAmount, serviceTotal.toString(),
                        mViewModel.mDiscountAmount, mViewModel.mDiscountInPercentage, mViewModel.mDiscountCodeId,
                        subscriptionDiscount
                    )
                }
            }
        }

        RxHelper.onClick(mBinding.btnApplyPromoCode, mDisposable) {
            hideKeyboard()
            if (checkCouponValidation()) {
                mViewModel.doVerifyCouponCode(
                    mBinding.etPromoCode.text.toString(),
                    intent.getStringExtra(KEY_ORDER_DATA), isInternetConnected, this, mDisposable
                )
            }
        }

        RxHelper.onClick(mBinding.tvRate, mDisposable) {
            mViewModel.getOrderDetail().value?.data?.data?.serviceRequest?.user?.id?.let {
                ViewRatingActivity.start(this, it.toString())
            }
        }
    }

    private fun showUploadBillPriceDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle(getString(R.string.dialog_title_confirmation))

        builder.setMessage(getString(R.string.text_dialog_bill_confirm))

        builder.setPositiveButton(getString(R.string.text_yes)) { _, _ ->
            mViewModel.uploadBillAndTotalPrice(
                mBinding.etTotalBill.text.toString(),
                isInternetConnected,
                this,
                mDisposable
            )
        }

        builder.setNegativeButton(getString(R.string.text_cancel)) { dialog, _ ->
            dialog.dismiss()
        }

        val alertDialog: AlertDialog = builder.create()

        alertDialog.setOnShowListener {
            val negativeButton = (it as AlertDialog).getButton(DialogInterface.BUTTON_NEGATIVE)
            val positiveButton = it.getButton(DialogInterface.BUTTON_POSITIVE)

            positiveButton.setTextColor(ContextCompat.getColor(it.context, android.R.color.black))

            negativeButton.setBackgroundColor(ContextCompat.getColor(it.context, android.R.color.white))
            negativeButton.setTextColor(ContextCompat.getColor(it.context, android.R.color.black))
        }
        alertDialog.show()
    }

    private fun checkCouponValidation(): Boolean {
        var isValid = true

        if (!ValidationUtils.isNotEmpty(mBinding.etPromoCode.text.toString())) {
            mBinding.tilPromoCode.isErrorEnabled = true
            mBinding.tilPromoCode.error = getString(R.string.text_error_empty_promo_code)
            isValid = false
        } else {
            mBinding.tilPromoCode.isErrorEnabled = false
        }

        return isValid
    }

    private fun checkValidation(): Boolean {
        var isValid = true
        if (!ValidationUtils.isNotEmpty(mBinding.etTotalBill.text.toString())) {
            mBinding.tilTotalBill.isErrorEnabled = true
            mBinding.tilTotalBill.error = getString(R.string.text_error_empty_total_bill)
            isValid = false
        } else {
            mBinding.tilTotalBill.isErrorEnabled = false
        }

        return isValid
    }

    private fun launchRatingScreen(calledFromDialog: Boolean) {
        mViewModel.getOrderDetail().value?.data?.data?.serviceRequest?.let {
            it.user?.id?.toString()?.let { userId ->
                if (mViewModel.mUserHolder.isUserAsProvider) {
                    ProviderOtherUserProfileActivity
                        .start(this@OrderDetailsActivity, userId, it.id.toString(), calledFromDialog)
                } else {
                    UserProviderProfileActivity
                        .start(this@OrderDetailsActivity, userId, it.id.toString())
                }
            }
        }
    }

    private fun showEditOrderDetailsDialog(id: String, orderDetails: String) {
        val fragmentTransaction = supportFragmentManager.beginTransaction()
        val previousFragment =
            supportFragmentManager.findFragmentByTag(EditOrderDetailsDialog.EDIT_ORDER_DETAILS_DIALOG_TAG)
        previousFragment?.let {
            fragmentTransaction.remove(previousFragment)
        }
        fragmentTransaction.addToBackStack(null)
        val editOrderDetailsDialog = EditOrderDetailsDialog.newInstance(id, orderDetails)
        editOrderDetailsDialog.setOnEditOrderDetailsListener(object :
            EditOrderDetailsDialog.OnEditOrderDetailsListener {
            override fun onSuccess(orderDetails: String) {
                if (!isFinishing) {
                    mBinding.tvNoteDesc.text = orderDetails
                    mViewModel.getOrderDetail().value?.data?.data?.serviceRequest?.note = orderDetails
                }
            }
        })
        editOrderDetailsDialog.show(fragmentTransaction, EditOrderDetailsDialog.EDIT_ORDER_DETAILS_DIALOG_TAG)
    }

    override fun displayMessage(message: String) {
        Snackbar.make(mBinding.root, message, Snackbar.LENGTH_LONG).show()
    }

    override fun showLoadingIndicator(isShow: Boolean) {
        mBinding.progressBar.visibility = if (isShow) View.VISIBLE else View.GONE
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        mImagePickerHelpers.onRequestPermissionsResult(requestCode, grantResults)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        mImagePickerHelpers.onActivityResult(requestCode, resultCode, data)

        when (requestCode) {
            KEY_PROVIDER_TO_USER_RATE -> {
                if (resultCode == Activity.RESULT_OK) {
                    if (data != null) {
                        if (data.getBooleanExtra(KEY_RATING_SUBMITTED, false)) {
                            if (mViewModel.mLDOrderDetail
                                    .value?.data?.data?.serviceRequest?.status?.toString() == TYPE_ORDER_COMPLETED
                            ) {
                                mBinding.btnRateAndReview.visibility = View.GONE
                            } else {
                                MainActivity.start(this@OrderDetailsActivity)
                            }
                        } else if (data.getBooleanExtra(KEY_CALL_FROM_DIALOG, false)) {
                            MainActivity.start(this@OrderDetailsActivity)
                        }
                    }
                }
            }
            KEY_USER_TO_PROVIDER_RATING -> {
                if (resultCode == Activity.RESULT_OK) {
                    mBinding.btnRateAndReview.visibility = View.GONE
                }
            }
            KEY_VIEW_IMAGE -> {
                if (resultCode == Activity.RESULT_OK) {
                    mBillAdapter.notifyDataSetChanged()
                }
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun initObserver() {
        mViewModel.mBillList.observe(this, Observer {
            if (::mBillAdapter.isInitialized) {
                mBillAdapter.notifyDataSetChanged()
            } else {
                mBillAdapter = JobBillAdapter(it, this)
                mBinding.rvUploadedBill.adapter = mBillAdapter
            }
        })

        mViewModel.getOrderDetail().observe(this, Observer {

            it?.let { requestState ->
                showLoadingIndicator(requestState.progress)
                requestState.data?.let {
                    bindViewData(it.data)
                }
                requestState.error?.let { errorObj ->
                    Timber.d("Successfully Error showing")
                    when (errorObj.errorState) {
                        Config.NETWORK_ERROR ->
                            displayMessage(getString(R.string.text_error_network))
                        else -> {
                            errorObj.customMessage
                                ?.let { it1 -> displayMessage(it1) }
                        }
                    }
                }
            }
        })

        mViewModel.getChangeOrderStatusObserver().observe(this, Observer {
            it?.let { requestState ->
                showLoadingIndicator(requestState.progress)
                requestState.data?.let {
                    setResult(Activity.RESULT_OK)
                    finish()
                }
                requestState.error?.let { errorObj ->
                    Timber.d("Successfully Error showing")
                    when (errorObj.errorState) {
                        Config.NETWORK_ERROR ->
                            displayMessage(getString(R.string.text_error_network))
                        else -> {
                            errorObj.customMessage
                                ?.let { it1 -> displayMessage(it1) }
                        }
                    }
                }
            }
        })

        mViewModel.getCompleteOrderObserver().observe(this, Observer {
            it.let { requestState ->
                showLoadingIndicator(requestState.progress)
                requestState.data?.let {
                    needToRefreshBackScreen = true
                    showRateDialog()
                }
                requestState.error?.let { errorObj ->
                    Timber.d("Successfully Error showing")
                    when (errorObj.errorState) {
                        Config.NETWORK_ERROR ->
                            displayMessage(getString(R.string.text_error_network))
                        else -> {
                            errorObj.customMessage
                                ?.let { it1 -> displayMessage(it1) }
                        }
                    }
                }
            }
        })

        mViewModel.getUploadOrderBillObserver().observe(this, Observer { it ->
            it.let { requestState ->
                showLoadingIndicator(requestState.progress)
                requestState.data?.let { responseData ->
                    needToRefreshBackScreen = true
                    mViewModel.getOrderDetail().value?.data?.data?.serviceRequest?.let { serviceRequest ->
                        serviceRequest.providerStatusList?.get(0)?.providerStatus = TYPE_ORDER_BILL_UPLOADED.toInt()
                        if (serviceRequest.providerStatusList != null && serviceRequest.providerStatusList.size > 0) {
                            mBinding.tvOrderStatus.text = serviceRequest.providerStatusList[0].getOrderType()
                        }
                        mBinding.tvOrderStatus.background = ContextCompat.getDrawable(
                            this@OrderDetailsActivity, R.drawable.bg_rect_orange_with_radius
                        )
                    }

                    mBinding.rlUploadedBill.visibility = View.GONE
                    mBinding.rlPaymentStatusBgForProvider.visibility = View.VISIBLE
                    mViewModel.getOrderDetail().value?.data?.data?.serviceRequest?.let {
                        if (responseData.data != null) {
                            it.serviceRequestCompleteImages = responseData.data!!
                        }
                        it.totalBillAmount = mBinding.etTotalBill.text.toString()
                        updateOrderTotal(it)
                    }
                    bindBillViewData()
                }
                requestState.error?.let { errorObj ->
                    Timber.d("Error showing")
                    when (errorObj.errorState) {
                        Config.NETWORK_ERROR ->
                            displayMessage(getString(R.string.text_error_network))
                        else -> {
                            errorObj.customMessage?.let { it1 ->
                                displayMessage(it1)
                            }
                        }
                    }
                }
            }
        })

        mViewModel.getVerifyCouponCode().observe(this, Observer { requestState ->
            showLoadingIndicator(requestState.progress)
            requestState.data?.let {
                it.data?.apply {
                    if (discountPercentage != null && couponCodeId != null) {
                        mViewModel.mLDOrderDetail.value?.data?.data?.serviceRequest?.let { serviceRequest ->
                            mViewModel.setDiscountData(serviceRequest, discountPercentage, couponCodeId)
                            updateOrderTotal(serviceRequest)
                            mBinding.tvPromoCodeApplied.text =
                                String.format(
                                    getString(R.string.text_coupon_code_applied),
                                    mBinding.etPromoCode.text.toString()
                                )
                            mBinding.llPromoCodeBg.visibility = View.GONE
                            mBinding.tvPromoCodeApplied.visibility = View.VISIBLE
                        }
                    }
                }
            }
            requestState.error?.let { errorObj ->
                Timber.d("Error Showing")
                when (errorObj.errorState) {
                    Config.NETWORK_ERROR -> {
                        displayMessage(getString(R.string.text_error_network))
                    }
                    else -> {
                        errorObj.customMessage?.let { it1 ->
                            displayMessage(it1)
                        }
                    }
                }
            }
        })

        mViewModel.getCallOtpObserver().observe(this, Observer { requestState ->
            showLoadingIndicator(requestState.progress)
            requestState.data?.let {
                it.data?.let { callModel -> showOtpForCallDialog(callModel) }
            }
            requestState.error?.let { errorObj ->
                Timber.d("Error showing")
                when (errorObj.errorState) {
                    Config.NETWORK_ERROR -> {
                        displayMessage(getString(R.string.text_error_network))
                    }
                    else -> {
                        errorObj.customMessage?.let { it1 ->
                            displayMessage(it1)
                        }
                    }
                }
            }
        })
    }

    private fun showOtpForCallDialog(callModel: CallModel) {
        val builder = AlertDialog.Builder(this)
            .setCancelable(false)

        val view = layoutInflater.inflate(R.layout.dialog_otp_for_call, null)
        builder.setView(view)

        val btnCall = view.findViewById(R.id.ibCall) as ImageButton
        val btnSkip = view.findViewById(R.id.btnSkip) as Button
        val tvOtpCode = view.findViewById(R.id.tvOtpCode) as TextView

        tvOtpCode.text = callModel.otp

        val dialog = builder.create()

        btnCall.setOnClickListener {
            dialog.dismiss()
            callModel.phone?.let { phoneNumber -> callPhoneIntent(phoneNumber) }
        }

        btnSkip.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun showRateDialog() {
        val builder = AlertDialog.Builder(this@OrderDetailsActivity)
            .setCancelable(false)

        val view = layoutInflater.inflate(R.layout.dialog_rate_user, null)
        builder.setView(view)

        val btnSkip = view.findViewById(R.id.btnSkip) as Button
        val btnRate = view.findViewById(R.id.btnRateUser) as Button

        val dialog = builder.create()

        btnSkip.setOnClickListener {
            dialog.dismiss()
            MainActivity.start(this@OrderDetailsActivity)
        }

        btnRate.setOnClickListener {
            dialog.cancel()
            launchRatingScreen(true)
        }

        dialog.show()
    }

    private fun loadOrderDetail() {
        mViewModel.getOrderDetail(
            intent.getStringExtra(KEY_ORDER_DATA)!!, isInternetConnected,
            this,
            mDisposable
        )
    }

    @SuppressLint("SetTextI18n")
    private fun bindViewData(orderDetailsModel: OrderDetailModel?) {
        mBinding.tvOtp.visibility = if (mUserHolder.isUserAsProvider) View.GONE else View.VISIBLE
        mBinding.tvOtpValue.visibility = if (mUserHolder.isUserAsProvider) View.GONE else View.VISIBLE
        mBinding.tvOtpNote.visibility = if (mUserHolder.isUserAsProvider) View.GONE else View.VISIBLE
        orderDetailsModel?.apply {
            mBinding.serviceDetail = serviceRequest
            mBinding.isUserAsProvider = mUserHolder.isUserAsProvider

            serviceRequest?.let {


                if (it.serviceRequestImages.isNotEmpty()) {
                    mBinding.rvOrderImages.visibility = View.VISIBLE
                    mBinding.rvOrderImages.adapter =
                        OrderItemImageAdapter(it.serviceRequestImages,
                            object : OrderItemImageAdapter.OnImageItemClickListener {
                                override fun onClick(url: String) {
                                    ViewImageActivity.start(this@OrderDetailsActivity, url)
                                }
                            })
                } else {
                    mBinding.rvOrderImages.visibility = View.GONE
                }

                mViewModel.mOrderType = when {
                    mUserHolder.isUserAsProvider && it.providerStatusList != null && it.providerStatusList.size > 0 -> {
                        it.providerStatusList[0].providerStatus.toString()
                    }
                    else -> it.status.toString()
                }
                mViewModel.mServiceId = it.id.toString()
                it.user?.phone?.let {
                    mViewModel.mPhoneNumber = it
                }

                mBinding.tvOrderStatus.text = when {
                    mUserHolder.isUserAsProvider && it.providerStatusList != null && it.providerStatusList.size > 0 -> {
                        it.providerStatusList[0].getOrderType()
                    }
                    else -> it.getOrderType()
                }

                mBinding.tvOrderStatus.background = ContextCompat.getDrawable(
                    this@OrderDetailsActivity,
                    when (mViewModel.mOrderType) {
                        TYPE_ORDER_COMPLETED -> {
                            R.drawable.bg_rect_grey_with_radius
                        }
                        TYPE_ORDER_CANCELLED -> {
                            R.drawable.bg_rect_red_with_radius
                        }
                        TYPE_ORDER_BILL_UPLOADED -> {
                            R.drawable.bg_rect_orange_with_radius
                        }
                        TYPE_ORDER_PAYMENT_DONE -> {
                            R.drawable.bg_rect_blue_with_radius
                        }
                        else -> {
                            R.drawable.bg_rect_green_with_radius
                        }
                    }
                )

                showUiForProvider(mUserHolder.isUserAsProvider)

                if (!mUserHolder.isUserAsProvider) {
                    mBinding.ivUserProfile
                        .loadImage(it.user?.image, null, null, R.drawable.ic_provider_normal, true)
                    it.user.let {
                        if (TextUtils.isEmpty(it?.firstname)) {
                            mBinding.cvBasicInfo.gone()
                        } else {
                            mBinding.cvBasicInfo.visible()
                        }
                    }
                    mBinding.ibEditOrderDetails.visibility =
                        if (mViewModel.mOrderType == MyOrdersViewModel.TYPE_ORDER_PENDING) View.VISIBLE else View.GONE
                } else {
                    mBinding.ibEditOrderDetails.visibility = View.GONE
                    mBinding.ivUserProfile
                        .loadImage(it.user?.image, null, null, R.drawable.ic_user_normal, true)
                }
                manageCustomerView()

                it.requestedItemList?.let {
                    mBinding.llServicesBg.visibility = View.VISIBLE
                    mBinding.rvService.adapter = OrderSubCategoryAdapter(it)
                } ?: let {
                    mBinding.llServicesBg.visibility = View.GONE
                }

                it.userAddress?.apply {
                    mBinding.tvAddressValue.text = address ?: ""
                    mBinding.tvAddressLocationValue.text = "Location : ${location ?: ""}"
                    mBinding.tvCityValue.text = "City : ${city?.name ?: ""}"
                } ?: let {
                    mBinding.tvAddressValue.text = ""
                    mBinding.tvAddressLocationValue.text = "Location : "
                    mBinding.tvCityValue.text = "City : "
                }

                updateOrderTotal(it)

                manageCallEnable()
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun updateOrderTotal(serviceRequest: OrderDetailModel.ServiceRequest) {
        val serviceTotal = mViewModel.getServiceTotal(serviceRequest.requestedItemList)
        var subscriptionDiscount = 0f
        if (mUserHolder.isUserAsProvider) {

            mBinding.tvSubscriptionDiscount.visibility = View.VISIBLE
            mBinding.tvSubscriptionDiscountValue.visibility = View.VISIBLE

            if (serviceRequest.transactionList != null && serviceRequest.transactionList.size > 0) {
                mBinding.tvOfferDiscountValue.text =
                    "${Config.RUPEES_SYMBOL} ${String.format(
                        "%.2f",
                        serviceRequest.transactionList[0].discountAmount
                    ).removeUnnecessaryDecimalPoint()}"

                mViewModel.mDiscountInPercentage = serviceRequest.transactionList[0].discountPercentage
                mViewModel.mDiscountAmount = serviceRequest.transactionList[0].discountAmount

                mBinding.tvSubscriptionDiscountValue.text = "${Config.RUPEES_SYMBOL} ${String.format(
                    "%.2f", serviceRequest.transactionList[0].subscriptionDisAmount
                ).removeUnnecessaryDecimalPoint()}"
                subscriptionDiscount = serviceRequest.transactionList[0].subscriptionDisAmount

            } else {
                mBinding.tvOfferDiscountValue.text = "${Config.RUPEES_SYMBOL} 0.00"
                mBinding.tvSubscriptionDiscountValue.text = "${Config.RUPEES_SYMBOL} 0.00"
            }

        } else {
            if (serviceRequest.transactionList != null && serviceRequest.transactionList.size > 0
                && serviceRequest.transactionList[0].transactionStatus != 3
            ) {
                mBinding.tvOfferDiscountValue.text =
                    "${Config.RUPEES_SYMBOL} ${String.format(
                        "%.2f",
                        serviceRequest.transactionList[0].discountAmount
                    ).removeUnnecessaryDecimalPoint()}"

                if (serviceRequest.transactionList[0].subscriptionDisAmount != 0f) {
                    mBinding.tvSubscriptionDiscount.visibility = View.VISIBLE
                    mBinding.tvSubscriptionDiscountValue.visibility = View.VISIBLE
                    mBinding.tvSubscriptionDiscountValue.text = "${Config.RUPEES_SYMBOL} ${String.format(
                        "%.2f", serviceRequest.transactionList[0].subscriptionDisAmount
                    ).removeUnnecessaryDecimalPoint()}"
                    subscriptionDiscount = serviceRequest.transactionList[0].subscriptionDisAmount
                }
                mViewModel.mDiscountInPercentage = serviceRequest.transactionList[0].discountPercentage
                mViewModel.mDiscountAmount = serviceRequest.transactionList[0].discountAmount
            } else {
                mBinding.tvOfferDiscountValue.text =
                    "${Config.RUPEES_SYMBOL} ${String.format(
                        "%.2f",
                        mViewModel.mDiscountAmount
                    ).removeUnnecessaryDecimalPoint()}"

                val activeSubscriptionPlan: List<ActiveSubscriptionPlan>? =
                    AppDatabase.getInstance(this).activeSubscriptionPlanDao().getActiveSubscription()

                if (activeSubscriptionPlan != null && activeSubscriptionPlan.isNotEmpty()
                    && activeSubscriptionPlan[0].serviceIds.contains(serviceRequest.subCategoryId.toString())
                ) {
                    mBinding.tvSubscriptionDiscount.visibility = View.VISIBLE
                    mBinding.tvSubscriptionDiscountValue.visibility = View.VISIBLE

                    subscriptionDiscount = (serviceTotal * activeSubscriptionPlan[0].discount / 100)
                    mBinding.tvSubscriptionDiscountValue.text = "${Config.RUPEES_SYMBOL} ${String.format(
                        "%.2f", subscriptionDiscount
                    ).removeUnnecessaryDecimalPoint()}"
                }
            }
        }

        mBinding.tvServiceChargeValue.text =
            "${Config.RUPEES_SYMBOL} ${String.format("%.2f", serviceTotal).removeUnnecessaryDecimalPoint()}"

        mBinding.tvTotalAmountOriginalValue.text = "${Config.RUPEES_SYMBOL} ${String.format(
            "%.2f", mViewModel.getActualTotalAmount(serviceRequest.totalBillAmount, serviceTotal)
        ).removeUnnecessaryDecimalPoint()}"

        mBinding.tvOrderPriceValue.text = "${Config.RUPEES_SYMBOL} ${serviceRequest.totalBillAmount?.let {
            String.format("%.2f", it.toFloat()).removeUnnecessaryDecimalPoint()
        } ?: let { "-" }}"

        mBinding.tvTotalAmountValue.text = "${Config.RUPEES_SYMBOL} ${String.format(
            "%.2f", mViewModel.getTotalAmount(
                serviceRequest.totalBillAmount, serviceTotal, subscriptionDiscount
            )
        ).removeUnnecessaryDecimalPoint()}"

        mBinding.tvTotalAmountOriginalValue.visibility =
            if (mBinding.tvTotalAmountOriginalValue.text == mBinding.tvTotalAmountValue.text)
                View.GONE else View.VISIBLE
    }

    private fun showUiForProvider(userAsProvider: Boolean) {
        /*mBinding.tvExperience.text =
            if (userAsProvider) "Ahmedabad" else String.format(getString(R.string.text_years_experience), 2)
        */
        if (userAsProvider) {
            mBinding.tvRate.gone()
            mBinding.tvExperience.visible()
            mBinding.btnCancelService.visibility = View.GONE
            mBinding.rlViewBill.visibility = View.GONE
        } else {
            mBinding.tvRate.visible()
            mBinding.tvExperience.gone()
        }
    }

    private fun manageCustomerView() {
        /**
         * hide cancel button for all the orders
         */
        mBinding.btnCancelService.gone()
        when (mViewModel.mOrderType) {
            TYPE_ORDER_CONFIRMED -> {
                mBinding.rlUploadedBill.visibility = if (mUserHolder.isUserAsProvider) View.VISIBLE else View.GONE
            }
            TYPE_ORDER_PENDING -> {
                if (!mUserHolder.isUserAsProvider) mBinding.btnCancelService.visible()
            }
            TYPE_ORDER_INPROGRESS -> {
            }
            TYPE_ORDER_CANCELLED -> {
                mBinding.tvOtp.gone()
                mBinding.tvOtpNote.gone()
                mBinding.tvOtpValue.gone()
            }
            TYPE_ORDER_BILL_UPLOADED -> {
                bindBillViewData()
                mBinding.rlPaymentStatusBgForProvider.visible()
                mViewModel.getOrderDetail().value?.data?.data?.serviceRequest?.let {
                    if (it.transactionList != null && it.transactionList.size > 0) {
                        mBinding.tvPaymentStatus.text = getString(R.string.text_payment_method)
                        mBinding.tvPaymentStatusValue.text = when {
                            it.transactionList[0].transactionType == "1" -> getString(R.string.text_cash)
                            it.transactionList[0].transactionType == "2" -> getString(R.string.text_online_payment)
                            else -> "-"
                        }
                    }
                }

                if (mUserHolder.isUserAsProvider) {
                    mBinding.llPromoCodeBg.gone()
                    mBinding.btnCompleteJob.visible()
                } else {
                    mBinding.btnCompleteJob.gone()
                    mViewModel.getOrderDetail().value?.data?.data?.serviceRequest?.let {
                        if (it.transactionList != null && it.transactionList.size > 0
                            && (it.transactionList[0].transactionStatus == 1 || it.transactionList[0].transactionStatus == 2)
                        ) {
                            mBinding.btnProceedToPay.gone()
                        } else {
                            mBinding.llPromoCodeBg.visible()
                            mBinding.btnProceedToPay.visible()
                        }
                    }
                }
            }
            TYPE_ORDER_PAYMENT_DONE -> {
                bindBillViewData()
                mBinding.rlPaymentStatusBgForProvider.visible()
                if (mUserHolder.isUserAsProvider) {
                    mBinding.btnCompleteJob.visible()
                } else {
                    mBinding.btnCompleteJob.gone()
                }
                mViewModel.getOrderDetail().value?.data?.data?.serviceRequest?.let {
                    if (it.transactionList != null && it.transactionList.size > 0) {
                        mBinding.tvPaymentStatus.text = getString(R.string.text_payment_method)
                        mBinding.tvPaymentStatusValue.text = when {
                            it.transactionList[0].transactionType == "1" -> getString(R.string.text_cash)
                            it.transactionList[0].transactionType == "2" -> getString(R.string.text_online_payment)
                            else -> "-"
                        }
                    }
                }
                mBinding.llPromoCodeBg.gone()
            }
            TYPE_ORDER_COMPLETED -> {
                mViewModel.getOrderDetail().value?.data?.data?.serviceRequest?.let {

                    mBinding.btnRateAndReview.visibility = if (it.isRatingDone) View.GONE else View.VISIBLE

                    bindBillViewData()
                    mBinding.llPromoCodeBg.gone()
                    mBinding.rlPaymentStatusBgForProvider.visible()
                    mBinding.btnCompleteJob.gone()
                    mBinding.tvPaymentStatus.text = getString(R.string.text_payment_method)
                    mBinding.tvPaymentStatusValue.text =
                        if (it.transactionList != null && it.transactionList.size > 0) {
                            when {
                                it.transactionList[0].transactionType == "1" -> getString(R.string.text_cash)
                                it.transactionList[0].transactionType == "2" -> getString(R.string.text_online_payment)
                                else -> "-"
                            }
                        } else "-"
                }
            }
        }
    }

    private fun bindBillViewData() {
        mViewModel.getOrderDetail().value?.data?.data?.serviceRequest?.let {
            mBinding.rlViewBill.visible()

            if (it.serviceRequestCompleteImages.isEmpty()) {
                mBinding.tvBillNotAvailable.visible()
                mBinding.rvBill.gone()
            } else {
                mBinding.tvBillNotAvailable.gone()
                mBinding.rvBill.visible()

                mBinding.rvBill.adapter =
                    OrderBillImageAdapter(
                        it.serviceRequestCompleteImages,
                        object : OrderBillImageAdapter.OnOrderBillClickListener {
                            override fun onClick(imageUrl: String) {
                                ViewImageActivity.start(this@OrderDetailsActivity, imageUrl)
                            }
                        })
            }
        }
    }

    override fun onImageLoad() {
        mImagePickerHelpers.mFile?.let {
            mViewModel.addBill(it)
        }
    }

    override fun getDisposable(): CompositeDisposable = mDisposable

    override fun onRemove(position: Int) {
        mViewModel.mBillList.value?.removeAt(position)
        mBillAdapter.notifyItemRemoved(position)
    }

    override fun onAddItem() {
        mImagePickerHelpers.selectOptionToLoadImage()
    }

    override fun showFileImageInFullScreen(file: File) {
        ViewImageActivity.start(this@OrderDetailsActivity, file)
    }

    private fun showJobDoneOtpDialog(id: String) {
        val fragmentTransaction = supportFragmentManager.beginTransaction()
        val previousFragment =
            supportFragmentManager.findFragmentByTag(JobDoneOTPDialog.JOB_DONE_OTP_DIALOG_TAG)
        previousFragment?.let {
            fragmentTransaction.remove(previousFragment)
        }
        fragmentTransaction.addToBackStack(null)
        val jobDoneDialog = JobDoneOTPDialog.newInstance(id, this)
        jobDoneDialog.show(fragmentTransaction, JobDoneOTPDialog.JOB_DONE_OTP_DIALOG_TAG)
    }

    override fun status(status: Boolean) {
        hideKeyboard()
        if (status) {
            mViewModel.completeJob(isInternetConnected, this, mDisposable)
        }
    }


    private fun manageCallEnable() {
        mBinding.ibCall.isEnabled = when (mViewModel.mOrderType) {
            TYPE_ORDER_CONFIRMED, TYPE_ORDER_INPROGRESS, TYPE_ORDER_BILL_UPLOADED, TYPE_ORDER_PAYMENT_DONE -> {
                mBinding.ibCall.setImageDrawable(getDrawable(R.drawable.ic_call))
                true
            }
            else -> {
                //    TYPE_ORDER_PENDING, TYPE_ORDER_CANCELLED, TYPE_ORDER_COMPLETED
                mBinding.ibCall.setImageDrawable(getDrawable(R.drawable.ic_gray_call))
                false
            }
        }

    }
}