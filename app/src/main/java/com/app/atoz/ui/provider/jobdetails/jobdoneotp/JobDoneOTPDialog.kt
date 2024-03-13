package com.app.atoz.ui.provider.jobdetails.jobdoneotp

import android.annotation.SuppressLint
import android.text.Editable
import android.text.TextWatcher
import android.view.MotionEvent
import android.view.View
import android.widget.EditText
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import com.app.atoz.AppApplication
import com.app.atoz.R
import com.app.atoz.common.extentions.hideKeyboardFromDialog
import com.app.atoz.common.extentions.snack
import com.app.atoz.common.helper.RxHelper
import com.app.atoz.databinding.DialogJobDoneOtpBinding
import com.app.atoz.shareddata.base.BaseDialogFragment
import com.app.atoz.ui.auth.verification.VerificationNavigator
import com.app.atoz.utils.Config
import com.google.gson.JsonObject
import io.reactivex.disposables.CompositeDisposable
import timber.log.Timber
import javax.inject.Inject

@SuppressLint("ValidFragment")
class JobDoneOTPDialog(private val mID: String, private val mOtpStatus: OTPStatus) : BaseDialogFragment(),
    VerificationNavigator {

    companion object {
        const val JOB_DONE_OTP_DIALOG_TAG = "JobDoneOTPDialog"
        fun newInstance(id: String, otpStatus: OTPStatus): JobDoneOTPDialog {
            return JobDoneOTPDialog(id, otpStatus)
        }
    }

    @Inject
    lateinit var mViewModelFactory: ViewModelProvider.Factory
    private val mViewModel: JobDoneOtpViewModel by lazy {
        ViewModelProviders.of(this, mViewModelFactory)[JobDoneOtpViewModel::class.java]
    }
    private lateinit var mBinding: DialogJobDoneOtpBinding
    private lateinit var etOtpIds: Array<Int>

    override fun getResource(): Int = R.layout.dialog_job_done_otp

    override fun initViewModel() {
        (activity!!.application as AppApplication).mComponent.inject(this)
        initDisposable()
        mViewModel.setListener(this)
    }

    override fun postInit() {
        mBinding = getBinding()
        etOtpIds = arrayOf(mBinding.etOtpOne.id, mBinding.etOtpTwo.id, mBinding.etOtpThree.id, mBinding.etOtpFour.id)



        mViewModel.getOTPObserver().observe(this, Observer {
            it?.let { requestState ->
                showLoadingIndicator(requestState.progress)
                requestState.data?.let {

                    if (requestState.data.data?.get("is_exist")?.asBoolean!!) {
                        dismiss()
                        mOtpStatus.status(true)
                    } else {
                        displayMessage(getString(R.string.invalid_otp))
                    }


                }
                requestState.error?.let { errorObj ->
                    Timber.d("Successfully Error showing")
                    when (errorObj.errorState) {
                        Config.NETWORK_ERROR ->
                            displayMessage(getString(R.string.text_error_network))
                        Config.CUSTOM_ERROR ->
                            errorObj.customMessage
                                ?.let { it1 -> displayMessage(it1) }
                        else -> {
                        }
                    }
                    showLoadingIndicator(requestState.progress)

                }
            }
        })
    }

    override fun displayMessage(message: String) {
        mBinding.root.snack(message)
    }

    private fun showLoadingIndicator(progress: Boolean) {
        if (progress) {
            mBinding.includeProgress.visibility = View.VISIBLE
        } else {
            mBinding.includeProgress.visibility = View.GONE
        }

    }

    override fun handleListener() {
        addTextWatchers()
        RxHelper.onClick(mBinding.btnSubmit, mDisposable) {
            dialog?.hideKeyboardFromDialog()
            concatOTP().let {
                if (it != null) {
                    if (it.length == 4) {
                        checkOTP(it)
                    } else {
                        displayMessage(getString(R.string.otp_message))
                    }
                }
            }
        }
    }

    private fun checkOTP(otp: String?) {
        val bodyData = JsonObject()
        bodyData.addProperty("id", mID)
        bodyData.addProperty("otp", otp)

        mViewModel.checkOTP(bodyData, isInternetConnected, this, mDisposable)
    }

    private fun concatOTP(): String? {
        val otp = StringBuilder()
        otp.append(mBinding.etOtpOne.text.toString())
        otp.append(mBinding.etOtpTwo.text.toString())
        otp.append(mBinding.etOtpThree.text.toString())
        otp.append(mBinding.etOtpFour.text.toString())

        return otp.toString()
    }


    override fun onRequestFocus(position: Int) {
        when (position) {
            in 0..3 -> requestFocus(dialog?.findViewById(etOtpIds[position]))
            4 -> dialog?.hideKeyboardFromDialog()
        }
    }

    private fun addTextWatchers() {
        for (id in etOtpIds) {
            dialog?.apply {
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
        et?.apply {
            setSelection(length())
            requestFocus()
        }
    }

    override fun onStatusChange(status: Int, msg: Any) {

    }

    override fun fillOTPRuntime() {

    }


    private fun initDisposable() {
        mDisposable = CompositeDisposable()
    }

    interface OTPStatus {
        fun status(status: Boolean)
    }
}