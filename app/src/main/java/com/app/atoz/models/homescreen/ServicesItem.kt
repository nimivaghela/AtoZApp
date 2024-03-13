package com.app.atoz.models.homescreen

import com.google.gson.annotations.SerializedName

data class ServicesItem(

    @field:SerializedName("image")
    val image: String? = null,

    @field:SerializedName("original_photo")
    val originalPhoto: String? = null,

    @field:SerializedName("payment_type")
    val paymentType: Int? = null,

    @field:SerializedName("level")
    val level: Int? = null,

    @field:SerializedName("new_children")
    val children: ArrayList<ChildrenItem>? = null,

    @field:SerializedName("parent_id")
    val parentId: Any? = null,

    @field:SerializedName("name")
    val name: String? = null,

    @field:SerializedName("id")
    val id: Int? = null
)