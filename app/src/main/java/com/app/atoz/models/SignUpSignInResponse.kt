package com.app.atoz.models

import com.google.gson.annotations.SerializedName

data class SignUpSignInResponse(
    @SerializedName("is_verified") val isVerified: Boolean,
    @SerializedName("id") val userId: String,
    @SerializedName("firstname") val firstName: String,
    @SerializedName("lastname") val lastName: String,
    @SerializedName("email") val email: String,
    @SerializedName("phone") val phone: String,
    @SerializedName("address") val address: String,
    @SerializedName("latitude") val latitude: String,
    @SerializedName("longitude") val longitude: String,
    @SerializedName("image") val image: String?,
    @SerializedName("imagethumb") val imageThumb: String?,
    @SerializedName("usertype") val userType: String,
    @SerializedName("authorizationkey") val token: String,
    /**
     * for signIn extra fields
     */
    @SerializedName("user_addresses")
    val userAddresses: ArrayList<UserAddress>,
    @SerializedName("certificate")
    val certificateUrl: String?,
    @SerializedName("certificatethumb")
    val certificateThumb: String?,
    @SerializedName("location_id")
    val locationId: String?,
    @SerializedName("status")
    val status: String?,
    @SerializedName("facebook_id")
    val facebookId: String?,
    @SerializedName("google_id")
    val googleId: String?
)


data class UserAddress(
    @SerializedName("id") val addressId: String,
    @SerializedName("address") val address: String?,
    @SerializedName("latitude") val latitude: String?,
    @SerializedName("longitude") val longitude: String?
)