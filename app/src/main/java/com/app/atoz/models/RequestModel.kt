package com.app.atoz.models

import com.google.gson.annotations.SerializedName

data class SocialLogin(
    val email: String,
    @SerializedName("firstname") val firstName: String,
    @SerializedName("lastname") val lastName: String,
    val deviceType: String = "android",
    var deviceToken: String = "",
    @SerializedName("provider") val socialType: String,
    @SerializedName("socialID") val socialId: String,
    val url: String
)

data class IsSocialMediaExistResponse(
    @SerializedName("is_exist") val isExist: Boolean
)

data class RequestChangeInput(
    @SerializedName("status") val serviceStatus: String,
    @SerializedName("id") val serviceId: String
)