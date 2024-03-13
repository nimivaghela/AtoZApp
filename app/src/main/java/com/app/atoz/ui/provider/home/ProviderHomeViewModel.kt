package com.app.atoz.ui.provider.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.app.atoz.models.RequestState
import com.app.atoz.models.providerhome.ProviderHomeModel
import com.app.atoz.shareddata.base.BaseView
import io.reactivex.disposables.CompositeDisposable
import javax.inject.Inject

class ProviderHomeViewModel @Inject constructor(private val providerRequesrRepo: ProviderRequestRepo) : ViewModel() {

    private val mProviderRequestObserver = MutableLiveData<RequestState<ProviderHomeModel>>()
    fun getProviderRequestObserver(): LiveData<RequestState<ProviderHomeModel>> {
        return mProviderRequestObserver
    }

    fun populateProviderRequest(
        isInternetConnected: Boolean,
        baseView: BaseView,
        disposable: CompositeDisposable
    ) {
        providerRequesrRepo.provideRequestData(isInternetConnected, baseView, disposable, mProviderRequestObserver)

    }
}
