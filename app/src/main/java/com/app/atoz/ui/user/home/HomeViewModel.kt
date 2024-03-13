package com.app.atoz.ui.user.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.app.atoz.models.RequestState
import com.app.atoz.models.homescreen.HomeModel
import com.app.atoz.shareddata.base.BaseView
import com.google.gson.JsonObject
import io.reactivex.disposables.CompositeDisposable
import javax.inject.Inject

class HomeViewModel @Inject constructor(private val homeRepo: HomeRepo) : ViewModel() {
    private val mHomeDataRequest = MutableLiveData<RequestState<ArrayList<HomeModel>>>()

    fun getHomeDataObserver(): LiveData<RequestState<ArrayList<HomeModel>>> {
        return mHomeDataRequest
    }

    fun populateHomeData(
        isInternetConnected: Boolean,
        baseView: BaseView,
        disposable: CompositeDisposable,
        lat: String,
        long: String
    ) {

        val body = JsonObject()
        body.addProperty("latitude", lat)
        body.addProperty("longitude", long)

        homeRepo.provideServiceData(isInternetConnected, baseView, disposable, mHomeDataRequest, body)
    }
}
