package com.app.atoz.ui.provider.ratecustomer

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

class ProviderOtherUserProfileViewModel @Inject constructor(
    private val mRatingRepo: RatingRepo,
    private val mUserHolder: UserHolder
) : ViewModel() {
    private var mUserId: String? = null
    private var mOrderRequestId: String? = null

    private val mLDProviderUserRatingsComment = MutableLiveData<RequestState<ProviderUserRatingResponse>>()
    private val mLDSendRating = MutableLiveData<RequestState<Any>>()

    fun setUserId(userId: String) {
        mUserId = userId
    }

    fun setOrderRequestId(orderRequestId: String) {
        mOrderRequestId = orderRequestId
    }

    fun getProviderToUserRatingsCommentObserver():
            LiveData<RequestState<ProviderUserRatingResponse>> = mLDProviderUserRatingsComment

    fun getSendRatingObserver(): LiveData<RequestState<Any>> = mLDSendRating

    fun getProviderToUserRatingsComment(
        isInternetConnected: Boolean,
        baseView: BaseView,
        disposable: CompositeDisposable
    ) {
        mUserId?.let {
            mRatingRepo.getUserProviderRatingsComment(
                it, isInternetConnected, baseView, disposable, mLDProviderUserRatingsComment
            )
        }
    }

    fun sendProviderToUserRatings(
        rating: Float,
        reviewComment: String,
        isInternetConnected: Boolean,
        baseView: BaseView,
        disposable: CompositeDisposable
    ) {
        if (mOrderRequestId != null && mUserId != null) {
            val bodyData = JsonObject()
            bodyData.addProperty("request_id", mOrderRequestId)
            bodyData.addProperty("sender_id", mUserHolder.userId)
            bodyData.addProperty("receiver_id", mUserId)
            bodyData.addProperty("rating", rating)
            bodyData.addProperty("review", reviewComment)

            mRatingRepo.sendRating(bodyData, isInternetConnected, baseView, disposable, mLDSendRating)
        }

    }
}