package com.app.atoz.models.providerhome

import com.google.gson.annotations.SerializedName

data class User(

    @field:SerializedName("image")
    val image: String? = null,

    @field:SerializedName("firstname")
    val firstname: String? = null,

    @field:SerializedName("id")
    val id: Int? = null,

    @field:SerializedName("lastname")
    val lastname: String? = null
)