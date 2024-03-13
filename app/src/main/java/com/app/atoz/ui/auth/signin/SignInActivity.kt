package com.app.atoz.ui.auth.signin

import android.content.Context
import android.content.Intent
import android.text.method.LinkMovementMethod
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import com.app.atoz.AppApplication
import com.app.atoz.R
import com.app.atoz.common.extentions.*
import com.app.atoz.common.helper.FacebookManager
import com.app.atoz.common.helper.GooglePlusManager
import com.app.atoz.common.helper.RxHelper
import com.app.atoz.common.listeners.SocailCallBackListener
import com.app.atoz.databinding.ActivitySigninBinding
import com.app.atoz.models.SocialLogin
import com.app.atoz.models.UserHolder
import com.app.atoz.services.ActiveSubscriptionService
import com.app.atoz.shareddata.base.BaseActivity
import com.app.atoz.ui.auth.forgotpassword.ForgotPasswordActivity
import com.app.atoz.ui.auth.verification.VerificationActivity
import com.app.atoz.ui.main.MainActivity
import com.app.atoz.ui.selectaccount.SelectAccountActivity
import com.app.atoz.utils.Config
import com.app.atoz.utils.ValidationUtils
import com.google.firebase.iid.FirebaseInstanceId
import com.google.gson.Gson
import timber.log.Timber
import javax.inject.Inject

class SignInActivity : BaseActivity(), SocailCallBackListener {

    companion object {
        private const val KEY_IS_SHOW_PROVIDER_MESSAGE = "ProviderMessage"

        fun start(context: Context, isShowProviderMessage: Boolean = false) {
            context.startActivity(
                Intent(context, SignInActivity::class.java)
                    .putExtra(KEY_IS_SHOW_PROVIDER_MESSAGE, isShowProviderMessage)
            )
        }
    }

    override fun getResource(): Int = R.layout.activity_signin

    private var mFCMToken: String = ""
    private lateinit var mBinding: ActivitySigninBinding

    private val mViewModel: SignInViewModel by lazy {
        ViewModelProviders
            .of(this, viewModelFactory)[SignInViewModel::class.java]
    }

    @Inject
    lateinit var userHolder: UserHolder
    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    override fun initView() {
        (application as AppApplication).mComponent.inject(this)
        lifecycle.addObserver(FacebookManager)



        mBinding = getBinding()
        mBinding.tvSignUp.text =
            getString(R.string.text_new_user)
                .highlightText(10, 17, ContextCompat.getColor(this, R.color.colorPrimary))
                .changeTextStyle(10, 17, ResourcesCompat.getFont(this, R.font.proximanova_bold)!!)
                .clickEvent(10, 17, false) {
                    SelectAccountActivity.start(this@SignInActivity)
                    finish()
                }
        mBinding.tvSignUp.movementMethod = LinkMovementMethod.getInstance()


        initFCMToken()

        initObserver()

        if (intent.getBooleanExtra(KEY_IS_SHOW_PROVIDER_MESSAGE, false))
            displayMessage(getString(R.string.text_error_provider_sign_up))
    }

