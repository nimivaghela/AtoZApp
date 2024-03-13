package com.app.atoz.ui.user.home

import androidx.lifecycle.MutableLiveData
import com.app.atoz.common.helper.CallbackWrapper
import com.app.atoz.models.ApiError
import com.app.atoz.models.AtoZResponseModel
import com.app.atoz.models.RequestState
import com.app.atoz.models.UserHolder
import com.app.atoz.models.homescreen.HomeModel
import com.app.atoz.models.homescreen.HomeModel.Companion.TYPE_ADVERTISEMENT
import com.app.atoz.models.homescreen.HomeModel.Companion.TYPE_PROMO_CODE
import com.app.atoz.models.homescreen.HomeModel.Companion.TYPE_SERVICE
import com.app.atoz.shareddata.base.BaseView
import com.app.atoz.shareddata.endpoint.ApiEndPoint
import com.app.atoz.utils.Config
import com.google.gson.JsonObject
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

class HomeRepo @Inject constructor(private val mAppEndPoint: ApiEndPoint, private val mUserHolder: UserHolder) {

    fun provideServiceData(
        isInternetConnected: Boolean,
        baseView: BaseView,
        disposable: CompositeDisposable,
        callback: MutableLiveData<RequestState<ArrayList<HomeModel>>>,
        searchParam: JsonObject
    ) {
        if (!isInternetConnected) {
            callback.value = RequestState(error = ApiError(Config.NETWORK_ERROR, null, true))
        } else {
            mAppEndPoint.provideHomeService(mUserHolder.authToken, searchParam)
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe { callback.postValue(RequestState(progress = true)) }
                .subscribeOn(Schedulers.computation())
                .map { response ->
                    val homeModelList = ArrayList<HomeModel>()
                    response.data?.apply {
                        services?.let {
                            for (serviceItem in it) {
                                if (serviceItem.children != null && serviceItem.children.size > 0) {
                                    val homeModel = HomeModel(TYPE_SERVICE, serviceItem, null, null)
                                    homeModelList.add(homeModel)
                                }
                            }
                        }

                        advertisement?.let {
                            val homeModel = HomeModel(TYPE_ADVERTISEMENT, null, it, null)
                            if (homeModelList.size > 1) {
                                homeModelList.add(1, homeModel)
                            } else {
                                homeModelList.add(homeModel)
                            }
                        }

                        couponList?.let {
                            val homeModel = HomeModel(TYPE_PROMO_CODE, null, null, it)
                            homeModelList.add(homeModel)
                        }
                    }

                    val newResponse =
                        AtoZResponseModel(response.status, response.isSuccess, response.message, homeModelList)
                    newResponse
                }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(object : CallbackWrapper<AtoZResponseModel<ArrayList<HomeModel>>>(baseView) {
                    override fun onApiSuccess(response: AtoZResponseModel<ArrayList<HomeModel>>) {
                        callback.value = RequestState(progress = false, data = response)
                    }

                    override fun onApiError(e: Throwable?) {
                        callback.value = RequestState(progress = false)
                    }
                })
                .addTo(disposable)
        }
    }
}