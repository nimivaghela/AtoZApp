package com.app.atoz.ui.auth.verification

import android.content.Context
import android.content.Intent
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import com.app.atoz.AppApplication
import com.app.atoz.R
import com.app.atoz.common.extentions.hideKeyboard
import com.app.atoz.common.extentions.resToast
import com.app.atoz.common.extentions.visible
import com.app.atoz.common.helper.RxHelper
import com.app.atoz.databinding.ActivityVerificationBinding
import com.app.atoz.models.UserHolder
import com.app.atoz.shareddata.base.BaseActivity
import com.app.atoz.ui.auth.signin.SignInActivity
import com.app.atoz.ui.main.MainActivity
import com.app.atoz.utils.Config
import com.google.firebase.auth.PhoneAuthProvider
import timber.log.Timber
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class VerificationActivity : BaseActivity(), VerificationNavigator {

    companion object {
        fun start(context: Context) {
            val verificationIntent = Intent(context, VerificationActivity::class.java)
            verificationIntent.flags =
                Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
            context.startActivity(verificationIntent)
        }
    }

    override fun getResource(): Int = R.layout.activity_verification

    private lateinit var etOtpIds: Array<Int>
    private lateinit var mBinding: ActivityVerificationBinding

    @Inject
    lateinit var mUserHolder: UserHolder
    private lateinit var mPhoneNumber: String

    private val mViewModel: VerificationViewModel by lazy {
        ViewModelProviders
            .of(this, viewModelFactory)[VerificationViewModel::class.java]
    }

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    override fun initView() {
        (application as AppApplication).mComponent.inject(this)
        mBinding = getBinding()

        setToolbar(mBinding.verificationToolbar.toolbar, "", true, R.color.white)
        mViewModel.setListener(this)
        etOtpIds = arrayOf(
            mBinding.etOtpOne.id,
            mBinding.etOtpTwo.id,
            mBinding.etOtpThree.id,
            mBinding.etOtpFour.id,
            mBinding.etOtpFive.id,
            mBinding.etOtpSix.id
        )
        mPhoneNumber = mUserHolder.phone.let {
            if (it!!.startsWith("+91")) {
                it
            } else {
                "+91$it"
            }
        }
        verifyPhoneNumber(mPhoneNumber)
        initObservable()
    }

    override fun onBackPressed() {
        SignInActivity.start(this)
        super.onBackPressed()
    }

    private fun initObservable() {
        mViewModel.getVerificationRequest().observe(this@VerificationActivity, Observer { it ->
            it?.let { requestState ->
                Timber.d("Verification Successfully done $requestState")
                showLoadingIndicator(requestState.progress)
                requestState.data?.let {
                    Timber.d("Verification Successfully done")
                    if (mUserHolder.isUserAsProvider) {
                        mUserHolder.clearData()
                        SignInActivity.start(this, true)
                        finish()
                    } else {
                        MainActivity.start(this@VerificationActivity)
                    }
                }
                requestState.error?.let { errorObj ->
                    Timber.d("Verification Successfully Error showing")
                    when (errorObj.errorState) {
                        Config.NETWORK_ERROR ->
                            displayMessage(getString(R.string.text_error_network))
                        else -> {
                            errorObj.customMessage
                                ?.let { displayMessage(it) }
                        }
                    }
                }
            }
        })
    }

    private fun showLoadingIndicator(isShow: Boolean) {
        mBinding.progressBar.visibility = if (isShow) View.VISIBLE else View.GONE
    }

    private fun verifyPhoneNumber(phoneNumber: String) {
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
            phoneNumber,      // Phone number to verify
            60,               // Timeout duration
            TimeUnit.SECONDS, // Unit of timeout
            this,             // Activity (for callback binding)
            mViewModel.registerOnVerificationStateChangedListener()
        ) // OnVerificationStateChangedCallbacks
        mViewModel.verificationInProgress = true
    }

    override fun handleListener() {
        addTextWatchers()

        RxHelper.onClick(mBinding.btnSubmit, mDisposable) {
            val code = mViewModel.getEnteredCode()
            if (TextUtils.isEmpty(code)) {
                displayMessage(getString(R.string.text_error_msg_empty_otp))
                return@onClick
            } else if (code.length != 6) {
                displayMessage(getString(R.string.text_error_msg_invalid_otp))
                return@onClick
            }
            mViewModel.verifyPhoneNumberWithCode()

        }

        RxHelper.onClick(mBinding.btnResendOtp, mDisposable) {
            resendVerificationCode(mPhoneNumber, mViewModel.resendToken)
        }

    }

    override fun onRequestFocus(position: Int) {
        when (position) {
            in 0..5 -> requestFocus(findViewById(etOtpIds[position]))
            6 -> hideKeyboard()
        }
    }

    override fun onStatusChange(status: Int, msg: Any) {
        when (status) {
            VerificationViewModel.STATE_CODE_SENT -> {

                if (mViewModel.resendToken != null) {
                    displayMessage(getString(R.string.text_otp_sent))
                } else {
                    displayMessage(getString(R.string.text_otp_resent))
                }
                mBinding.btnResendOtp.visible()

            }
            VerificationViewModel.STATE_VERIFY_SUCCESS -> {
                mViewModel.doVerification(isInternetConnected, this, mDisposable)
            }

            VerificationViewModel.STATE_VERIFY_FAILED -> {
                displayMessage(msg as String)
            }
        }
    }

    private fun addTextWatchers() {
        for (id in etOtpIds) {
            findViewById<EditText>(id).run {
                setOnTouchListener { _, event ->
                    if (event.action == MotionEvent.ACTION_DOWN) {
                        requestFocus(findViewById<EditText>(etOtpIds[mViewModel.getFocusPosition()]))
                        return@setOnTouchListener true
                    } else return@setOnTouchListener false

                }
                applyTextWatchers(this)
            }
        }
    }

    override fun fillOTPRuntime() {
        for ((index, id) in etOtpIds.withIndex()) {
            findViewById<EditText>(id).run {
                setText(mViewModel.getEnteredCode()[index].toString())
            }
        }
    }

    private fun applyTextWatchers(et: EditText) {
        et.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                if (s != null) {
                    mViewModel.onCodeTextChange(getEditTextPosition(et), s.toString())
                }
            }
        })
    }

    fun getEditTextPosition(et: EditText): Int {
        etOtpIds.forEachIndexed { index, i ->
            if (i == et.id) return index
        }
        return -1
    }

    private fun requestFocus(et: EditText?) {
        (getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager)
            .showSoftInput(et, InputMethodManager.SHOW_FORCED)
        et?.apply {
            setSelection(length())
            requestFocus()
        }
    }

    // START resend_verification
    private fun resendVerificationCode(
        phoneNumber: String,
        token: PhoneAuthProvider.ForceResendingToken?
    ) {
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
            phoneNumber, // Phone number to verify
            60, // Timeout duration
            TimeUnit.SECONDS, // Unit of timeout
            this, // Activity (for callback binding)
            mViewModel.registerOnVerificationStateChangedListener(), // OnVerificationStateChangedCallbacks
            token
        ) // ForceResendingToken from callbacks
    }

    override fun displayMessage(message: String) {
        resToast(message)
    }
}