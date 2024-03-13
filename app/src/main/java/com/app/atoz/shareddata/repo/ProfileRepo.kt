package com.app.atoz.shareddata.repo

import androidx.lifecycle.MutableLiveData
import com.app.atoz.AppApplication
import com.app.atoz.R
import com.app.atoz.common.helper.CallbackWrapper
import com.app.atoz.models.*
import com.app.atoz.shareddata.base.BaseView
import com.app.atoz.shareddata.endpoint.ApiEndPoint
import com.app.atoz.utils.Config
import com.google.gson.Gson
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import okhttp3.MultipartBody
import timber.log.Timber
import javax.inject.Inject

class ProfileRepo @Inject constructor(private val mApiEndPoint: ApiEndPoint, private val mUserHolder: UserHolder) {

    fun getProfile(
        isInternetConnected: Boolean, baseView: BaseView, disposable: CompositeDisposable,
        callback: MutableLiveData<RequestState<ProfileResponseModel>>
    ) {
        if (!isInternetConnected) {
            callback.value = RequestState(error = ApiError(Config.NETWORK_ERROR, null, true))
        } else {
            mApiEndPoint.getProfile(mUserHolder.authToken, mUserHolder.userId)
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe { callback.value = RequestState(progress = true) }
                .subscribeWith(object :
                    CallbackWrapper<AtoZResponseModel<ProfileResponseModel>>(baseView) {
                    override fun onApiSuccess(response: AtoZResponseModel<ProfileResponseModel>) {
                        response.data?.let {
                            try {
                                if (it.userType == "agent") {
                                    val newCategoryList: ArrayList<CategoryModel> = ArrayList()
                                    it.providerServiceList?.let { serviceData ->
                                        for (service in serviceData) {
                                            var categoryIndex = -1
                                            category@ for ((index, value) in newCategoryList.withIndex()) {
                                                if (service.categoryModel.categoryId == value.categoryId) {
                                                    categoryIndex = index
                                                    break@category
                                                }
                                            }
                                            if (categoryIndex != -1) {
                                                service.apply {
                                                    val newServiceModel: ServiceModel =
                                                        ServiceModel(serviceId, serviceName, serviceImage)
                                                    newCategoryList[categoryIndex].serviceList?.add(newServiceModel)
                                                }
                                            } else {
                                                val categoryModel = service.categoryModel
                                                categoryModel.serviceList = ArrayList()
                                                service.apply {
                                                    categoryModel.serviceList?.add(
                                                        ServiceModel(
                                                            serviceId,
                                                            serviceName,
                                                            serviceImage
                                                        )
                                                    )
                                                    newCategoryList.add(categoryModel)
                                                }
                                            }
                                            categoryIndex = -1
                                        }
                                    }
                                    Timber.d("New service list : ${Gson().toJson(newCategoryList)}")
                                    it.providerCategoryList = newCategoryList
                                }
                                /**
                                 * store user data in shared preference
                                 */
                                mUserHolder.storeDate(
                                    mUserHolder.authToken,
                                    it.userType != "user",
                                    it.userId,
                                    it.email,
                                    it.firstName,
                                    it.lastName,
                                    it.phoneNumber,
                                    it.image,
                                    mUserHolder.imageThumbUrl,
                                    it.address ?: "",
                                    it.latitude ?: "",
                                    it.longitude ?: "",
                                    it.certificate,
                                    it.isVerified,
                                    it.facebookId,
                                    it.googleId,
                                    it.providerStatus
                                )
                                mUserHolder.saveUserAddressFromProfile(it.userAddress)
                            } catch (e: Exception) {
                                e.printStackTrace()
                                callback.value = RequestState(
                                    error = ApiError(
                                        Config.CUSTOM_ERROR,
                                        AppApplication.instance.getString(R.string.text_error_internal_server),
                                        true
                                    )
                                )
                            }
                        }
                        Timber.d("New response  : ${Gson().toJson(response)}")
                        callback.value = RequestState(progress = false, data = response)
                    }

                    override fun onApiError(e: Throwable?) {
                        callback.value = RequestState(progress = false)
                    }
                }).addTo(disposable)
        }
    }

    fun editProfile(
        multipartBody: MultipartBody,
        isInternetConnected: Boolean, baseView: BaseView, disposable: CompositeDisposable,
        callback: MutableLiveData<RequestState<ProfileResponseModel>>
    ) {
        if (!isInternetConnected) {
            callback.value = RequestState(error = ApiError(Config.NETWORK_ERROR, null, true))
        } else {
            mApiEndPoint.editProfile(mUserHolder.authToken, multipartBody)
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe { callback.value = RequestState(progress = true) }
                .subscribeWith(object :
                    CallbackWrapper<AtoZResponseModel<ProfileResponseModel>>(baseView) {
                    override fun onApiSuccess(response: AtoZResponseModel<ProfileResponseModel>) {
                        /**
                         * on success of edit profile call get profile to get latest data
                         */
                        getProfile(isInternetConnected, baseView, disposable, callback)
                    }

                    override fun onApiError(e: Throwable?) {
                        callback.value = RequestState(progress = false)
                    }
                }).addTo(disposable)
        }
    }

    fun provideCities(
        isInternetConnected: Boolean,
        baseView: BaseView,
        disposable: CompositeDisposable,
        callback: MutableLiveData<RequestState<CityModel>>
    ) {
        if (!isInternetConnected) {
            callback.value = RequestState(error = ApiError(Config.NETWORK_ERROR, null, true))
        } else {

            mApiEndPoint.getCities()
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe { callback.value = RequestState(progress = true) }
                .subscribeWith(object : CallbackWrapper<AtoZResponseModel<CityModel>>(baseView) {
                    override fun onApiError(e: Throwable?) {
                        callback.value = RequestState(progress = false)
                    }

                    override fun onApiSuccess(response: AtoZResponseModel<CityModel>) {
                        callback.value = RequestState(progress = false, data = response)
                    }
                }).addTo(disposable)
        }
    }
}