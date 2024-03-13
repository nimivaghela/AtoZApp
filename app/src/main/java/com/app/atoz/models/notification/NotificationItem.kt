package com.app.atoz.models.notification


import com.google.gson.annotations.SerializedName

data class NotificationItem(

        @field:SerializedName("is_read")
        val isRead: String? = null,

        @field:SerializedName("updated_at")
        val updatedAt: String? = null,

        @field:SerializedName("payload")
        val payload: String? = null,

        @field:SerializedName("receiver_id")
        val receiverId: Int? = null,

        @field:SerializedName("created_at")
        val createdAt: String? = null,

        @field:SerializedName("id")
        val id: Int? = null
)