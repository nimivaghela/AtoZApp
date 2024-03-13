package com.app.atoz.ui.provider.jobdetails

import android.annotation.SuppressLint
import android.app.Activity
import android.content.DialogInterface
import android.content.Intent
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import com.app.atoz.AppApplication
import com.app.atoz.R
import com.app.atoz.common.extentions.*
import com.app.atoz.common.helper.ImagePickerHelper
import com.app.atoz.common.helper.RxHelper
import com.app.atoz.databinding.ActivityJobDetailsBinding
import com.app.atoz.models.*
import com.app.atoz.shareddata.base.BaseActivity
import com.app.atoz.ui.main.MainActivity
import com.app.atoz.ui.myorders.MyOrdersViewModel.Companion.TYPE_ORDER_BILL_UPLOADED
import com.app.atoz.ui.myorders.MyOrdersViewModel.Companion.TYPE_ORDER_CANCELLED
import com.app.atoz.ui.myorders.MyOrdersViewModel.Companion.TYPE_ORDER_COMPLETED
import com.app.atoz.ui.myorders.MyOrdersViewModel.Companion.TYPE_ORDER_CONFIRMED
import com.app.atoz.ui.myorders.MyOrdersViewModel.Companion.TYPE_ORDER_INPROGRESS
import com.app.atoz.ui.myorders.MyOrdersViewModel.Companion.TYPE_ORDER_PAYMENT_DONE
import com.app.atoz.ui.myorders.MyOrdersViewModel.Companion.TYPE_ORDER_PENDING
import com.app.atoz.ui.myorders.orderdetails.OrderBillImageAdapter
import com.app.atoz.ui.myorders.orderdetails.OrderItemImageAdapter
import com.app.atoz.ui.myorders.orderdetails.OrderSubCategoryAdapter
import com.app.atoz.ui.provider.jobdetails.jobdoneotp.JobDoneOTPDialog
import com.app.atoz.ui.provider.ratecustomer.ProviderOtherUserProfileActivity
import com.app.atoz.ui.viewimage.ViewImageActivity
import com.app.atoz.utils.Config
import com.app.atoz.utils.ValidationUtils
import com.google.android.material.snackbar.Snackbar
import io.reactivex.disposables.CompositeDisposable
import timber.log.Timber
import java.io.File
import java.util.*
import javax.inject.Inject


