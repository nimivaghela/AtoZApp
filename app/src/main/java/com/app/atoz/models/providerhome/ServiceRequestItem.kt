package com.app.atoz.models.providerhome

import com.app.atoz.common.extentions.formatTime12hr
import com.app.atoz.common.extentions.plusTime
import com.app.atoz.common.extentions.stringToDate
import com.google.gson.annotations.SerializedName
import java.util.*

data class ServiceRequestItem(

    @field:SerializedName("note")
    val note: String? = null,

    @field:SerializedName("is_active")
    val isActive: Boolean? = null,

    @field:SerializedName("latitude")
    val latitude: Any? = null,

    @field:SerializedName("user_address")
    val userAddress: UserAddress? = null,

    @field:SerializedName("address_id")
    val addressId: Any? = null,

    @field:SerializedName("created_at")
    val createdAt: String? = null,

    @field:SerializedName("requests")
    val requests: List<RequestsItem?>? = null,

    @field:SerializedName("child_category")
    val childCategory: String? = null,

    @field:SerializedName("date_time")
    val dateTime: String? = null,

    @field:SerializedName("category_id")
    val categoryId: Int? = null,

    @field:SerializedName("updated_at")
    val updatedAt: String? = null,

    @field:SerializedName("user_id")
    val userId: Int? = null,

    @field:SerializedName("sub_category_id")
    val subCategoryId: Any? = null,

    @field:SerializedName("name")
    val name: String? = null,

    @field:SerializedName("parent_category")
    val parentCategory: String? = null,

    @field:SerializedName("id")
    val id: Int? = null,

    @field:SerializedName("order_id")
    val orderId: Any? = null,

    @field:SerializedName("user")
    val user: User? = null,

    @field:SerializedName("status")
    val status: Int? = null,

    @field:SerializedName("longitude")
    val longitude: Any? = null
) {

    fun getFormattedDate(): String? {
        return dateTime?.stringToDate("MMM dd , yyyy")
    }

    fun getFormattedTime(): String? {
        return dateTime?.stringToDate("hh:mm a")?.toUpperCase(Locale.ROOT)
    }

    fun getRequestEndTime(plusTime: Int): String? {

        return createdAt?.plusTime(plusTime)?.formatTime12hr()?.toUpperCase(Locale.ROOT)
    }
}