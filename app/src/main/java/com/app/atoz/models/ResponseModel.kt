package com.app.atoz.models

import com.app.atoz.AppApplication
import com.app.atoz.R
import com.app.atoz.common.extentions.stringToDate
import com.google.gson.annotations.SerializedName


data class UserServiceStatusList(
    @SerializedName("service_request")
    val serviceRequest: ArrayList<ServiceResponse?>?
) {
    data class ServiceResponse(
        @SerializedName("address")
        val address: String?, // pakvan
        @SerializedName("category_id")
        val categoryId: Any?, // null
        @SerializedName("child_category")
        val childCategory: String?,
        @SerializedName("child_category_image")
        val childCategoryImage: String?, // http://192.168.2.90:3000/categories/default.png
        @SerializedName("created_at")
        val createdAt: String?, // 2019-04-03T08:11:42.000Z
        @SerializedName("date_time")
        val dateTime: String?, // 2019-04-01T16:52:00.000Z
        @SerializedName("id")
        val id: Int?, // 4
        @SerializedName("is_active")
        val isActive: Boolean?, // true
        @SerializedName("latitude")
        val latitude: String?, // 5.66600000
        @SerializedName("longitude")
        val longitude: String?, // 7.88800000
        @SerializedName("name")
        val name: String?, // devid
        @SerializedName("note")
        val note: String?, // testing
        @SerializedName("order_id")
        val orderId: String?, // null
        @SerializedName("parent_category")
        val parentCategory: String?,
        @SerializedName("parent_category_image")
        val parentCategoryImage: String?, // http://192.168.2.90:3000/categories/default.png
        @SerializedName("status")
        val status: Int?, // 5
        @SerializedName("sub_category_id")
        val subCategoryId: Any?, // null
        @SerializedName("updated_at")
        val updatedAt: String?, // 2019-04-03T08:11:42.000Z
        @SerializedName("user")
        val user: User?,
        @SerializedName("requests")
        val providerStatusList: ArrayList<RequestStatus>?,
        @SerializedName("user_id")
        val userId: Int? // 1
    ) {

        fun getOrderType(): String? {
            val statusString: String

            when (status) {

                1 -> {
                    statusString = AppApplication.getAppContext().getString(R.string.text_pending)
                }
                2 -> {
                    statusString = AppApplication.getAppContext().getString(R.string.text_confirmed)
                }
                3 -> {
                    statusString = AppApplication.getAppContext().getString(R.string.text_inprogress)
                }
                4 -> {
                    statusString = AppApplication.getAppContext().getString(R.string.text_completed)
                }
                5 -> {
                    statusString = AppApplication.getAppContext().getString(R.string.text_cancelled)
                }
                6 -> {
                    statusString = AppApplication.getAppContext().getString(R.string.text_cancelled)
                }
                7 -> {
                    statusString = AppApplication.getAppContext().getString(R.string.text_bill_uploaded)
                }
                8 -> {
                    statusString= AppApplication.getAppContext().getString(R.string.text_payment_done)
                }
                else -> {
                    statusString = ""
                }
            }
            return statusString
        }

        fun getFormatedDate(): String? {

            return dateTime?.stringToDate("MMM dd , yyyy")
        }

        data class User(
            @SerializedName("firstname")
            val firstname: String?, // Parul
            @SerializedName("id")
            val id: Int?, // 1
            @SerializedName("lastname")
            val lastname: String? // Patel
        )
    }
}

data class RequestStatus(
    @SerializedName("status")
    var providerStatus: Int?
) {
    fun getOrderType(): String? {
        var statusString: String? = ""

        when (providerStatus) {

            1 -> {
                statusString = AppApplication.getAppContext().getString(R.string.text_pending)
            }
            2 -> {
                statusString = AppApplication.getAppContext().getString(R.string.text_confirmed)
            }
            3 -> {
                statusString = AppApplication.getAppContext().getString(R.string.text_inprogress)
            }
            4 -> {
                statusString = AppApplication.getAppContext().getString(R.string.text_completed)
            }
            5 -> {
                statusString = AppApplication.getAppContext().getString(R.string.text_cancelled)
            }
            6 -> {
                statusString = AppApplication.getAppContext().getString(R.string.text_cancelled)
            }
            7 -> {
                statusString = AppApplication.getAppContext().getString(R.string.text_bill_uploaded)
            }
            8 -> {
                statusString= AppApplication.getAppContext().getString(R.string.text_payment_done)
            }
            else -> {
                statusString = ""
            }
        }
        return statusString
    }
}