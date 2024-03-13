package com.app.atoz.models

import com.google.gson.annotations.SerializedName

class ProviderExistServiceResponse(
    @SerializedName("service_id")
    val serviceId: Int,
    @SerializedName("service_name")
    val serviceName: String,
    @SerializedName("image")
    val serviceImage: String,
    @SerializedName("original_photo")
    val serviceOriginalImage: String,
    @SerializedName("parent")
    val categoryModel: CategoryModel
)
