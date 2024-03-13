package com.app.atoz.ui.editprofile

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.app.atoz.models.*
import com.app.atoz.shareddata.base.BaseView
import com.app.atoz.shareddata.repo.ProfileRepo
import com.google.android.gms.maps.model.LatLng
import io.reactivex.disposables.CompositeDisposable
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import javax.inject.Inject

class UserEditProfileViewModel @Inject constructor(
    private val mProfileRepo: ProfileRepo,
    private val mUserHolder: UserHolder
) : ViewModel() {

    private val mLDProfileRequest = MutableLiveData<RequestState<ProfileResponseModel>>()
    private val mLDEditProfileRequest = MutableLiveData<RequestState<ProfileResponseModel>>()
    private val mLDCityRequest = MutableLiveData<RequestState<CityModel>>()
    var mSignUpRequestModel: SignUpRequestModel? = null

    var latLng: LatLng? = null
    var mProfilePicFile: File? = null

    var mProfilePicFileUri: Uri? = null


    fun viewEditProfileRequest(): LiveData<RequestState<ProfileResponseModel>> =
        mLDEditProfileRequest

    fun getCitiesLiveData(): LiveData<RequestState<CityModel>> = mLDCityRequest

    fun viewProfileRequest(): LiveData<RequestState<ProfileResponseModel>> = mLDProfileRequest

    fun getUserProfile(
        isInternetConnected: Boolean,
        baseView: BaseView,
        disposable: CompositeDisposable
    ) {
        mProfileRepo.getProfile(isInternetConnected, baseView, disposable, mLDProfileRequest)
    }

    fun editProfile(
        isInternetConnected: Boolean,
        baseView: BaseView,
        disposable: CompositeDisposable
    ) {
        mSignUpRequestModel?.apply {

            val multipartBody = MultipartBody.Builder()
            multipartBody.setType(MultipartBody.FORM)
            multipartBody.addFormDataPart("firstname", firstName!!)
            multipartBody.addFormDataPart("lastname", lastName!!)
            multipartBody.addFormDataPart("address", address!!)
            multipartBody.addFormDataPart("id", mUserHolder.userId!!)

            location?.let {
                multipartBody.addFormDataPart("location", it)
            }
            latitude?.let {
                multipartBody.addFormDataPart("latitude", it)
            }
            longitude?.let {
                multipartBody.addFormDataPart("longitude", it)
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
            mProfileRepo.editProfile(
                multipartBody.build(),
                isInternetConnected,
                baseView,
                disposable,
                mLDEditProfileRequest
            )
        }
    }

    fun provideCities(
        isInternetConnected: Boolean,
        baseView: BaseView,
        disposable: CompositeDisposable
    ) {
        mProfileRepo.provideCities(isInternetConnected, baseView, disposable, mLDCityRequest)
    }

    fun generateRequestModel(
        firstName: String,
        lastName: String,
        address: String,
        profileFile: File?,
        latitude: String?,
        longitude: String?,
        location: String?,
        cityStateID: String
    ) {
        mSignUpRequestModel = SignUpRequestModel(
            firstName,
            lastName,
            mUserHolder.email,
            mUserHolder.phone,
            "",
            address,
            "",
            profileFile,
            latitude,
            longitude,
            null,
            null,
            null,
            location,
            cityStateID
        )
    }


}