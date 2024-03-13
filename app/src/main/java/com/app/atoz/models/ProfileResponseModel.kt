package com.app.atoz.models

import com.app.atoz.models.address.AddressListItem
import com.google.gson.annotations.SerializedName

data class ProfileResponseModel(
    @SerializedName("id")
    val userId: String,
    @SerializedName("firstname")
    val firstName: String,
    @SerializedName("lastname")
    val lastName: String,
    @SerializedName("email")
    val email: String,
    @SerializedName("phone")
    val phoneNumber: String,
    @SerializedName("address")
    val address: String?,
    @SerializedName("image")
    val image: String?,
    @SerializedName("certificate")
    val certificate: String?,
    @SerializedName("latitude")
    val latitude: String?,
    @SerializedName("longitude")
    val longitude: String?,
    @SerializedName("usertype")
    val userType: String,
    @SerializedName("is_verified")
    val isVerified: Boolean,
    @SerializedName("location_id")
    val locationId: String?,
    @SerializedName("status")
    val providerStatus: String?,
    @SerializedName("facebook_id")
    val facebookId: String?,
    @SerializedName("google_id")
    val googleId: String?,
    @SerializedName("city_name")
    val providerCityName: String?,
    @SerializedName("rating")
    val providerRating: String?,
    @SerializedName("user_addresses")
    val userAddress: AddressListItem?,
    @SerializedName("user_services")
    val providerServiceList: ArrayList<ProviderExistServiceResponse>?,
    var providerCategoryList: ArrayList<CategoryModel>? = null
)