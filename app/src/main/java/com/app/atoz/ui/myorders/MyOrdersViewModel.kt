package com.app.atoz.ui.myorders

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.app.atoz.models.*
import com.app.atoz.shareddata.base.BaseView
import com.app.atoz.shareddata.repo.JobRepository
import com.app.atoz.shareddata.repo.MyOrderRepo
import com.app.atoz.ui.provider.jobdetails.JobBillAdapter
import com.google.gson.JsonObject
import io.reactivex.disposables.CompositeDisposable
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import javax.inject.Inject

class MyOrdersViewModel @Inject constructor(
    private val myUserOrderRepo: MyOrderRepo,
    val mUserHolder: UserHolder,
    private val jobRepo: JobRepository
) : ViewModel() {


    companion object {
        const val TYPE_ORDER_ALL = "0"
        const val TYPE_ORDER_PENDING = "1"
        const val TYPE_ORDER_INPROGRESS = "3"
        const val TYPE_ORDER_CONFIRMED = "2"
        const val TYPE_ORDER_COMPLETED = "4"
        const val TYPE_ORDER_CANCELLED = "5"
        const val TYPE_ORDER_CANCELLED_BY_SYSTEM = "6"
        const val TYPE_ORDER_BILL_UPLOADED = "7"
        const val TYPE_ORDER_PAYMENT_DONE = "8"
    }

    private val mLDOrderList = MutableLiveData<RequestState<UserServiceStatusList>>()
    internal val mLDOrderDetail = MutableLiveData<RequestState<OrderDetailModel>>()
    private val mLDChangeOrderStatus = MutableLiveData<RequestState<Any>>()
    val mBillList = MutableLiveData<ArrayList<JobBillModel>>()
    private val mLdCompleteOrder = MutableLiveData<RequestState<Any>>()
    private val mLdUploadOrderBill =
        MutableLiveData<RequestState<List<OrderDetailModel.ServiceRequest.ServiceRequestImage>>>()
    private val mLDVerifyCouponCode = MutableLiveData<RequestState<CouponCode>>()
    private val mLDCallOtp = MutableLiveData<RequestState<CallModel>>()
    var mOrderList = ArrayList<UserServiceStatusList.ServiceResponse>()

    var mOrderType = TYPE_ORDER_ALL
    var mServiceId: String = ""
    var mPhoneNumber: String = ""
    var mDiscountInPercentage: Int? = null
    var mDiscountCodeId: String? = null
    var mDiscountAmount: Float = 0f

    init {
        val list = ArrayList<JobBillModel>()
        list.add(JobBillModel(null, JobBillAdapter.TYPE_ADD_BILL))
        mBillList.postValue(list)
    }

    fun getOrderStatusList() = mLDOrderList
    fun getOrderDetail() = mLDOrderDetail
    fun getChangeOrderStatusObserver() = mLDChangeOrderStatus
    fun getCompleteOrderObserver() = mLdCompleteOrder
    fun getUploadOrderBillObserver() = mLdUploadOrderBill
    fun getVerifyCouponCode() = mLDVerifyCouponCode
    fun getCallOtpObserver(): LiveData<RequestState<CallModel>> = mLDCallOtp

    fun setDiscountData(
        serviceRequest: OrderDetailModel.ServiceRequest,
        discountInPercentage: Int, discountCodeId: String
    ) {
        val serviceTotal = getServiceTotal(serviceRequest.requestedItemList)
        mDiscountInPercentage = discountInPercentage
        mDiscountCodeId = discountCodeId
        mDiscountAmount = (serviceTotal * discountInPercentage) / 100
    }

    fun addBill(file: File) {
        val list = mBillList.value
        list?.add(0, JobBillModel(file, JobBillAdapter.TYPE_VIEW_VIEW))
        mBillList.postValue(list)
    }

    fun getOrderStatusList(
        orderStatus: String,
        isInternetConnected: Boolean,
        baseView: BaseView,
        disposable: CompositeDisposable
    ) {
        if (mUserHolder.isUserAsProvider) {
            myUserOrderRepo.getProviderOrderStatusList(
                orderStatus,
                isInternetConnected,
                baseView,
                disposable,
                mLDOrderList
            )

        } else {
            myUserOrderRepo.getUserOrderStatusList(
                orderStatus,
                isInternetConnected,
                baseView,
                disposable,
                mLDOrderList
            )
        }
    }

    fun getOrderDetail(
        orderId: String, isInternetConnected: Boolean,
        baseView: BaseView,
        disposable: CompositeDisposable
    ) {
        if (mUserHolder.isUserAsProvider) {
            myUserOrderRepo.getJobDetail(
                orderId,
                isInternetConnected,
                baseView,
                disposable,
                mLDOrderDetail
            )
        } else {
            myUserOrderRepo.getUserOrderDetail(
                orderId,
                isInternetConnected,
                baseView,
                disposable,
                mLDOrderDetail
            )
        }
    }

    fun doRequestChangeStatus(
        requestChangeInput: RequestChangeInput, isInternetConnected: Boolean,
        baseView: BaseView,
        disposable: CompositeDisposable
    ) {
        myUserOrderRepo.getChangeOrderStatus(
            requestChangeInput,
            isInternetConnected,
            baseView,
            disposable,
            mLDChangeOrderStatus
        )
    }

    fun uploadBillAndTotalPrice(
        totalAmount: String,
        isInternetConnected: Boolean,
        baseView: BaseView,
        disposable: CompositeDisposable
    ) {
        val multipartBody = MultipartBody.Builder()
        multipartBody.setType(MultipartBody.FORM)
        if (mServiceId.isNotEmpty()) {
            multipartBody.addFormDataPart("id", mServiceId)
            multipartBody.addFormDataPart("status", TYPE_ORDER_BILL_UPLOADED)
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

    fun completeJob(
        isInternetConnected: Boolean,
        baseView: BaseView,
        disposable: CompositeDisposable
    ) {

        val multipartBody = MultipartBody.Builder()
        multipartBody.setType(MultipartBody.FORM)

        if (mServiceId.isNotEmpty()) {
            multipartBody.addFormDataPart("id", mServiceId)
            multipartBody.addFormDataPart("status", TYPE_ORDER_COMPLETED)
            multipartBody.addFormDataPart("provider_id", mUserHolder.userId!!)

            jobRepo.completeJob(
                multipartBody.build(),
                isInternetConnected,
                baseView,
                disposable,
                mLdCompleteOrder
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
        subscriptionDiscount: Float
    ): Float = getActualTotalAmount(
        totalEnteredBillAmountByProvider,
        serviceTotal
    ) - mDiscountAmount - subscriptionDiscount

    fun getActualTotalAmount(
        totalEnteredBillAmountByProvider: String?,
        serviceTotal: Float
    ): Float {
        val totalBillAmount = totalEnteredBillAmountByProvider?.toFloat() ?: 0f
        return totalBillAmount + serviceTotal
    }

    fun doVerifyCouponCode(
        enteredCode: String, orderId: String?,
        isInternetConnected: Boolean, baseView: BaseView, disposable: CompositeDisposable
    ) {
        val body = JsonObject()
        body.addProperty("code", enteredCode)
        body.addProperty("user_id", mUserHolder.userId)
        body.addProperty("order_id", orderId)
        myUserOrderRepo.verifyCouponCode(
            body,
            isInternetConnected,
            baseView,
            disposable,
            mLDVerifyCouponCode
        )
    }

    fun getOtpForCall(
        isInternetConnected: Boolean,
        baseView: BaseView,
        disposable: CompositeDisposable
    ) {
        getOrderDetail().value?.data?.data?.serviceRequest?.user?.id?.toString()?.let { userId ->
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
