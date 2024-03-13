package com.app.atoz.ui.user.address

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.app.atoz.models.RequestState
import com.app.atoz.models.address.AddressListItem
import com.app.atoz.models.address.AddressModel
import com.app.atoz.models.address.AddressRequestModel
import com.app.atoz.shareddata.base.BaseView
import io.reactivex.disposables.CompositeDisposable
import javax.inject.Inject

class AddressViewModel @Inject constructor(private val addressRepo: AddressRepo) : ViewModel() {

    private val addressMutableLiveData: MutableLiveData<RequestState<AddressModel>>by lazy {
        MutableLiveData<RequestState<AddressModel>>()
    }

    private val addAddressMutableLiveData: MutableLiveData<RequestState<AddressListItem>>by lazy {
        MutableLiveData<RequestState<AddressListItem>>()
    }

    private val deleteAddressMutableLiveData: MutableLiveData<RequestState<AddressListItem>>by lazy {
        MutableLiveData<RequestState<AddressListItem>>()
    }


    private val addressRequestModel = AddressRequestModel()

    fun getAddressLiveData(): LiveData<RequestState<AddressModel>> {
        return addressMutableLiveData
    }

    fun getAddAddressLiveData(): LiveData<RequestState<AddressListItem>> {
        return addAddressMutableLiveData
    }

    fun getDeleteAddressLiveData(): MutableLiveData<RequestState<AddressListItem>> {
        return deleteAddressMutableLiveData
    }


    fun getAddressRequestModel(): AddressRequestModel {
        return addressRequestModel
    }

    fun provideAddresses(isInternetConnected: Boolean, baseView: BaseView, disposable: CompositeDisposable) {
        addressRepo.provideAddress(isInternetConnected, baseView, disposable, addressMutableLiveData)
    }

    fun addAddress(isInternetConnected: Boolean, baseView: BaseView, disposable: CompositeDisposable) {
        addressRepo.addAddress(
            isInternetConnected,
            baseView,
            disposable,
            addAddressMutableLiveData,
            addressRequestModel
        )
    }

    fun deleteAddress(
        isInternetConnected: Boolean,
        baseView: BaseView,
        disposable: CompositeDisposable,
        addressItem: AddressListItem
    ) {
        addressRepo.deleteAddress(
            isInternetConnected,
            baseView,
            disposable,
            deleteAddressMutableLiveData, addressItem
        )
    }
}