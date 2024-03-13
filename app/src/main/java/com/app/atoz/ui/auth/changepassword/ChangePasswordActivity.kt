package com.app.atoz.ui.auth.changepassword


import android.content.Context
import android.content.Intent
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import com.app.atoz.AppApplication
import com.app.atoz.R
import com.app.atoz.common.extentions.hideKeyboard
import com.app.atoz.common.extentions.resToast
import com.app.atoz.common.extentions.snack
import com.app.atoz.common.helper.RxHelper
import com.app.atoz.databinding.ActivityChangePasswordBinding
import com.app.atoz.shareddata.base.BaseActivity
import com.app.atoz.utils.Config
import com.app.atoz.utils.ValidationUtils
import timber.log.Timber
import javax.inject.Inject

/**
 * A simple [Fragment] subclass.
 */
class ChangePasswordActivity : BaseActivity() {

    companion object {
        fun start(context: Context) {
            val starter = Intent(context, ChangePasswordActivity::class.java)
            context.startActivity(starter)
        }
    }

    override fun getResource() = R.layout.activity_change_password

    private lateinit var mBinding: ActivityChangePasswordBinding
    private val mViewModel: ChangePasswordViewModel by lazy {
        ViewModelProviders.of(this, mViewModelFactory)[ChangePasswordViewModel::class.java]
    }

    @Inject
    lateinit var mViewModelFactory: ViewModelProvider.Factory

    override fun initView() {
        (application as AppApplication).mComponent.inject(this)
        mBinding = getBinding()
        setToolbar(mBinding.includeToolbar.toolbar, getString(R.string.change_password), true)
        initObserver()
    }

    private fun initObserver() {
        mViewModel.getChangePasswordRequest().observe(this, Observer { requestState ->
            showLoadingIndicator(requestState.progress)
            requestState.data?.let {
                Timber.d("Successfully done")
                resToast(R.string.text_password_changed_success)
                finish()
            }
            requestState.error?.let { error ->
                Timber.d("Successfully Error showing")
                when (error.errorState) {
                    Config.NETWORK_ERROR ->
                        displayMessage(getString(R.string.text_error_network))
                    else ->
                        error.customMessage
                            ?.let { displayMessage(it) }

                }
            }
        })
    }

    private fun showLoadingIndicator(isShow: Boolean) {
        mBinding.progressBar.visibility = if (isShow) View.VISIBLE else View.GONE
    }

    override fun handleListener() {
        RxHelper.onClick(mBinding.btnSubmit, mDisposable) {
            hideKeyboard()
            if (checkValidation()) {
                mViewModel.doChangePassword(
                    mBinding.etOldPassword.text.toString(),
                    mBinding.etNewPassword.text.toString(), isInternetConnected, this, mDisposable
                )
            }
        }
    }

    override fun displayMessage(message: String) {
        mBinding.rlRootView.snack(message)
    }

    private fun checkValidation(): Boolean {
        var isValid = true

        if (!ValidationUtils.isNotEmpty(mBinding.etOldPassword.text.toString())) {
            mBinding.tilOldPassword.isErrorEnabled = true
            mBinding.tilOldPassword.error = getString(R.string.text_error_old_password)
            isValid = false
        } else {
            mBinding.tilOldPassword.isErrorEnabled = false
        }

        if (!ValidationUtils.isNotEmpty(mBinding.etNewPassword.text.toString())) {
            mBinding.tilNewPassword.isErrorEnabled = true
            mBinding.tilNewPassword.error = getString(R.string.text_error_new_password)
            isValid = false
        } else if (!ValidationUtils.doesPasswordLengthValid(mBinding.etNewPassword.text.toString())) {
            mBinding.tilNewPassword.isErrorEnabled = true
            mBinding.tilNewPassword.error = getString(R.string.text_error_invalid_password)
            isValid = false
        } else if (ValidationUtils.doesOldPasswordAndNewPasswordSame(
                mBinding.etOldPassword.text.toString(), mBinding.etNewPassword.text.toString()
            )
        ) {
            mBinding.tilNewPassword.isErrorEnabled = true
            mBinding.tilNewPassword.error = getString(R.string.text_error_old_new_password_not_same)
            isValid = false
        } else {
            mBinding.tilNewPassword.isErrorEnabled = false
        }

        if (!ValidationUtils.isNotEmpty(mBinding.etConfirmPassword.text.toString())) {
            mBinding.tilConfirmPassword.isErrorEnabled = true
            mBinding.tilConfirmPassword.error = getString(R.string.text_error_confirm_password)
            isValid = false
        } else if (!ValidationUtils.doesNewPasswordAndConfirmPasswordSame(
                mBinding.etNewPassword.text.toString(),
                mBinding.etConfirmPassword.text.toString()
            )
        ) {
            mBinding.tilConfirmPassword.isErrorEnabled = true
            mBinding.tilConfirmPassword.error = getString(R.string.text_error_new_and_confirm_password_same)
            isValid = false
        } else {
            mBinding.tilConfirmPassword.isErrorEnabled = false
        }
        return isValid
    }

}
