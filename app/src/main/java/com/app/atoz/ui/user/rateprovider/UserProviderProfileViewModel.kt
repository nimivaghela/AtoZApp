package com.app.atoz.ui.user.rateprovider

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.app.atoz.models.ProviderUserRatingResponse
import com.app.atoz.models.RequestState
import com.app.atoz.models.UserHolder
import com.app.atoz.shareddata.base.BaseView
import com.app.atoz.shareddata.repo.RatingRepo
import com.google.gson.JsonObject
import io.reactivex.disposables.CompositeDisposable
import javax.inject.Inject

class UserProviderProfileViewModel @Inject constructor(
    private val mRatingRepo: RatingRepo,
    private val mUserHolder: UserHolder
) : ViewModel() {
    private var mProviderId: String? = null
    private var mOrderRequestId: String? = null

    private val mLDUserProviderRatingsComment = MutableLiveData<RequestState<ProviderUserRatingResponse>>()
    private val mLDSendRating = MutableLiveData<RequestState<Any>>()

    fun setProviderId(providerId: String) {
        mProviderId = providerId
    }

    fun setOrderRequestId(orderRequestId: String) {
        mOrderRequestId = orderRequestId
    }

    fun getUserProviderRatingsCommentObserver():
            LiveData<RequestState<ProviderUserRatingResponse>> = mLDUserProviderRatingsComment

    fun getSendRatingObserver(): LiveData<RequestState<Any>> = mLDSendRating

    fun getUserProviderRatingsComment(
        isInternetConnected: Boolean,
        baseView: BaseView,
        disposable: CompositeDisposable
    ) {
        mProviderId?.let {
            mRatingRepo.getUserProviderRatingsComment(
                it, isInternetConnected, baseView, disposable, mLDUserProviderRatingsComment
            )
        }
    }

    fun sendUserToProviderRatings(
        rating: Float,
        reviewComment: String,
        isInternetConnected: Boolean,
        baseView: BaseView,
        disposable: CompositeDisposable
    ) {
        if (mOrderRequestId != null && mProviderId != null) {
            val bodyData = JsonObject()
            bodyData.addProperty("request_id", mOrderRequestId)
            bodyData.addProperty("sender_id", mUserHolder.userId)
            bodyData.addProperty("receiver_id", mProviderId)
            bodyData.addProperty("rating", rating)
            bodyData.addProperty("review", reviewComment)

            mRatingRepo.sendRating(bodyData, isInternetConnected, baseView, disposable, mLDSendRating)
        }

    }
}