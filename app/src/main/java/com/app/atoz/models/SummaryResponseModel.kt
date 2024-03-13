package com.app.atoz.models

import com.app.atoz.models.address.AddressListItem
import com.google.gson.annotations.SerializedName

data class SummaryResponseModel(
    @SerializedName("service_request")
    val serviceRequest: ServiceRequest
)

data class ServiceRequest(
    @SerializedName("id")
    val requestId: String,
    @SerializedName("order_id")
    val orderId: String,
    @SerializedName("address_id")
    val addressId: String,
    @SerializedName("name")
    val name: String,
    @SerializedName("date_time")
    val dateTime: String,
    @SerializedName("note")
    val note: String?,
    @SerializedName("category_id")
    val categoryId: String,
    @SerializedName("sub_category_id")
    val subCategoryId: String,
    @SerializedName("offer_discount")
    val offerDiscount: Int,
    @SerializedName("service_charge")
    val serviceCharge: Int,
    @SerializedName("user_address")
    val addressData: AddressListItem,
    @SerializedName("service_request_categories")
    val requestItemList: ArrayList<RequestItemList>
)

data class RequestItemList(
    @SerializedName("id")
    val itemId: String,
    @SerializedName("category_id")
    val childSubCategoryId: String,
    @SerializedName("service_request_id")
    val serviceRequestId: String,
    @SerializedName("service_name")
    val serviceName: String,
    @SerializedName("service_price")
    val servicePrice: Float,
    @SerializedName("service_image")
    val serviceImage: String?
)