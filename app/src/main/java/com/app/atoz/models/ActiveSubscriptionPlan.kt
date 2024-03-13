package com.app.atoz.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

@Entity
data class ActiveSubscriptionPlan(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: Int,
    @ColumnInfo(name = "plan")
    @SerializedName("plan")
    val plan: String,
    @ColumnInfo(name = "discount")
    @SerializedName("discount")
    val discount: Int,
    @ColumnInfo(name = "service_ids")
    @SerializedName("service_ids")
    val serviceIds: ArrayList<String>,
    @ColumnInfo(name = "expiry_date")
    @SerializedName("expiry_date")
    val expiryDate: String,
    @ColumnInfo(name = "is_expired")
    @SerializedName("is_expired")
    val isExpired: Boolean
)