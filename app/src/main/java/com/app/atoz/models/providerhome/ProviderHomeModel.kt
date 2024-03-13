package com.app.atoz.models.providerhome

import com.google.gson.annotations.SerializedName

data class ProviderHomeModel(

    @field:SerializedName("service_request")
    val serviceRequest: List<ServiceRequestItem>? = null
)