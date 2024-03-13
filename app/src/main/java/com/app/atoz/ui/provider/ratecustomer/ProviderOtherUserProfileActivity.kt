package com.app.atoz.ui.provider.ratecustomer

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.view.MotionEvent
import android.view.View
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import com.app.atoz.AppApplication
import com.app.atoz.R
import com.app.atoz.common.extentions.hideKeyboard
import com.app.atoz.common.extentions.snack
import com.app.atoz.common.helper.RxHelper
import com.app.atoz.databinding.ActivityProviderOtherUserProfileBinding
import com.app.atoz.models.ProviderUserRatingResponse
import com.app.atoz.shareddata.base.BaseActivity
import com.app.atoz.utils.Config
import timber.log.Timber
import javax.inject.Inject

class ProviderOtherUserProfileActivity : BaseActivity(), View.OnTouchListener {

    override fun getResource(): Int = R.layout.activity_provider_other_user_profile

    companion object {
        private const val KEY_USER_ID = "UserId"
        private const val KEY_ORDER_REQUEST_ID = "OrderRequestId"
        const val KEY_PROVIDER_TO_USER_RATE = 8000
        const val KEY_RATING_SUBMITTED = "RatingSubmitted"
        const val KEY_CALL_FROM_DIALOG = "CalledFromRateDialog"

        fun start(context: Activity, userId: String, requestOrderId: String, calledFromDialog: Boolean = false) {
            val mainIntent = Intent(context, ProviderOtherUserProfileActivity::class.java)
                .putExtra(KEY_USER_ID, userId)
                .putExtra(KEY_ORDER_REQUEST_ID, requestOrderId)
                .putExtra(KEY_CALL_FROM_DIALOG, calledFromDialog)
            context.startActivityForResult(mainIntent, KEY_PROVIDER_TO_USER_RATE)
        }
    }

    @Inject
    lateinit var mViewModelFactory: ViewModelProvider.Factory

    private val mViewModel by lazy {
        ViewModelProviders.of(this, mViewModelFactory)[ProviderOtherUserProfileViewModel::class.java]
    }

    private lateinit var mBinding: ActivityProviderOtherUserProfileBinding

    override fun initView() {
        (application as AppApplication).mComponent.inject(this)
        mBinding = getBinding()
        mViewModel.setUserId(intent.getStringExtra(KEY_USER_ID)!!)
        mViewModel.setOrderRequestId(intent.getStringExtra(KEY_ORDER_REQUEST_ID)!!)
        initObserver()
        getProvidersRating()
    }

    private fun getProvidersRating() {
        mViewModel.getProviderToUserRatingsComment(isInternetConnected, this, mDisposable)
    }

    private fun initObserver() {
        mViewModel.getProviderToUserRatingsCommentObserver().observe(this,
            Observer { requestState ->
                showLoadingIndicator(requestState.progress)
                requestState.data?.let {
                    Timber.d("Successfully data showing")
                    it.data?.apply {
                        bindViewData(this)
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
            })

        mViewModel.getSendRatingObserver().observe(this,
            Observer { requestState ->
                showLoadingIndicator(requestState.progress)
                requestState.data?.let {
                    Timber.d("Success response")
                    closeScreen(true)
                }
                requestState.error?.let { errorObj ->
                    Timber.d("Error showing")
                    when (errorObj.errorState) {
                        Config.NETWORK_ERROR ->
                            displayMessage(getString(R.string.text_error_network))
                        else -> {
                            errorObj.customMessage?.let { message ->
                                displayMessage(message)
                            }
                        }
                    }
                }
            })
    }

    private fun bindViewData(providerUserRatingResponse: ProviderUserRatingResponse) {
        mBinding.userProfile = providerUserRatingResponse.userDetails
        mBinding.rcvComments.adapter = CommentAdapter(providerUserRatingResponse.ratingList)
    }

    private fun showLoadingIndicator(isShow: Boolean) {
        mBinding.progressBar.visibility = if (isShow) View.VISIBLE else View.GONE
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun handleListener() {

        mBinding.etComments.setOnTouchListener(this)

        RxHelper.onClick(mBinding.ivBack, mDisposable) {
            closeScreen(false)
        }

        RxHelper.onClick(mBinding.btnSubmit, mDisposable) {
            hideKeyboard()
            if (checkValidation()) {
                mViewModel.sendProviderToUserRatings(
                    mBinding.rbProvider.rating, mBinding.etComments.text.toString(),
                    isInternetConnected, this, mDisposable
                )
            }
        }
    }

    private fun checkValidation(): Boolean {
        var isSuccess = true
        if (mBinding.rbProvider.rating == 0f) {
            displayMessage(getString(R.string.text_error_empty_rating))
            isSuccess = false
        }

        if (mBinding.etComments.text.toString().isBlank()) {
            mBinding.tilComment.isErrorEnabled = true
            mBinding.tilComment.error = getString(R.string.text_error_empty_review)
            isSuccess = false
        } else {
            mBinding.tilComment.isErrorEnabled = false
        }

        return isSuccess
    }

    override fun displayMessage(message: String) {
        mBinding.root.snack(message)
    }

    override fun onBackPressed() {
        closeScreen(false)
    }

    private fun closeScreen(isRateSubmitted: Boolean) {
        val dataIntent = Intent()
        dataIntent.putExtra(KEY_RATING_SUBMITTED, isRateSubmitted)
        dataIntent.putExtra(KEY_CALL_FROM_DIALOG, intent.getBooleanExtra(KEY_CALL_FROM_DIALOG, false))
        setResult(Activity.RESULT_OK, dataIntent)
        finish()
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouch(v: View?, event: MotionEvent?): Boolean {
        if (event != null) {
            if (v?.id == R.id.et_comments) {
                v.parent.requestDisallowInterceptTouchEvent(true)
                when (event.action and MotionEvent.ACTION_MASK) {
                    MotionEvent.ACTION_UP -> v.parent.requestDisallowInterceptTouchEvent(false)
                }
            }
        }
        return false
    }
}
