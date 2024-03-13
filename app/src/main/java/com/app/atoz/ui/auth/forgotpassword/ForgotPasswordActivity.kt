package com.app.atoz.ui.auth.forgotpassword

import android.content.Context
import android.content.Intent
import android.view.View
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import com.app.atoz.AppApplication
import com.app.atoz.R
import com.app.atoz.common.extentions.hideKeyboard
import com.app.atoz.common.extentions.snack
import com.app.atoz.common.helper.RxHelper
import com.app.atoz.databinding.ActivityForgotPasswordBinding
import com.app.atoz.shareddata.base.BaseActivity
import com.app.atoz.utils.Config
import com.app.atoz.utils.ValidationUtils
import com.google.android.material.snackbar.Snackbar
import timber.log.Timber
import javax.inject.Inject

class ForgotPasswordActivity : BaseActivity() {

    companion object {
        fun start(context: Context) {
            context.startActivity(Intent(context, ForgotPasswordActivity::class.java))
        }
    }

    override fun getResource(): Int = R.layout.activity_forgot_password

    private lateinit var mBinding: ActivityForgotPasswordBinding
    private val mViewModel: ForgotPasswordViewModel by lazy {
        ViewModelProviders
            .of(this, viewModelFactory)[ForgotPasswordViewModel::class.java]
    }

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    override fun initView() {
        (application as AppApplication).mComponent.inject(this)
        mBinding = getBinding()
        setToolbar(mBinding.verificationToolbar.toolbar, "", true, R.color.white)
        initObserver()
    }

    private fun initObserver() {
        mViewModel.getForgotPasswordRequest().observe(this@ForgotPasswordActivity, Observer { it ->
            it?.let { requestState ->
                showLoadingIndicator(requestState.progress)
                requestState.data?.let {
                    Timber.d("Successfully done")
                    displayMessage(getString(R.string.text_success_forgot_password))
                }
                requestState.error?.let { errorObj ->
                    Timber.d("Successfully Error showing")
                    when (errorObj.errorState) {
                        Config.NETWORK_ERROR ->
                            displayMessage(getString(R.string.text_error_network))
                        Config.CUSTOM_ERROR ->
                            errorObj.customMessage
                                ?.let { displayMessage(it) }
                        else -> {
                        }
                    }
                }
            }
        })
    }

    private fun showLoadingIndicator(progress: Boolean) {
        mBinding.progressBar.visibility = if (progress) View.VISIBLE else View.GONE
    }

    override fun handleListener() {
        RxHelper.onClick(mBinding.btnSubmit, mDisposable) {
            hideKeyboard()
            if (checkValidation()) {
                mViewModel.doForgotPassword(
                    isInternetConnected,
                    this, mDisposable, mBinding.etEmail.text.toString()
                )
            }
        }
    }

    override fun displayMessage(message: String) {
        mBinding.rlRootView.snack(message, Snackbar.LENGTH_LONG)
    }

    private fun checkValidation(): Boolean {
        var isValid = true

        if (!ValidationUtils.isNotEmpty(mBinding.etEmail.text.toString())) {
            mBinding.tilEmail.isErrorEnabled = true
            mBinding.tilEmail.error = getString(R.string.text_error_empty_email)
            isValid = false
        } else if (!ValidationUtils.doesEmailValid(mBinding.etEmail.text.toString())) {
            mBinding.tilEmail.isErrorEnabled = true
            mBinding.tilEmail.error = getString(R.string.text_error_invalid_email)
            isValid = false
        } else {
            mBinding.tilEmail.isErrorEnabled = false
        }

        return isValid
    }
}