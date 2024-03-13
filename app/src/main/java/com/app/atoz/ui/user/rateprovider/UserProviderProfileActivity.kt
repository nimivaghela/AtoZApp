package com.app.atoz.ui.user.rateprovider

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
import com.app.atoz.common.extentions.snack
import com.app.atoz.common.helper.RxHelper
import com.app.atoz.databinding.ActivityUserProviderProfileBinding
import com.app.atoz.models.ProviderUserRatingResponse
import com.app.atoz.shareddata.base.BaseActivity
import com.app.atoz.ui.provider.ratecustomer.CommentAdapter
import com.app.atoz.utils.Config
import timber.log.Timber
import javax.inject.Inject

class UserProviderProfileActivity : BaseActivity(), View.OnTouchListener {

    override fun getResource() = R.layout.activity_user_provider_profile

    companion object {
        private const val KEY_PROVIDER_ID = "ProviderId"
        private const val KEY_ORDER_REQUEST_ID = "OrderRequestId"
        const val KEY_USER_TO_PROVIDER_RATING = 9000

        fun start(context: Activity, providerId: String, requestOrderId: String) {
            val mainIntent = Intent(context, UserProviderProfileActivity::class.java)
                .putExtra(KEY_PROVIDER_ID, providerId)
                .putExtra(KEY_ORDER_REQUEST_ID, requestOrderId)
            context.startActivityForResult(mainIntent, KEY_USER_TO_PROVIDER_RATING)
        }
    }

    @Inject
    lateinit var mViewModelFactory: ViewModelProvider.Factory

    private val mViewModel by lazy {
        ViewModelProviders.of(this, mViewModelFactory)[UserProviderProfileViewModel::class.java]
    }

    private lateinit var mBinding: ActivityUserProviderProfileBinding

    override fun initView() {
        (application as AppApplication).mComponent.inject(this)
        mBinding = getBinding()
        mViewModel.setProviderId(intent.getStringExtra(KEY_PROVIDER_ID)!!)
        mViewModel.setOrderRequestId(intent.getStringExtra(KEY_ORDER_REQUEST_ID)!!)
        initObserver()
        getProvidersRating()
    }

    private fun getProvidersRating() {
        mViewModel.getUserProviderRatingsComment(isInternetConnected, this, mDisposable)
    }

    private fun initObserver() {
        mViewModel.getUserProviderRatingsCommentObserver().observe(this,
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
                    closeScreen()
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
        mBinding.providerProfile = providerUserRatingResponse.userDetails
        mBinding.rcvComments.adapter = CommentAdapter(providerUserRatingResponse.ratingList)
    }

    private fun showLoadingIndicator(isShow: Boolean) {
        mBinding.progressBar.visibility = if (isShow) View.VISIBLE else View.GONE
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun handleListener() {
        mBinding.etComments.setOnTouchListener(this)

        RxHelper.onClick(mBinding.ivBack, mDisposable) {
            onBackPressed()
        }

        RxHelper.onClick(mBinding.btnSubmit, mDisposable) {
            if (checkValidation()) {
                mViewModel.sendUserToProviderRatings(
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

    private fun closeScreen() {
        setResult(Activity.RESULT_OK)
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
