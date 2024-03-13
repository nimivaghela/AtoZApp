package com.app.atoz.models

import com.google.gson.annotations.SerializedName

data class VerificationResponse(
    @SerializedName("is_verified") val isVerified: Boolean)