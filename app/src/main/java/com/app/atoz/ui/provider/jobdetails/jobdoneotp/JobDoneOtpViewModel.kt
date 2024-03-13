package com.app.atoz.ui.provider.jobdetails.jobdoneotp

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.app.atoz.models.RequestState
import com.app.atoz.shareddata.base.BaseView
import com.app.atoz.ui.auth.verification.VerificationNavigator
import com.google.gson.JsonObject
import io.reactivex.disposables.CompositeDisposable
import javax.inject.Inject

class JobDoneOtpViewModel @Inject constructor(private val jobDoneOTPRepo: JobDoneOTPRepo) : ViewModel() {
    private var mListener: VerificationNavigator? = null
    private val mCodeArray = Array<String?>(4) { null }

    private val mLdOTPData: MutableLiveData<RequestState<JsonObject>> by lazy {
        MutableLiveData<RequestState<JsonObject>>()
    }

    fun getOTPObserver(): MutableLiveData<RequestState<JsonObject>> {
        return mLdOTPData
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

    fun checkOTP(
        bodyData: JsonObject,
        isInternetConnected: Boolean,
        baseView: BaseView,
        disposable: CompositeDisposable

    ) {
        jobDoneOTPRepo.checkOTP(bodyData,isInternetConnected, baseView, disposable, mLdOTPData)
    }
}