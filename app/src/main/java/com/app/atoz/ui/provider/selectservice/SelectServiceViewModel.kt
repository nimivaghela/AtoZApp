package com.app.atoz.ui.provider.selectservice

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.app.atoz.models.*
import com.app.atoz.shareddata.base.BaseView
import com.app.atoz.shareddata.repo.SignUpRepo
import com.google.gson.JsonObject
import io.reactivex.disposables.CompositeDisposable
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import javax.inject.Inject

class SelectServiceViewModel @Inject constructor(private val mSignUpRepo: SignUpRepo) :
    ViewModel() {
    private val mLDSignUpRequest = MutableLiveData<RequestState<SignUpSignInResponse>>()
    private val mLDCategoryRequest = MutableLiveData<RequestState<CategorySelectionResponseModel>>()

    var mSignUpRequestModel: SignUpRequestModel? = null
    var mSelectedServicesId = ArrayList<Int>()

    fun countCheckedServices(): Int {
        mSelectedServicesId.clear()
        val categoryList: RequestState<CategorySelectionResponseModel>? = mLDCategoryRequest.value
        categoryList?.data?.let { requestData ->
            requestData.data?.let { response ->
                val categories: ArrayList<CategoryModel> = response.service
                categories
                    .filter { !it.serviceList.isNullOrEmpty() }
                    .flatMap { it.serviceList!! }
                    .filter { it.selected }
                    .forEach { mSelectedServicesId.add(it.serviceId) }
            }
        }
        return mSelectedServicesId.size
    }

    fun getSignUpRequest(): LiveData<RequestState<SignUpSignInResponse>> = mLDSignUpRequest

    fun getCategoryRequest(): LiveData<RequestState<CategorySelectionResponseModel>> =
        mLDCategoryRequest

    fun doSignUpForProvider(
        isInternetConnected: Boolean,
        baseView: BaseView,
        disposable: CompositeDisposable
    ) {
        mSignUpRequestModel?.apply {
            val multipartBody = MultipartBody.Builder()
            multipartBody.setType(MultipartBody.FORM)
            multipartBody.addFormDataPart("firstname", firstName!!)
            multipartBody.addFormDataPart("lastname", lastName!!)
            multipartBody.addFormDataPart("email", email!!)
            multipartBody.addFormDataPart("phone", phoneNumber!!)
            multipartBody.addFormDataPart("password", password!!)
            multipartBody.addFormDataPart("address", address!!)
            multipartBody.addFormDataPart("device_token", deviceToken!!)
            latitude?.let {
                multipartBody.addFormDataPart("latitude", it)
            }
            longitude?.let {
                multipartBody.addFormDataPart("longitude", it)
            }
            multipartBody.addFormDataPart("deviceType", "android")
            multipartBody.addFormDataPart("usertype", "agent")
            for ((index, serviceId) in mSelectedServicesId.withIndex()) {
                multipartBody.addFormDataPart("services[$index]", serviceId.toString())
            }
            location?.let {
                multipartBody.addFormDataPart("location", it)
            }
            cityStateID?.let {
                multipartBody.addFormDataPart("city_state_id", it)
            }
            file?.let {
                multipartBody.addFormDataPart(
                    "image", it.name,
                    it.asRequestBody("image/*".toMediaTypeOrNull())
                )
            }

            certificateFile?.let {
                multipartBody.addFormDataPart(
                    "certificate", it.name,
                    it.asRequestBody("image/*".toMediaTypeOrNull())
                )
            }

            if (!socialId.isNullOrBlank()) {
                multipartBody.addFormDataPart("socialID", socialId!!)
            }

            if (!socialType.isNullOrBlank()) {
                multipartBody.addFormDataPart("provider", socialType!!)
            }

            mSignUpRepo.signUp(
                multipartBody.build(),
                isInternetConnected,
                baseView,
                disposable,
                mLDSignUpRequest
            )
        }
    }

    fun getCategoryList(
        isInternetConnected: Boolean,
        baseView: BaseView,
        disposable: CompositeDisposable
    ) {
        val body = JsonObject()
        mSignUpRequestModel?.apply {
            body.addProperty("latitude", latitude)
            body.addProperty("longitude", longitude)
        }
        mSignUpRepo.getCategoryList(
            body,
            isInternetConnected,
            baseView,
            disposable,
            mLDCategoryRequest
        )
    }
}