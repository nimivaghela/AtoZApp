package com.app.atoz.models

import com.google.gson.annotations.SerializedName

data class PhoneEmailExistResponseModel(
    @SerializedName("is_email_exist")
    val doesEmailExist: Boolean,
    @SerializedName("is_phone_exist")
    val doesPhoneExist: Boolean
)