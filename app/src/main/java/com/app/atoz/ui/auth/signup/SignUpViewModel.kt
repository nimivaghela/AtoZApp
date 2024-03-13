package com.app.atoz.ui.auth.signup

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.app.atoz.models.*
import com.app.atoz.shareddata.base.BaseView
import com.app.atoz.shareddata.repo.SignUpRepo
import com.google.android.gms.maps.model.LatLng
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import io.reactivex.disposables.CompositeDisposable
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import javax.inject.Inject

class SignUpViewModel @Inject constructor(private val mSignUpRepo: SignUpRepo) : ViewModel() {

    private val mLDSignUpRequest = MutableLiveData<RequestState<SignUpSignInResponse>>()
    private val mLDCityRequest = MutableLiveData<RequestState<CityModel>>()
    private val mLDPhoneEmailExistRequest =
        MutableLiveData<RequestState<PhoneEmailExistResponseModel>>()
    private val _mLDProfileRequest = MutableLiveData<RequestState<ProfileResponseModel>>()
    private val _mLDTimerRequest = MutableLiveData<RequestState<JsonElement>>()


    var mProfilePicFile: File? = null

    var mProfilePicFileUri: Uri? = null
    var mCertificateFile: File? = null
    var mDeviceToken: String? = null
    var mCertificateFileUri: Uri? = null
    var latLng: LatLng? = null

    var mSignUpRequestModel: SignUpRequestModel? = null
    val mProfileRequestModel: LiveData<RequestState<ProfileResponseModel>>
        get() = _mLDProfileRequest

    val mTimerRequestModel: LiveData<RequestState<JsonElement>>
        get() = _mLDTimerRequest

    fun getSignUpRequest(): LiveData<RequestState<SignUpSignInResponse>> = mLDSignUpRequest
    fun getCitiesLiveData(): LiveData<RequestState<CityModel>> = mLDCityRequest

    fun getPhoneEmailExistRequest(): LiveData<RequestState<PhoneEmailExistResponseModel>> =
        mLDPhoneEmailExistRequest

    fun doSignUpForUser(
        isInternetConnected: Boolean,
        baseView: BaseView,
        disposable: CompositeDisposable,
        location: String,
        cityStateID: String,
        socialId: String,
        socialType: String
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
            multipartBody.addFormDataPart("location", location)
            multipartBody.addFormDataPart("latitude", latLng?.latitude.toString())
            multipartBody.addFormDataPart("longitude", latLng?.longitude.toString())
            multipartBody.addFormDataPart("city_state_id", cityStateID)
            multipartBody.addFormDataPart("deviceType", "android")
            multipartBody.addFormDataPart("usertype", "user")
            multipartBody.addFormDataPart("device_token", deviceToken!!)

            if (!socialId.isBlank()) {
                multipartBody.addFormDataPart("socialID", socialId)
            }

            if (!socialType.isBlank()) {
                multipartBody.addFormDataPart("provider", socialType)
            }

            file?.let {
                multipartBody.addFormDataPart(
                    "image", it.name,
                    it.asRequestBody("image/*".toMediaTypeOrNull())
                )
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

    fun doesPhoneEmailExist(
        isInternetConnected: Boolean,
        baseView: BaseView,
        disposable: CompositeDisposable,
        email: String,
        phone: String
    ) {
        val body = JsonObject()
        body.addProperty("email", email)
        body.addProperty("phone", phone)

        mSignUpRepo.phoneEmailExist(
            body,
            isInternetConnected,
            baseView,
            disposable,
            mLDPhoneEmailExistRequest
        )
    }

    fun generateRequestModel(
        firstName: String,
        lastName: String,
        email: String,
        phoneNumber: String,
        password: String,
        address: String,
        profileFile: File?,
        latitude: String?,
        longitude: String?,
        certificate: File?,
        socialId: String,
        socialType: String,
        location: String,
        cityStateID: String
    ) {
        mSignUpRequestModel = mDeviceToken?.let {
            SignUpRequestModel(
                firstName,
                lastName,
                email,
                phoneNumber,
                password,
                address,
                it,
                profileFile,
                latitude,
                longitude,
                certificate,
                socialId,
                socialType,
                location,
                cityStateID
            )
        }
    }

    fun provideCities(
        isInternetConnected: Boolean,
        baseView: BaseView?,
        disposable: CompositeDisposable
    ) {
        mSignUpRepo.provideCities(isInternetConnected, baseView, disposable, mLDCityRequest)
    }

    fun updateFCMToken(
        isInternetConnected: Boolean,
        disposable: CompositeDisposable,
        deviceToken: String
    ) {
        val multipartBody = MultipartBody.Builder()
        multipartBody.setType(MultipartBody.FORM)
        multipartBody.addFormDataPart("device_token", deviceToken)

        mSignUpRepo.updateFCMToken(
            multipartBody.build(),
            isInternetConnected,
            disposable,
            _mLDProfileRequest
        )
    }

    fun fetchTimerValue(
        isInternetConnected: Boolean,
        disposable: CompositeDisposable
    ) {

        mSignUpRepo.fetchTimerValue(
            isInternetConnected,
            disposable,
            _mLDTimerRequest
        )
    }
}
