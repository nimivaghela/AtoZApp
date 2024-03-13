package com.app.atoz.models

import com.app.atoz.common.extentions.convertIntoAnother
import com.google.gson.annotations.SerializedName

data class SubscriptionResponse(
    @SerializedName("subscriptions")
    val subscriptionList: ArrayList<SubscriptionModel>?,
    @SerializedName("current_plan")
    val currentPlanModel: CurrentPlanModel?
)

data class SubscriptionModel(
    @SerializedName("id")
    val subscriptionId: String,
    @SerializedName("name")
    val subscriptionName: String,
    @SerializedName("description")
    val description: String,
    @SerializedName("discount")
    val discount: Int,
    @SerializedName("validity")
    val validity: Int,
    @SerializedName("price")
    val subscriptionPrice: Float,
    @SerializedName("image")
    val subscriptionImage: String?,
    @SerializedName("is_active")
    val isActive: Boolean,
    @SerializedName("subscription_services")
    val subscriptionServiceList: ArrayList<SubscriptionServiceItem>?
)

data class SubscriptionServiceItem(
    @SerializedName("service_id")
    val serviceId: String,
    @SerializedName("name")
    val serviceName: String
)

data class CurrentPlanModel(
    @SerializedName("subscription_id")
    val subscriptionId: String,
    @SerializedName("plan")
    val planName: String,
    @SerializedName("discount")
    val discount: Int,
    @SerializedName("price")
    val price: Float,
    @SerializedName("expiry_date")
    val expiryDate: String,
    @SerializedName("is_expired")
    val isExpired: Boolean,
    @SerializedName("purchase_date")
    val purchaseDate: String,
    @SerializedName("subscription_services")
    val subscriptionServiceList: ArrayList<SubscriptionServiceItem>?
){
    fun getExpirationDate()= expiryDate.convertIntoAnother("dd MMM yyyy") ?: "-"
}