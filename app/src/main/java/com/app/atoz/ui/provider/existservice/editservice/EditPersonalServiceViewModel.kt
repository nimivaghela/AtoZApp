package com.app.atoz.ui.provider.existservice.editservice

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.app.atoz.models.CategorySelectionResponseModel
import com.app.atoz.models.RequestState
import com.app.atoz.models.UserHolder
import com.app.atoz.shareddata.base.BaseView
import com.app.atoz.shareddata.repo.ExistServiceRepo
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import io.reactivex.disposables.CompositeDisposable
import javax.inject.Inject

class EditPersonalServiceViewModel @Inject constructor(
    private val mExistServiceRepo: ExistServiceRepo,
    private val mUserHolder: UserHolder
) : ViewModel() {
    private var mServiceId: String? = null
    private val mLDChildServices = MutableLiveData<RequestState<CategorySelectionResponseModel>>()
    private val mLDPriceChange = MutableLiveData<RequestState<Any>>()

    fun getChildServiceObserver(): LiveData<RequestState<CategorySelectionResponseModel>> = mLDChildServices

    fun getPriceChangeObserver(): LiveData<RequestState<Any>> = mLDPriceChange

    fun setServiceId(serviceId: String) {
        mServiceId = serviceId
    }

    fun callChildService(isInternetConnected: Boolean, baseView: BaseView, disposable: CompositeDisposable) {
        mServiceId?.let {
            val bodyData = JsonObject()
            bodyData.addProperty("parent_id", mServiceId)
            bodyData.addProperty("user_id", mUserHolder.userId)
            mExistServiceRepo.getChildServices(bodyData, isInternetConnected, baseView, disposable, mLDChildServices)
        }
    }

    fun changePriceOfServiceItem(
        serviceId: String,
        newPrice: String,
        locationId: String?,
        isInternetConnected: Boolean,
        baseView: BaseView,
        disposable: CompositeDisposable
    ) {
        val bodyData = JsonObject()
        bodyData.addProperty("user_id", mUserHolder.userId)
        bodyData.addProperty("location_id", locationId)
        val priceArray = JsonArray()
        val priceObject = JsonObject()
        priceObject.addProperty("service_id", serviceId)
        priceObject.addProperty("price", newPrice)
        priceArray.add(priceObject)
        bodyData.add("prices", priceArray)

        mExistServiceRepo.sendProviderPriceChangeRequest(
            bodyData,
            isInternetConnected,
            baseView,
            disposable,
            mLDPriceChange
        )
    }
}