class JobDetailsActivity : BaseActivity(),
    ImagePickerHelper.ImagePickerListener, JobBillAdapter.OnItemClickListener, JobDoneOTPDialog.OTPStatus {

    companion object {
        const val KEY_JOB_ID = "JOBID"
        const val KEY_REQUEST_END_TIMER = "requestEndTime"
        const val KEY_JOB_DETAILS = 4000

        fun start(context: Activity, jobId: String, requestEndTime: String?) {
            context.startActivityForResult(
                Intent(context, JobDetailsActivity::class.java)
                    .putExtra(KEY_JOB_ID, jobId)
                    .putExtra(KEY_REQUEST_END_TIMER, requestEndTime),
                KEY_JOB_DETAILS
            )
        }
    }

    override fun getResource(): Int = R.layout.activity_job_details

    private var requestEndTime: String? = null
    @Inject
    lateinit var mViewModelFactory: ViewModelProvider.Factory

    @Inject
    lateinit var userHolder: UserHolder

    private val mViewModel: JobDetailsViewModel by lazy {
        ViewModelProviders.of(this, mViewModelFactory)[JobDetailsViewModel::class.java]
    }

    private lateinit var mBinding: ActivityJobDetailsBinding
    private lateinit var mImagePickerHelpers: ImagePickerHelper
    private lateinit var mBillAdapter: JobBillAdapter

    private var needToUpdateBackScreen = false

    override fun initView() {
        (application as AppApplication).mComponent.inject(this)
        mBinding = getBinding()

        setToolbar(mBinding.jobDetailsToolbar.toolbar, getString(R.string.text_job_details), true)

        initObserver()
        loadJobDetail()

        mImagePickerHelpers = ImagePickerHelper(this@JobDetailsActivity, this)
    }

    override fun onBackPressed() {
        if (needToUpdateBackScreen) {
            setResult(Activity.RESULT_OK)
            finish()
        } else {
            super.onBackPressed()
        }
    }

    override fun handleListener() {
        RxHelper.onClick(mBinding.ivUserProfile, mDisposable) {
            mViewModel.getJobDetail().value?.data?.data?.serviceRequest?.user?.image?.apply {
                ViewImageActivity.start(this@JobDetailsActivity, this)
            }
        }

        RxHelper.onClick(mBinding.btnReject, mDisposable) {
            mViewModel.requestStatus = TYPE_ORDER_CANCELLED

            val requestChangeInput = RequestChangeInput(mViewModel.requestStatus!!, mViewModel.mJobId!!)
            mViewModel.doRequestChangeStatus(requestChangeInput, isInternetConnected, this, mDisposable)

        }

        RxHelper.onClick(mBinding.btnAccept, mDisposable) {
            //            showUploadBillUI()

            mViewModel.requestStatus = TYPE_ORDER_CONFIRMED

            val requestChangeInput = RequestChangeInput(mViewModel.requestStatus!!, mViewModel.mJobId!!)

            mViewModel.doRequestChangeStatus(requestChangeInput, isInternetConnected, this, mDisposable)
        }

        RxHelper.onClick(mBinding.btnUploadBill, mDisposable) {
            if (checkValidation()) {
                showUploadBillPriceDialog()
            }
        }

        RxHelper.onClick(mBinding.btnCompleteJob, mDisposable) {
            hideKeyboard()
            showJobDoneOtpDialog(mViewModel.getJobDetail().value?.data?.data?.serviceRequest?.id.toString())
        }

        RxHelper.onClick(mBinding.ibCall, mDisposable) {
            mViewModel.getOtpForCall(isInternetConnected, this, mDisposable)
        }

        RxHelper.onClick(mBinding.ibMap, mDisposable) {
            mViewModel.getJobDetail().value?.data?.data?.serviceRequest?.userAddress?.let {
                if (it.latitude != null && it.longitude != null)
                    showMapForNavigation(it.latitude, it.longitude)
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

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        mImagePickerHelpers.onRequestPermissionsResult(requestCode, grantResults)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        mImagePickerHelpers.onActivityResult(requestCode, resultCode, data)

        when (requestCode) {
            ProviderOtherUserProfileActivity.KEY_PROVIDER_TO_USER_RATE -> {
                launchMainActivity()
            }

            ViewImageActivity.KEY_VIEW_IMAGE -> {
                if (resultCode == Activity.RESULT_OK) {
                    mBillAdapter.notifyDataSetChanged()
                }
            }
        }
    }

    override fun onImageLoad() {
        mImagePickerHelpers.mFile?.let {
            mViewModel.addBill(it)
        }
    }

    override fun showLoadingIndicator(isShow: Boolean) {
        mBinding.progressBar.visibility = if (isShow) View.VISIBLE else View.GONE
    }

    override fun displayMessage(message: String) {
        Snackbar.make(mBinding.rlRootView, message, Snackbar.LENGTH_LONG).show()
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
        ViewImageActivity.start(this@JobDetailsActivity, file)
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

        mViewModel.getJobDetail().observe(this, Observer {

            it?.let { requestState ->
                showLoadingIndicator(requestState.progress)
                requestState.data?.let {
                    bindViewData(it)
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
                    mViewModel.requestStatus?.let { status -> manageCallEnable(status) }
                    when (mViewModel.requestStatus) {
                        TYPE_ORDER_CANCELLED -> {
                            Timber.d("Request Cancelled by me")
                            needToUpdateBackScreen = true
                            setResult(Activity.RESULT_OK)
                            finish()
                        }
                        TYPE_ORDER_CONFIRMED -> {
                            displayMessage(getString(R.string.text_order_accepted))
                            manageCustomerView()
                        }
                        else -> {
                            manageCustomerView()
                        }
                    }
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
                    needToUpdateBackScreen = true
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

        mViewModel.getUploadOrderBillObserver().observe(this, Observer {
            it.let { requestState ->
                showLoadingIndicator(requestState.progress)
                requestState.data?.let { responseData ->
                    needToUpdateBackScreen = true
                    mBinding.rlUploadedBill.visibility = View.GONE
                    mBinding.rlPaymentStatusBgForProvider.visibility = View.VISIBLE
                    mViewModel.getJobDetail().value?.data?.data?.serviceRequest?.let {
                        if (responseData.data != null) {
                            it.serviceRequestCompleteImages = responseData.data!!
                        }
                        mBinding.tvOfferDiscountValue.text = "${Config.RUPEES_SYMBOL} 0.00"
                        val serviceTotal = mViewModel.getServiceTotal(it.requestedItemList)
                        mBinding.tvServiceChargeValue.text =
                            "${Config.RUPEES_SYMBOL} ${String.format(
                                "%.2f",
                                serviceTotal
                            ).removeUnnecessaryDecimalPoint()}"
                        mBinding.tvTotalAmountValue.text = "${Config.RUPEES_SYMBOL} ${String.format(
                            "%.2f", mViewModel.getTotalAmount(
                                mBinding.etTotalBill.text.toString(), serviceTotal, 0f, 0f
                            )
                        ).removeUnnecessaryDecimalPoint()}"

                        mBinding.tvOrderPriceValue.text = "${Config.RUPEES_SYMBOL} ${
                        String.format("%.2f", mBinding.etTotalBill.text.toString().toFloat())
                            .removeUnnecessaryDecimalPoint()}"
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

    private fun bindBillViewData() {
        mViewModel.getJobDetail().value?.data?.data?.serviceRequest?.let {
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
                                ViewImageActivity.start(this@JobDetailsActivity, imageUrl)
                            }
                        })
            }
        }
    }

    private fun showRateDialog() {
        val builder = AlertDialog.Builder(this@JobDetailsActivity)
            .setCancelable(false)

        val view = layoutInflater.inflate(R.layout.dialog_rate_user, null)
        builder.setView(view)

        val btnSkip = view.findViewById(R.id.btnSkip) as Button
        val btnRate = view.findViewById(R.id.btnRateUser) as Button

        val dialog = builder.create()

        btnSkip.setOnClickListener {
            dialog.dismiss()
            MainActivity.start(this@JobDetailsActivity)
        }

        btnRate.setOnClickListener {
            dialog.cancel()
            launchRatingScreen()
        }

        dialog.show()
    }

    private fun launchRatingScreen() {
        mViewModel.getJobDetail().value?.data?.data?.serviceRequest?.let {
            it.user?.id?.toString()?.let { userId ->
                ProviderOtherUserProfileActivity.start(
                    this@JobDetailsActivity,
                    userId,
                    it.id.toString(),
                    true
                )
            }
        }
    }

    private fun launchMainActivity() {
        MainActivity.start(this@JobDetailsActivity)
    }

    private fun loadJobDetail() {

        mViewModel.mJobId = intent.getStringExtra(KEY_JOB_ID)
        requestEndTime = intent.getStringExtra(KEY_REQUEST_END_TIMER)

        mBinding.tvEndTimeValue.text =
            requestEndTime?.plusTime(userHolder.timer)?.formatTime12hr()?.toUpperCase(Locale.ROOT)


        mViewModel.mJobId?.let {
            mViewModel.getJobDetail(
                it, isInternetConnected,
                this,
                mDisposable
            )
        }
    }

    @SuppressLint("SetTextI18n")
    private fun bindViewData(orderDetails: AtoZResponseModel<OrderDetailModel>) {

        orderDetails.data?.serviceRequest?.apply {


            mBinding.serviceDetail = this
            if (serviceRequestImages.isNotEmpty()) {
                mBinding.rvOrderImages.visibility = View.VISIBLE
                mBinding.rvOrderImages.adapter =
                    OrderItemImageAdapter(serviceRequestImages,
                        object : OrderItemImageAdapter.OnImageItemClickListener {
                            override fun onClick(url: String) {
                                ViewImageActivity.start(this@JobDetailsActivity, url)
                            }
                        })
            } else {
                mBinding.rvOrderImages.visibility = View.GONE
            }

            mViewModel.mOrderType = when {
                providerStatusList != null && providerStatusList.size > 0 -> {
                    providerStatusList[0].providerStatus.toString()
                }
                else -> status.toString()
            }
            mViewModel.requestStatus = mViewModel.mOrderType

            mViewModel.serviceId = id.toString()

            user?.let {
                mBinding.ivUserProfile
                    .loadImage(it.image, null, null, R.drawable.ic_user_normal, true)

                it.phone?.let {
                    mViewModel.phoneNumber = it
                }
            }
            manageCustomerView()
            requestedItemList?.let {
                mBinding.llServicesBg.visibility = View.VISIBLE
                mBinding.rvService.adapter = OrderSubCategoryAdapter(it)
            } ?: let {
                mBinding.llServicesBg.visibility = View.GONE
            }

            userAddress?.apply {
                mBinding.tvAddressValue.text = address ?: ""
                mBinding.tvAddressLocationValue.text = "Location : ${location ?: ""}"
                mBinding.tvCityValue.text = "City : ${city?.name ?: ""}"
            } ?: let {
                mBinding.tvAddressValue.text = ""
                mBinding.tvAddressLocationValue.text = "Location : "
                mBinding.tvCityValue.text = "City : "
            }
            updateOrderTotal(this)
            manageCallEnable(mViewModel.mOrderType)
        }
    }

    @SuppressLint("SetTextI18n")
    private fun updateOrderTotal(it: OrderDetailModel.ServiceRequest) {
        var discount = 0f
        val serviceTotal = mViewModel.getServiceTotal(it.requestedItemList)
        var subscriptionDiscount = 0f

        mBinding.tvSubscriptionDiscount.visibility = View.VISIBLE
        mBinding.tvSubscriptionDiscountValue.visibility = View.VISIBLE

        if (it.transactionList != null && it.transactionList.size > 0) {
            mBinding.tvOfferDiscountValue.text =
                "${Config.RUPEES_SYMBOL} ${String.format(
                    "%.2f",
                    it.transactionList[0].discountAmount
                ).removeUnnecessaryDecimalPoint()}"
            discount = it.transactionList[0].discountAmount
            mBinding.tvSubscriptionDiscountValue.text = "${Config.RUPEES_SYMBOL} ${String.format(
                "%.2f", it.transactionList[0].subscriptionDisAmount
            ).removeUnnecessaryDecimalPoint()}"
            subscriptionDiscount = it.transactionList[0].subscriptionDisAmount
        } else {
            mBinding.tvOfferDiscountValue.text = "${Config.RUPEES_SYMBOL} 0.00"
            mBinding.tvSubscriptionDiscountValue.text = "${Config.RUPEES_SYMBOL} 0.00"
        }


        mBinding.tvServiceChargeValue.text =
            "${Config.RUPEES_SYMBOL} ${String.format("%.2f", serviceTotal).removeUnnecessaryDecimalPoint()}"

        mBinding.tvTotalAmountValue.text = "${Config.RUPEES_SYMBOL} ${String.format(
            "%.2f", mViewModel.getTotalAmount(
                it.totalBillAmount, serviceTotal, discount, subscriptionDiscount
            )
        ).removeUnnecessaryDecimalPoint()}"

        mBinding.tvOrderPriceValue.text = "${Config.RUPEES_SYMBOL} ${it.totalBillAmount?.let {
            String.format("%.2f", it.toFloat()).removeUnnecessaryDecimalPoint()
        } ?: let { "-" }}"

        mBinding.tvTotalAmountOriginalValue.text = "${Config.RUPEES_SYMBOL} ${String.format(
            "%.2f", mViewModel.getActualTotalAmount(it.totalBillAmount, serviceTotal)
        ).removeUnnecessaryDecimalPoint()}"

        mBinding.tvTotalAmountOriginalValue.visibility =
            if (mBinding.tvTotalAmountOriginalValue.text == mBinding.tvTotalAmountValue.text)
                View.GONE else View.VISIBLE
    }


    private fun manageCustomerView() {
        needToUpdateBackScreen = true

        when (mViewModel.requestStatus) {
            TYPE_ORDER_CONFIRMED -> {
                mBinding.llRejectAcceptBg.gone()
                mBinding.rlUploadedBill.visible()
                mBinding.llEndTimeBg.gone()
            }
            TYPE_ORDER_PENDING -> {
                needToUpdateBackScreen = false
                mBinding.llRejectAcceptBg.visible()
                if (!mBinding.tvEndTimeValue.text.isNullOrEmpty()) {
                    mBinding.llEndTimeBg.visible()
                }
            }
            TYPE_ORDER_INPROGRESS -> {

            }
            TYPE_ORDER_CANCELLED -> {
                mBinding.llRejectAcceptBg.gone()
            }
            TYPE_ORDER_BILL_UPLOADED -> {
                bindBillViewData()
                mBinding.rlPaymentStatusBgForProvider.visible()

                mViewModel.getJobDetail().value?.data?.data?.serviceRequest?.let {
                    if (it.transactionList != null && it.transactionList.size > 0) {
                        mBinding.tvPaymentStatus.text = getString(R.string.text_payment_method)
                        mBinding.tvPaymentStatusValue.text = when {
                            it.transactionList[0].transactionType == "1" -> getString(R.string.text_cash)
                            it.transactionList[0].transactionType == "2" -> getString(R.string.text_online_payment)
                            else -> "-"
                        }
                    }
                }
                mBinding.btnCompleteJob.visible()
            }
            TYPE_ORDER_PAYMENT_DONE -> {
                bindBillViewData()
                mBinding.rlPaymentStatusBgForProvider.visible()
                mBinding.btnCompleteJob.visible()

                mViewModel.getJobDetail().value?.data?.data?.serviceRequest?.let {
                    if (it.transactionList != null && it.transactionList.size > 0) {
                        mBinding.tvPaymentStatus.text = getString(R.string.text_payment_method)
                        mBinding.tvPaymentStatusValue.text = when {
                            it.transactionList[0].transactionType == "1" -> getString(R.string.text_cash)
                            it.transactionList[0].transactionType == "2" -> getString(R.string.text_online_payment)
                            else -> "-"
                        }
                    }
                }
            }
            TYPE_ORDER_COMPLETED -> {
                mViewModel.getJobDetail().value?.data?.data?.serviceRequest?.let {
                    bindBillViewData()
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

    override fun status(status: Boolean) {
        hideKeyboard()
        if (status) {
            mViewModel.completeJob(isInternetConnected, this, mDisposable)
        }
    }

    private fun manageCallEnable(orderType: String) {
        mBinding.ibCall.isEnabled = when (orderType) {
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