package com.app.atoz.models.homescreen

import com.app.atoz.models.AdvertisementModel

data class HomeModel(
    var viewType: Int,
    val serviceItem: ServicesItem? = null,
    val advertisement: AdvertisementModel? = null,
    val couponList: ArrayList<CouponItem>? = null
) {
    companion object {
        const val TYPE_SERVICE = 1
        const val TYPE_ADVERTISEMENT = 2
        const val TYPE_PROMO_CODE = 3
    }
}