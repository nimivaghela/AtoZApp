package com.app.atoz.models

import com.app.atoz.common.extentions.getRelativeTimeDisplay
import com.google.gson.annotations.SerializedName

data class ProviderUserRatingResponse(
    @SerializedName("user_detail")
    val userDetails: RateUserProfile,
    @SerializedName("ratings")
    val ratingList: ArrayList<UserRatingModel>
)

data class UserRatingModel(
    @SerializedName("id")
    val ratingId: String,
    @SerializedName("rating")
    val rating: Float,
    @SerializedName("review")
    val review: String?,
    @SerializedName("updated_at")
    val timeOfRating: String?,//2019-04-24T12:19:11.000Z
    @SerializedName("sender")
    val sender: RateUserProfile?
) {
    fun getTimeOfRatings() = timeOfRating?.getRelativeTimeDisplay() ?: ""
}

data class RateUserProfile(
    @SerializedName("firstname")
    val firstName: String?,
    @SerializedName("lastname")
    val lastName: String?,
    @SerializedName("image")
    val profileImage: String?,
    @SerializedName("city_name")
    val cityName: String?,

    /**
     * below fields are used in user details
     */
    @SerializedName("email")
    val emailId: String?,
    @SerializedName("rating")
    val rating: String?
) {
    fun getName() = "$firstName $lastName"
}