package com.app.atoz.ui.auth.verification

import android.os.Handler
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.app.atoz.AppApplication
import com.app.atoz.R
import com.app.atoz.models.RequestState
import com.app.atoz.models.UserHolder
import com.app.atoz.models.VerificationResponse
import com.app.atoz.shareddata.base.BaseView
import com.app.atoz.shareddata.repo.VerificationRepo
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthProvider
import com.google.gson.JsonObject
import io.reactivex.disposables.CompositeDisposable
import timber.log.Timber
import javax.inject.Inject
import com.google.firebase.auth.FirebaseAuth


class VerificationViewModel @Inject constructor(private val mVerificationRepo: VerificationRepo) : ViewModel() {

    companion object {
        const val STATE_VERIFY_SUCCESS = 1001
        const val STATE_VERIFY_FAILED = 1002
        const val STATE_CODE_SENT = 1003
    }

    @Inject
    lateinit var mUserHolder: UserHolder
    private var mListener: VerificationNavigator? = null
    var mCodeArray = Array<String?>(6) { null }
    private lateinit var callbacks: PhoneAuthProvider.OnVerificationStateChangedCallbacks
    var storedVerificationId: String? = null
    var resendToken: PhoneAuthProvider.ForceResendingToken? = null
    var verificationInProgress = false

    private val mLDVerificationRequest = MutableLiveData<RequestState<VerificationResponse>>()

    fun getVerificationRequest() = mLDVerificationRequest

    fun doVerification(
        isInternetConnected: Boolean,
        baseView: BaseView,
        disposable: CompositeDisposable
    ) {
        val verificationJson = JsonObject()
        verificationJson.addProperty("id", mUserHolder.userId)
        verificationJson.addProperty("status", true)

        mVerificationRepo.verification(
            verificationJson,
            isInternetConnected,
            baseView,
            disposable,
            mLDVerificationRequest
        )
    }



    fun setListener(listener: VerificationNavigator) {
        mListener = listener
    }

    fun onCodeTextChange(pos: Int, value: String) {
        if (pos >= 0) {
            if (value.isEmpty()) {
                mCodeArray[pos] = null
                mListener?.onRequestFocus(pos - 1)
            } else {
                mCodeArray[pos] = value
                mListener?.onRequestFocus(pos + 1)
            }
        }
    }

    fun getFocusPosition(): Int {
        mCodeArray.forEachIndexed { index, it ->
            if (it == null)
                return index
        }
        return mCodeArray.size - 1
    }


    fun getEnteredCode(): String {
        val sb = StringBuilder()
        mCodeArray.forEach {
            sb.append(it)
        }

        return sb.toString()
    }

    fun registerOnVerificationStateChangedListener(): PhoneAuthProvider.OnVerificationStateChangedCallbacks {

        if (::callbacks.isInitialized)
            return callbacks

        // Initialize phone auth callbacks
        // [START phone_auth_callbacks]
        callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                // This callback will be invoked in two situations:
                // 1 - Instant verification. In some cases the phone number can be instantly
                //     verified without needing to send or enter a verification code.
                // 2 - Auto-retrieval. On some devices Google Play services can automatically
                //     detect the incoming verification SMS and perform verification without
                //     user action.

                credential.smsCode?.run {
                    mCodeArray = Array(this.length) {
                        null
                    }
                    this.forEachIndexed { index, c ->
                        mCodeArray[index] = c.toString()
                    }
                    mListener?.fillOTPRuntime()
                }
                Timber.d("onVerificationCompleted:$credential")
                // [START_EXCLUDE silent]
                verificationInProgress = false
                // [END_EXCLUDE]

                Handler().postDelayed({
//                    mListener?.onStatusChange(STATE_VERIFY_SUCCESS, credential)
                    verifyPhoneNumberWithCode()
                }, 200)
            }

            override fun onVerificationFailed(e: FirebaseException) {


                // This callback is invoked in an invalid request for verification is made,
                // for instance if the the phone number format is not valid.
                Timber.w("onVerificationFailed $e")
                // [START_EXCLUDE silent]
                verificationInProgress = false
                // [END_EXCLUDE]

                val errorMsg = when (e) {
                    is FirebaseAuthInvalidCredentialsException -> // Invalid request
                        AppApplication.instance.getString(com.app.atoz.R.string.text_error_invalid_phone_number)
                    is FirebaseTooManyRequestsException ->
                        AppApplication.instance.getString(R.string.text_error_quota_exceeded)
                    else -> e.localizedMessage
                }
                mListener?.onStatusChange(STATE_VERIFY_FAILED, errorMsg)

            }

            override fun onCodeSent(
                verificationId: String,
                token: PhoneAuthProvider.ForceResendingToken
            ) {
                // The SMS verification code has been sent to the provided phone number, we
                // now need to ask the user to enter the code and then construct a credential
                // by combining the code with a verification ID.
                Timber.d("onCodeSent: $verificationId")

                // Save verification ID and resending token so we can use them later
                storedVerificationId = verificationId
                resendToken = token


                // [START_EXCLUDE]
                // Update UI
                mListener?.onStatusChange(STATE_CODE_SENT, "onCodeSent")
                // [END_EXCLUDE]
            }
        }
        return callbacks
    }

    fun verifyPhoneNumberWithCode() {

        val enterOtp = getEnteredCode()
        val credential = storedVerificationId?.let { PhoneAuthProvider.getCredential(it, enterOtp) }

        if (credential != null) {

           /* Timber.d("OTP code ${credential.smsCode}")
            if (enterOtp == credential.smsCode) {
                mListener?.onStatusChange(STATE_VERIFY_SUCCESS, credential)
            } else {

            }*/
            authenticationProcess(credential)
        } else {
            mListener?.onStatusChange(
                STATE_VERIFY_FAILED,
                AppApplication.instance.getString(R.string.text_error_internal_server)
            )
        }
    }
    private fun authenticationProcess(credential :PhoneAuthCredential)
    {
        val mAuth = FirebaseAuth.getInstance()
        mAuth.signInWithCredential(credential).addOnCompleteListener {
            if (it.isSuccessful()) {
                //verification successful we will start the profile activity
                mListener?.onStatusChange(STATE_VERIFY_SUCCESS, credential)

            } else {
                //verification unsuccessful.. display an error message

                var  resId = R.string.text_error_internal_server

                if (it.getException() is FirebaseAuthInvalidCredentialsException) {

                    resId = R.string.text_error_invalid_otp_code
                }

                mListener?.onStatusChange(
                    STATE_VERIFY_FAILED,
                    AppApplication.instance.getString(resId)
                )

            }

        }
    }
}