    private fun initFCMToken() {
        FirebaseInstanceId.getInstance().instanceId
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    mFCMToken = task.result?.token.toString()
                }
                // Get new Instance ID token
            }
    }

    private fun initObserver() {
        mViewModel.getSignInRequest().observe(this@SignInActivity, Observer { it ->
            it?.let { requestState ->
                showLoadingIndicator(requestState.progress)
                requestState.data?.let {
                    Timber.d("Successfully done")
                    if (userHolder.isVerified) {
                        if (userHolder.isUserAsProvider && it.data != null && it.data!!.status == "pending") {
                            userHolder.clearData()
                            displayMessage(getString(R.string.text_error_provider_sign_up))
                        } else {
                            ActiveSubscriptionService.startService(this@SignInActivity)
                            MainActivity.start(this@SignInActivity)
                            finish()
                        }
                    } else {
                        VerificationActivity.start(this@SignInActivity)
                        finish()
                    }

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

        mViewModel.getSocialIdExist().observe(this@SignInActivity, Observer
        { it ->
            it?.let { requestState ->
                showLoadingIndicator(requestState.progress)
                requestState.data?.let {
                    Timber.d("Successfully done")

                    mViewModel.isSocialIdExist = it.data?.isExist!!
                    if (mViewModel.isSocialIdExist) {
                        callSignInSocialApi()

                    } else {
                        val socialLoginString = Gson().toJson(mViewModel.socialLoginModel)
                        SelectAccountActivity.start(this@SignInActivity, socialLoginString)
                        finish()
                    }
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

    override fun handleListener() {
        RxHelper.onClick(mBinding.btnSignIn, mDisposable) {
            hideKeyboard()
            if (checkValidation()) {
                mFCMToken.let { fcmToken ->
                    mViewModel.doSignIn(
                        isInternetConnected, this, mDisposable, mBinding.etEmail.text.toString(),
                        mBinding.etPassword.text.toString(), fcmToken
                    )
                }.let {
                    initFCMToken()
                }
            }
        }

        RxHelper.onClick(mBinding.tvFogotPassword, mDisposable) {
            ForgotPasswordActivity.start(this@SignInActivity)
        }

        RxHelper.onClick(mBinding.btnFacebook, mDisposable)
        {
            if (isInternetConnected) {
                FacebookManager.mListener = this
                FacebookManager.doLogin(this)
            } else {
                displayMessage(getString(R.string.text_error_network))
            }

        }

        RxHelper.onClick(mBinding.btnGooglePlus, mDisposable)
        {
            if (isInternetConnected) {
                GooglePlusManager.mListener = this
                GooglePlusManager.doLogin(this)
            } else {
                displayMessage(getString(R.string.text_error_network))
            }


        }

    }

    override fun onSocialSuccess(socialType: Int, statusCode: Int, data: Any) {
        when (socialType) {
            FacebookManager.SOCIAL_FACEBOOK -> {
                mViewModel.socialLoginModel = data as SocialLogin
                mViewModel.socialType = FacebookManager.SOCIAL_FACEBOOK
            }

            GooglePlusManager.SOCIAL_GOOGLE_PLUS -> {
                mViewModel.socialLoginModel = data as SocialLogin
                mViewModel.socialType = GooglePlusManager.SOCIAL_GOOGLE_PLUS
                GooglePlusManager.doLogout()

            }
        }
        mViewModel.socialLoginModel?.deviceToken = mFCMToken
        doCheckingSocialIdExist()
    }

    private fun doCheckingSocialIdExist() {
        mViewModel.doCheckingSocialIdExist(isInternetConnected, this, mDisposable)
    }

    private fun callSignInSocialApi() {

        mViewModel.doSocialLogin(isInternetConnected, this, mDisposable, mViewModel.socialLoginModel!!)

    }

    override fun onSocialCancel(socialType: Int, statusCode: Int, msg: String) {
        Timber.d("cancel social Login $msg")
//        displayMessage(msg)
    }

    override fun onSocialFailed(socialType: Int, statusCode: Int, errorMsg: String) {
        Timber.d("Failed Login $errorMsg")
        displayMessage(errorMsg)
    }

    override fun signout(success: Boolean) {

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == GooglePlusManager.RC_SIGN_IN) {
            GooglePlusManager.onActivityResult(requestCode, data!!)
        } else {
            FacebookManager.onActivityResult(requestCode, resultCode, data!!)
        }
    }


    override fun displayMessage(message: String) {
        mBinding.flRootView.snack(message)
    }

    private fun showLoadingIndicator(isShow: Boolean) {
        mBinding.progressBar.visibility = if (isShow) View.VISIBLE else View.GONE
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

        if (!ValidationUtils.isNotEmpty(mBinding.etPassword.text.toString())) {
            mBinding.tilPassword.isErrorEnabled = true
            mBinding.tilPassword.error = getString(R.string.text_error_empty_password)
            isValid = false
        } else {
            mBinding.tilPassword.isErrorEnabled = false
        }
        return isValid
    }


}