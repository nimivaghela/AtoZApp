package com.app.atoz.ui.user.summary

import android.annotation.SuppressLint
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.view.View
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import com.app.atoz.AppApplication
import com.app.atoz.R
import com.app.atoz.common.extentions.*
import com.app.atoz.common.helper.RxHelper
import com.app.atoz.databinding.ActivitySummaryBinding
import com.app.atoz.db.database.AppDatabase
import com.app.atoz.models.ActiveSubscriptionPlan
import com.app.atoz.models.SummaryResponseModel
import com.app.atoz.shareddata.base.BaseActivity
import com.app.atoz.ui.main.MainActivity
import com.app.atoz.utils.Config
import timber.log.Timber
import javax.inject.Inject

class SummaryActivity : BaseActivity() {
    companion object {
        const val KEY_REQUEST_ID = "RequestId"
        fun start(context: Context, requestId: String) {
            val summaryIntent = Intent(context, SummaryActivity::class.java)
                .putExtra(KEY_REQUEST_ID, requestId)
            summaryIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
            context.startActivity(summaryIntent)
        }
    }

    override fun getResource(): Int = R.layout.activity_summary

    private lateinit var mBinding: ActivitySummaryBinding

    private val mViewModel: SummaryViewModel by lazy {
        ViewModelProviders
            .of(this, viewModelFactory)[SummaryViewModel::class.java]
    }

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    override fun initView() {
        (application as AppApplication).mComponent.inject(this)
        mBinding = getBinding()
        setToolbar(mBinding.summaryToolbar.toolbar, getString(R.string.text_summary), true)
        initRequestId()
        initObserver()
        callSummaryApi()
    }

    private fun initRequestId() {
        mViewModel.setRequestId(intent.getStringExtra(KEY_REQUEST_ID))
    }

    private fun callSummaryApi() {
        mViewModel.getSummaryData(isInternetConnected, this, mDisposable)
    }

    private fun initObserver() {
        mViewModel.getSummaryRequest().observe(this,
            Observer { requestState ->
                showLoadingIndicator(requestState.progress)
                requestState.data?.let { mainResponse ->
                    Timber.d("Data rendered ")
                    showErrorView(false)
                    mainResponse.data?.let {
                        bindDataInView(it)
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
                }
            })
    }

    private fun showErrorView(isShow: Boolean) {
        mBinding.llErrorView.visibility = if (isShow) View.VISIBLE else View.GONE
        mBinding.svSummary.visibility = if (isShow) View.GONE else View.VISIBLE
    }

    @SuppressLint("SetTextI18n")
    private fun bindDataInView(summaryResponse: SummaryResponseModel) {
        summaryResponse.serviceRequest.apply {
            mBinding.rvService.adapter = SummaryAdapter(
                requestItemList,
                dateTime.stringToDate("MMM dd,yyyy") ?: "",
                dateTime.stringToDate("hh:mm a") ?: "",
                subCategoryId
            )
            mBinding.tvUserName.text = name
            addressData.let {
                mBinding.tvAddressValue.text = "${it.address} \n\nLocation : ${it.location}"
            }

            val activeSubscriptionPlan: List<ActiveSubscriptionPlan>? =
                AppDatabase.getInstance(this@SummaryActivity).activeSubscriptionPlanDao().getActiveSubscription()

            if (activeSubscriptionPlan != null && activeSubscriptionPlan.isNotEmpty()
                && activeSubscriptionPlan[0].serviceIds.contains(subCategoryId)
            ) {
                mBinding.tvSubscriptionAmount.visibility = View.VISIBLE
                mBinding.tvSubscriptionAmountValue.visibility = View.VISIBLE
                val subscriptionDiscount: Float =
                    (mViewModel.getTotalAmount() * activeSubscriptionPlan[0].discount / 100)
                mBinding.tvSubscriptionAmountValue.text =
                    "${Config.RUPEES_SYMBOL} ${String.format(
                        "%.2f",
                        subscriptionDiscount
                    ).removeUnnecessaryDecimalPoint()}"
                mBinding.tvTotalAmountValue.text =
                    "${Config.RUPEES_SYMBOL} ${String.format(
                        "%.2f",
                        mViewModel.getTotalAmount() - subscriptionDiscount
                    ).removeUnnecessaryDecimalPoint()}"
            } else {
                mBinding.tvSubscriptionAmount.visibility = View.GONE
                mBinding.tvSubscriptionAmountValue.visibility = View.GONE
                mBinding.tvTotalAmountValue.text =
                    "${Config.RUPEES_SYMBOL} ${String.format(
                        "%.2f",
                        mViewModel.getTotalAmount()
                    ).removeUnnecessaryDecimalPoint()}"
            }
        }
    }

    override fun handleListener() {
        RxHelper.onClick(mBinding.btnGoHome, mDisposable) {
            MainActivity.start(this)
        }

        RxHelper.onClick(mBinding.ibMap, mDisposable)
        {
            mViewModel.getSummaryRequest().value?.data?.data?.serviceRequest?.addressData?.let {
                if (it.latitude != null && it.longitude != null)
                    showMapForNavigation(it.latitude, it.longitude)
            }
        }

        RxHelper.onClick(mBinding.tvRetry, mDisposable) {
            callSummaryApi()
        }
    }

    override fun displayMessage(message: String) {
        showErrorView(true)
        mBinding.root.snack(message)
    }

    private fun showLoadingIndicator(isShow: Boolean) {
        mBinding.progressBar.visibility = if (isShow) View.VISIBLE else View.GONE
    }

    override fun onBackPressed() {
        showAlert(getString(R.string.text_are_your_sure),
            null,
            getString(R.string.text_yes),
            getString(R.string.text_no),
            DialogInterface.OnClickListener { dialog, button ->
                dialog?.dismiss()
                if (button == DialogInterface.BUTTON_POSITIVE) {
                    dialog?.dismiss()
                    launchMainActivity()
                }
            })
    }

    private fun launchMainActivity() {
        MainActivity.start(this@SummaryActivity)
    }
}