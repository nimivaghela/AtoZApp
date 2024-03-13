package com.app.atoz.ui.provider.existservice

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.app.atoz.models.CategorySelectionResponseModel
import com.app.atoz.models.RequestState
import com.app.atoz.models.UserHolder
import com.app.atoz.shareddata.base.BaseView
import com.app.atoz.shareddata.repo.ExistServiceRepo
import com.google.gson.JsonObject
import io.reactivex.disposables.CompositeDisposable
import okhttp3.MultipartBody
import javax.inject.Inject

class ExistServiceViewModel @Inject constructor(
    private val mExistServiceRepo: ExistServiceRepo, private val mUserHolder: UserHolder
) : ViewModel() {
    private val mLDCategoryRequest = MutableLiveData<RequestState<CategorySelectionResponseModel>>()
    private val mLDDeleteService = MutableLiveData<RequestState<Any>>()

    fun getDeleteServiceObserver() = mLDDeleteService

    fun getCategoryRequest(): LiveData<RequestState<CategorySelectionResponseModel>> = mLDCategoryRequest

    fun getCategoryList(
        isInternetConnected: Boolean,
        baseView: BaseView,
        disposable: CompositeDisposable
    ) {
        val body = JsonObject()
        body.addProperty("user_id", mUserHolder.userId)
        mExistServiceRepo.getProvidersExistService(body, isInternetConnected, baseView, disposable, mLDCategoryRequest)
    }

    fun callDeleteService(
        deleteServiceId: String,
        isInternetConnected: Boolean,
        baseView: BaseView,
        disposable: CompositeDisposable
    ) {
        val multipartBody = MultipartBody.Builder()
        multipartBody.setType(MultipartBody.FORM)
        multipartBody.addFormDataPart("id", mUserHolder.userId!!)
        multipartBody.addFormDataPart("service_ids[0]", deleteServiceId)

        mExistServiceRepo.addDeleteProviderServices(
            multipartBody.build(),
            isInternetConnected,
            baseView,
            disposable,
            mLDDeleteService
        )
    }
}