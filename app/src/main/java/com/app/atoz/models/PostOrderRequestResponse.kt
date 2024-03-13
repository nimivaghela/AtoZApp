package com.app.atoz.models

import com.google.gson.annotations.SerializedName

data class PostOrderRequestResponse(
    @SerializedName("name") val name: String
)