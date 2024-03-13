package com.app.atoz.models.homescreen

import com.app.atoz.models.AdvertisementModel
import com.google.gson.annotations.SerializedName

data class HomeResponse(

    @field:SerializedName("services")
    val services: List<ServicesItem>? = null,

    @SerializedName("coupons")
    val couponList: ArrayList<CouponItem>? = null,

    @SerializedName("advertisedata")
    val advertisement: AdvertisementModel? = null

)