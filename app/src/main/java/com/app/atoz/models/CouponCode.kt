package com.app.atoz.models

import com.google.gson.annotations.SerializedName

data class CouponCode(
    @SerializedName("coupon_id") val couponCodeId: String?,
    @SerializedName("discount") val discountPercentage: Int?
)