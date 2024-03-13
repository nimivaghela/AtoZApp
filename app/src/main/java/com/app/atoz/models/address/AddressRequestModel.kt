package com.app.atoz.models.address

import com.google.gson.annotations.SerializedName

class AddressRequestModel {
    @SerializedName("user_id")
    var userID: String ? = null

    @SerializedName("location")
    var location: String ? = null

    @SerializedName("address")
    var address: String ? = null

    @SerializedName("latitude")
    var latitude: String ? = null
    @SerializedName("longitude")
    var longitude: String ? = null

    @SerializedName("city_state_id")
    var cityStateID: String? = null

    @SerializedName("address_type")
    var addressType: String? = null
}