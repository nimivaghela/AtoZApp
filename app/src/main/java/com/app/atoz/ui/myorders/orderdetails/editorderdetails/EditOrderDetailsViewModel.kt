package com.app.atoz.ui.myorders.orderdetails.editorderdetails

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.app.atoz.models.RequestState
import com.app.atoz.shareddata.base.BaseView
import com.app.atoz.shareddata.repo.MyOrderRepo
import com.google.gson.JsonObject
import io.reactivex.disposables.CompositeDisposable
import timber.log.Timber
import javax.inject.Inject

class EditOrderDetailsViewModel @Inject constructor(private val mOrderRepo: MyOrderRepo) : ViewModel() {

    private val mLDEditOrderNote = MutableLiveData<RequestState<Any>>()
    var mOrderId: String? = null

    fun getEditOrderNote() = mLDEditOrderNote

    fun callEditOrderNote(
        editedNotes: String,
        isInternetConnected: Boolean,
        baseView: BaseView,
        disposable: CompositeDisposable
    ) {
        mOrderId?.let {
            val body = JsonObject()
            body.addProperty("id", mOrderId)
            body.addProperty("note", editedNotes)
            mOrderRepo.editOrderDetails(body, isInternetConnected, baseView, disposable, mLDEditOrderNote)
        } ?: let {
            Timber.d("orderId must required")
        }
    }
}