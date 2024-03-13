package com.app.atoz.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class PaymentDue(
    @PrimaryKey
    @ColumnInfo(name = "request_id")
    val requestId: String,
    @ColumnInfo(name = "user_id")
    val userId: String,
    @ColumnInfo(name = "amount")
    val amount: String,
    @ColumnInfo(name = "transaction_id")
    val transactionId: String,
    @ColumnInfo(name = "description")
    val description: String,
    @ColumnInfo(name = "status")
    val paymentStatus: Int,
    @ColumnInfo(name = "transaction_type")
    val transactionType: Int,
    @ColumnInfo(name = "provider_entered_amount")
    val providerEnteredAmount: String?,
    @ColumnInfo(name = "agent_service_charge")
    val agentServiceCharge: String?,
    @ColumnInfo(name = "discount_amount")
    val discountAmount: String?,
    @ColumnInfo(name = "discount_percentage")
    val discountPercentage: String?,
    @ColumnInfo(name = "discount_coupon_id")
    val discountCouponId: String?,
    @ColumnInfo(name = "subscription_user_id")
    val subscriptionUserId: Int?,
    @ColumnInfo(name = "subscription_discount_amount")
    val subscriptionDiscountAmount: Float?
)