package com.app.atoz.models

import com.app.atoz.AppApplication
import com.app.atoz.R
import com.app.atoz.common.extentions.stringToDate
import com.google.gson.annotations.SerializedName

data class OrderDetailModel(
    @SerializedName("service_request")
    val serviceRequest: ServiceRequest?
) {
    data class ServiceRequest(
        @SerializedName("address_id")
        val addressId: Any?, // null
        @SerializedName("category_id")
        val categoryId: Int?, // 96
        @SerializedName("created_at")
        val createdAt: String?, // 2019-04-11T05:39:43.000Z
        @SerializedName("date_time")
        val dateTime: String?, // 2019-04-01T16:52:00.000Z
        @SerializedName("id")
        val id: Int?, // 11
        @SerializedName("is_active")
        val isActive: Boolean?, // true
        @SerializedName("latitude")
        val latitude: Any?, // null
        @SerializedName("longitude")
        val longitude: Any?, // null
        @SerializedName("name")
        val name: String?, // devid
        @SerializedName("note")
        var note: String?, // testing
        @SerializedName("order_id")
        val orderId: String?, // null
        @SerializedName("user")
        val user: User?,
        @SerializedName("total_bill_amount")
        var totalBillAmount: String?,
        @SerializedName("transactions")
        val transactionList: ArrayList<Transaction>?,
        @SerializedName("service_request_images")
        val serviceRequestImages: List<ServiceRequestImage>,
        @SerializedName("status")
        val status: Int?, // 2
        @SerializedName("sub_category_id")
        val subCategoryId: Int?, // 99
        @SerializedName("updated_at")
        val updatedAt: String?, // 2019-04-11T05:39:43.000Z
        @SerializedName("user_id")
        val userId: Int?, // 1
        @SerializedName("otp")
        val otp: String? = "1234",
        @SerializedName("user_address")
        val userAddress: UserAddress?,
        @SerializedName("child_category")
        val childCategory: String?, // test34
        @SerializedName("requests")
        val providerStatusList: ArrayList<RequestStatus>?,
        @SerializedName("service_request_complete_images")
        var serviceRequestCompleteImages: List<ServiceRequestImage> = mutableListOf(),
        @SerializedName("service_request_categories")
        val requestedItemList: ArrayList<RequestItemList>?,
        @SerializedName("is_rating")
        val isRatingDone: Boolean
    ) {

        data class Transaction(
            @SerializedName("transaction_id")
            val transactionId: String?,
            @SerializedName("reference_no")
            val referenceNumber: String?,
            @SerializedName("amount")
            val billAmount: String?,
            @SerializedName("transaction_type")
            val transactionType: String?,
            @SerializedName("status")
            val transactionStatus: Int,
            @SerializedName("discount_amount")
            val discountAmount: Float,
            @SerializedName("discount_per")
            val discountPercentage: Int,
            @SerializedName("subscription_discount_amt")
            val subscriptionDisAmount: Float,
            @SerializedName("subscription_user_id")
            val subscriptionUserId: Int
        )

        data class ServiceRequestImage(
            @SerializedName("id")
            val id: Int?, // 6
            @SerializedName("image")
            val image: String?, // 1554961182914.jpg
            @SerializedName("original")
            val original: String?, // http://192.168.2.90:3000/requests/original/1554961182914.jpg
            @SerializedName("service_request_id")
            val serviceRequestId: Int?, // 11
            @SerializedName("thumb")
            val thumb: String? // http://192.168.2.90:3000/requests/thumb/1554961182914.jpg
        )

        data class UserAddress(
            @SerializedName("address")
            val address: String?, // 123
            @SerializedName("city")
            val city: City?,
            @SerializedName("city_id")
            val cityId: Int?, // 783
            @SerializedName("location")
            val location: String?, // abc
            @SerializedName("latitude")
            val latitude: String?,
            @SerializedName("longitude")
            val longitude: String?
        ) {
            data class City(
                @SerializedName("name")
                val name: String? // Ahmedabad
            )
        }

        data class User(
            @SerializedName("firstname")
            val firstname: String?, // sagar
            @SerializedName("id")
            val id: Int?, // 69
            @SerializedName("image")
            val image: String?, // http://192.168.0.145:3000/categories/default.png
            @SerializedName("lastname")
            val lastname: String?, // jain
            @SerializedName("phone")
            val phone: String?, // 12354546546
            @SerializedName("rating")
            val userRating: String?
        ) {
            fun getFullName() = "$firstname $lastname"
        }

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
                    statusString = AppApplication.getAppContext().getString(R.string.text_payment_done)
                }
                else -> {
                    statusString = ""
                }
            }
            return statusString
        }

        fun getFormattedDate(): String? {

            return dateTime?.stringToDate("MMM dd , yyyy")
        }

        fun getFormattedTime(): String? {
            return dateTime?.stringToDate("hh:mm a")?.toUpperCase()
        }
    }
}