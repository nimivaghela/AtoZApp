package com.app.atoz.models.providerhome

import com.google.gson.annotations.SerializedName

data class RequestsItem(

    @field:SerializedName("updated_at")
    val updatedAt: String? = null,

    @field:SerializedName("service_request_id")
    val serviceRequestId: Int? = null,

    @field:SerializedName("provider_id")
    val providerId: Int? = null,

    @field:SerializedName("created_at")
    val createdAt: String? = null,

    @field:SerializedName("id")
    val id: Int? = null,

    @field:SerializedName("status")
    val status: Int? = null
)