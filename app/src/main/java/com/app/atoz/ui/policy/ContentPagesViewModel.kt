package com.app.atoz.ui.policy

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.app.atoz.models.RequestState
import com.app.atoz.shareddata.base.BaseView
import com.app.atoz.shareddata.repo.ContentPageRepo
import io.reactivex.disposables.CompositeDisposable
import javax.inject.Inject

class ContentPagesViewModel @Inject constructor(private val mContentRepo: ContentPageRepo) : ViewModel() {
    private val mLDContentPageResponse = MutableLiveData<RequestState<String>>()

    fun getContentPageObserver() = mLDContentPageResponse

    fun getContentPage(
        pageName: String,
        isInternetConnected: Boolean,
        baseView: BaseView,
        disposable: CompositeDisposable
    ) {
        mContentRepo.getContentPage(pageName, isInternetConnected, baseView, disposable, mLDContentPageResponse)
    }
}