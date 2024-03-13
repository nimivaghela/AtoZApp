package com.app.atoz.ui.user.profile

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.app.atoz.models.ProfileResponseModel
import com.app.atoz.models.RequestState
import com.app.atoz.shareddata.base.BaseView
import com.app.atoz.shareddata.repo.ProfileRepo
import io.reactivex.disposables.CompositeDisposable
import javax.inject.Inject

class UserMyProfileViewModel @Inject constructor(private val mProfileRepo: ProfileRepo) : ViewModel() {

    private val mLDProfileRequest = MutableLiveData<RequestState<ProfileResponseModel>>()

    fun viewProfileRequest(): LiveData<RequestState<ProfileResponseModel>> = mLDProfileRequest

    fun getUserProfile(
        isInternetConnected: Boolean,
        baseView: BaseView,
        disposable: CompositeDisposable
    ) {
        mProfileRepo.getProfile(isInternetConnected, baseView, disposable, mLDProfileRequest)
    }
}