package com.app.atoz.models

import com.google.gson.annotations.SerializedName

data class CallModel(
    @SerializedName("otp")
    val otp: String?,
    @SerializedName("phone")
    val phone: String?
)