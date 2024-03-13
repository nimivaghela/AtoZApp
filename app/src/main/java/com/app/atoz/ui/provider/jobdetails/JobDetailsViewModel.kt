package com.app.atoz.ui.provider.jobdetails

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.app.atoz.models.*
import com.app.atoz.shareddata.base.BaseView
import com.app.atoz.shareddata.repo.JobRepository
import com.app.atoz.shareddata.repo.MyOrderRepo
import com.app.atoz.ui.myorders.MyOrdersViewModel
import com.app.atoz.ui.myorders.MyOrdersViewModel.Companion.TYPE_ORDER_ALL
import com.app.atoz.ui.myorders.MyOrdersViewModel.Companion.TYPE_ORDER_COMPLETED
import com.google.gson.JsonObject
import io.reactivex.disposables.CompositeDisposable
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import javax.inject.Inject

class JobDetailsViewModel @Inject constructor(
    private val jobRepo: JobRepository,
    private var myUserOrderRepo: MyOrderRepo,
    private val mUserHolder: UserHolder
) : ViewModel() {

    val mBillList = MutableLiveData<ArrayList<JobBillModel>>()

    var mJobId: String? = null
    var requestStatus: String? = null

    private val mLDJobDetail = MutableLiveData<RequestState<OrderDetailModel>>()
    private val mLdCompleteOrder = MutableLiveData<RequestState<Any>>()

    private val mLdUploadOrderBill =
        MutableLiveData<RequestState<List<OrderDetailModel.ServiceRequest.ServiceRequestImage>>>()

    private val mLDCallOtp = MutableLiveData<RequestState<CallModel>>()

    fun getUploadOrderBillObserver() = mLdUploadOrderBill

    fun getJobDetail() = mLDJobDetail

    private val mLDChangeOrderStatus = MutableLiveData<RequestState<Any>>()

    fun getChangeOrderStatusObserver() = mLDChangeOrderStatus

    fun getCompleteOrderObserver() = mLdCompleteOrder

    fun getCallOtpObserver(): LiveData<RequestState<CallModel>> = mLDCallOtp

    var serviceId: String = ""
    var phoneNumber: String = ""

    var mOrderType = TYPE_ORDER_ALL

    fun getJobDetail(
        jobId: String, isInternetConnected: Boolean,
        baseView: BaseView,
        disposable: CompositeDisposable
    ) {
        jobRepo.getJobDetail(jobId, isInternetConnected, baseView, disposable, mLDJobDetail)
    }


    init {
        val list = ArrayList<JobBillModel>()
        list.add(JobBillModel(null, JobBillAdapter.TYPE_ADD_BILL))
        mBillList.postValue(list)
    }

    fun addBill(file: File) {
        val list = mBillList.value
        list?.add(0, JobBillModel(file, JobBillAdapter.TYPE_VIEW_VIEW))
        mBillList.postValue(list)
    }

    fun doRequestChangeStatus(
        requestChangeInput: RequestChangeInput, isInternetConnected: Boolean,
        baseView: BaseView,
        disposable: CompositeDisposable
    ) {
        jobRepo.getChangeOrderStatus(
            requestChangeInput,
            isInternetConnected,
            baseView,
            disposable,
            mLDChangeOrderStatus
        )
    }

    fun completeJob(
        isInternetConnected: Boolean,
        baseView: BaseView,
        disposable: CompositeDisposable
    ) {

        val multipartBody = MultipartBody.Builder()
        multipartBody.setType(MultipartBody.FORM)

        mJobId?.let {
            multipartBody.addFormDataPart("id", it)
            multipartBody.addFormDataPart("status", TYPE_ORDER_COMPLETED)
            multipartBody.addFormDataPart("provider_id", mUserHolder.userId!!)

//            mBillList.value?.let {
//                for ((index, fileModel) in it.withIndex()) {
//                    fileModel.file?.let { file ->
//                        multipartBody.addFormDataPart(
//                            "medias[$index]", file.name,
//                            RequestBody.create(MediaType.parse("image/*"), file)
//                        )
//                    }
//                }
//
//            }

            jobRepo.completeJob(
                multipartBody.build(),
                isInternetConnected,
                baseView,
                disposable,
                mLdCompleteOrder
            )
        }
    }

    fun uploadBillAndTotalPrice(
        totalAmount: String,
        isInternetConnected: Boolean,
        baseView: BaseView,
        disposable: CompositeDisposable
    ) {
        val multipartBody = MultipartBody.Builder()
        multipartBody.setType(MultipartBody.FORM)
        if (serviceId.isNotEmpty()) {
            multipartBody.addFormDataPart("id", serviceId)
            multipartBody.addFormDataPart("status", MyOrdersViewModel.TYPE_ORDER_BILL_UPLOADED)
            multipartBody.addFormDataPart("provider_id", mUserHolder.userId!!)
            multipartBody.addFormDataPart("total_bill_amount", totalAmount)
            mBillList.value?.let {
                for ((index, fileModel) in it.withIndex()) {
                    fileModel.file?.let { file ->
                        multipartBody.addFormDataPart(
                            "medias[$index]", file.name,
                            file.asRequestBody("image/*".toMediaTypeOrNull())
                        )
                    }
                }
            }
            myUserOrderRepo.uploadBill(
                multipartBody.build(),
                isInternetConnected,
                baseView,
                disposable,
                mLdUploadOrderBill
            )
        }
    }

    fun getServiceTotal(requestedItemList: ArrayList<RequestItemList>?): Float {
        return requestedItemList?.run {
            var total = 0f
            for (requestItem in requestedItemList) {
                total += requestItem.servicePrice
            }
            total
        } ?: let { 0f }
    }

    fun getTotalAmount(
        totalEnteredBillAmountByProvider: String?,
        serviceTotal: Float,
        discount: Float,
        subscriptionDiscount: Float
    ): Float = getActualTotalAmount(
        totalEnteredBillAmountByProvider,
        serviceTotal
    ) - discount - subscriptionDiscount


    fun getActualTotalAmount(
        totalEnteredBillAmountByProvider: String?,
        serviceTotal: Float
    ): Float {
        val totalBillAmount = totalEnteredBillAmountByProvider?.toFloat() ?: 0f
        return totalBillAmount + serviceTotal
    }

    fun getOtpForCall(
        isInternetConnected: Boolean,
        baseView: BaseView,
        disposable: CompositeDisposable
    ) {
        getJobDetail().value?.data?.data?.serviceRequest?.user?.id?.toString()?.let { userId ->
            val bodyData = JsonObject()
            bodyData.addProperty("user_id", userId)
            myUserOrderRepo.getOtpForCall(
                bodyData,
                isInternetConnected,
                baseView,
                disposable,
                mLDCallOtp
            )
        }
    }
}