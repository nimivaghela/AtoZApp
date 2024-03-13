package com.app.atoz.models.address

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
data class AddressModel(

    @SerializedName("list")
    val addressList: List<AddressListItem?>
) : Parcelable