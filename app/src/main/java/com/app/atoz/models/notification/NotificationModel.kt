package com.app.atoz.models.notification

import com.google.gson.annotations.SerializedName

data class NotificationModel(

        @field:SerializedName("data")
        val data: List<NotificationItem?>? = null
)