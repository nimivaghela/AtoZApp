package com.app.atoz.models.homescreen

import com.app.atoz.common.extentions.convertIntoAnother
import com.google.gson.annotations.SerializedName

data class CouponItem(
    @SerializedName("id")
    val id: String,
    @SerializedName("name")
    val couponName: String,
    @SerializedName("code")
    val couponCode: String,
    @SerializedName("description")
    val description: String,
    @SerializedName("discount")
    val discount: Int,
    @SerializedName("image")
    val couponImage: String ,
    @SerializedName("start_date")
    val couponStartDate: String?,
    @SerializedName("end_date")
    val couponEndDate: String?
) {
    fun getCouponTillEndDate() = couponEndDate?.convertIntoAnother("dd MMM yyyy") ?: "-"
}