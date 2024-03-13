package com.app.atoz.models

import com.google.gson.annotations.SerializedName

data class AtoZResponseModel<T>(
    @SerializedName("status")
    var status: Int,
    @SerializedName("success")
    var isSuccess: Boolean? = null,
    @SerializedName("message")
    var message: String? = null,
    @SerializedName("data")
    var data: T? = null
)

data class RequestState<T>(
    val error: ApiError? = null,
    val progress: Boolean = false,
    val data: AtoZResponseModel<T>? = null
)

/**
 * @errorState error state defined in the Config.kt class
 * you can set CUSTOM_ERROR, NETWORK_ERROR
 * In case of CUSTOM_ERROR, you have to set customMessage also
 */
data class ApiError(val errorState: String, val customMessage: String?, val show: Boolean)
