package com.app.atoz.ui.settings

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.app.atoz.models.RequestState
import com.app.atoz.models.UserHolder
import com.app.atoz.shareddata.base.BaseView
import com.app.atoz.shareddata.repo.SettingsRepo
import io.reactivex.disposables.CompositeDisposable
import javax.inject.Inject

class SettingsViewModel @Inject constructor(
    private val mSettingsRepo: SettingsRepo,
    val mUserHolder: UserHolder
) : ViewModel() {

    private val mLDLogout = MutableLiveData<RequestState<Any>>()

    fun getLogoutObserver(): LiveData<RequestState<Any>> = mLDLogout

    fun logout(
        isInternetConnected: Boolean,
        baseView: BaseView,
        disposable: CompositeDisposable
    ) {
        mSettingsRepo.logout(isInternetConnected, baseView, disposable, mLDLogout)
    }
}

