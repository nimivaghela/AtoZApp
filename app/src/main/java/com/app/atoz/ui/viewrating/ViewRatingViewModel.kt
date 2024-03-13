package com.app.atoz.ui.viewrating

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.app.atoz.models.RequestState
import com.app.atoz.models.UserRatingModel
import com.app.atoz.shareddata.base.BaseView
import com.app.atoz.shareddata.repo.RatingRepo
import io.reactivex.disposables.CompositeDisposable
import javax.inject.Inject

class ViewRatingViewModel @Inject constructor(private val mRatingRepo: RatingRepo) : ViewModel() {
    private val mLdPersonalRating = MutableLiveData<RequestState<ArrayList<UserRatingModel>>>()

    fun getPersonalRatingObserver(): LiveData<RequestState<ArrayList<UserRatingModel>>> = mLdPersonalRating

    fun getUserProviderPersonalRatingsComments(
        userId: String?, isInternetConnected: Boolean, baseView: BaseView, disposable: CompositeDisposable
    ) {
        mRatingRepo.getUserProviderPersonalRatingComments(
            userId!!,
            isInternetConnected,
            baseView,
            disposable,
            mLdPersonalRating
        )
    }
}