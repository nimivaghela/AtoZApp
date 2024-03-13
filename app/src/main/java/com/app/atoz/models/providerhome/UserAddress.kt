package com.app.atoz.models.providerhome

import com.google.gson.annotations.SerializedName

data class UserAddress(

    @field:SerializedName("address")
    val address: String? = null,

    @field:SerializedName("city")
    val city: City? = null,

    @field:SerializedName("location")
    val location: String? = null,

    @field:SerializedName("city_id")
    val cityId: Int? = null
)