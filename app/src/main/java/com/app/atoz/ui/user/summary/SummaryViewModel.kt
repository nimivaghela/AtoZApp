package com.app.atoz.ui.user.summary

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.app.atoz.models.RequestState
import com.app.atoz.models.ServiceRequest
import com.app.atoz.models.SummaryResponseModel
import com.app.atoz.shareddata.base.BaseView
import com.app.atoz.shareddata.repo.SummaryRepo
import io.reactivex.disposables.CompositeDisposable
import javax.inject.Inject

class SummaryViewModel @Inject constructor(private val mSummaryRepo: SummaryRepo) : ViewModel() {
    private lateinit var mRequestId: String

    private val mLDSummaryRequest = MutableLiveData<RequestState<SummaryResponseModel>>()

    fun setRequestId(requestId: String?) {
        mRequestId = requestId!!
    }

    fun getTotalAmount(): Float {
        var total = 0f
        val serviceRequest: ServiceRequest? = mLDSummaryRequest.value?.data?.data?.serviceRequest
        serviceRequest?.apply {
            for (item in requestItemList) {
                total += item.servicePrice
            }
            total += serviceCharge
            total -= offerDiscount
            if (total < 0) {
                total = 0f
            }
        }
        return total
    }

    fun getSummaryRequest(): LiveData<RequestState<SummaryResponseModel>> = mLDSummaryRequest

    fun getSummaryData(
        isInternetConnected: Boolean,
        baseView: BaseView,
        disposable: CompositeDisposable
    ) {
        if (::mRequestId.isInitialized) {
            mSummaryRepo.getSummaryData(mRequestId, isInternetConnected, baseView, disposable, mLDSummaryRequest)
        }
    }
}