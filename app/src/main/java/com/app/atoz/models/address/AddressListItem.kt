package com.app.atoz.models.address

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
data class AddressListItem(


    @SerializedName("id")
    val id: Int?,
    @SerializedName("location")
    val location: String?,
    @SerializedName("address")
    val address: String?,
    @SerializedName("city_id")
    val cityId: Int?,

    @SerializedName("state_id")
    val stateId: Int?,
    @SerializedName("address_type")
    val addressType: String,

    /**
     * below field are used in view profile API & summary api
     */
    @SerializedName("latitude")
    val latitude: String?,
    @SerializedName("longitude")
    val longitude: String?
) : Parcelable