package com.app.atoz.models

import com.google.gson.annotations.SerializedName

data class AdvertisementModel(
    @SerializedName("isGoogleEnable")
    val isGoogleEnable: Boolean,
    @SerializedName("advertise")
    val advertisementList: ArrayList<AdvertisementItem>?
)

data class AdvertisementItem(
    @SerializedName("id")
    val advertisementId: String,
    @SerializedName("media")
    val mediaUrl: String?,
    @SerializedName("url")
    val redirectUrl: String?,
    @SerializedName("description")
    val description: String?,
    @SerializedName("mimetype")
    val mimeType: Int
)