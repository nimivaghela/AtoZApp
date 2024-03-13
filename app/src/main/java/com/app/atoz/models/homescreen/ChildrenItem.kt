package com.app.atoz.models.homescreen

import com.google.gson.annotations.SerializedName

data class ChildrenItem(

    @SerializedName("image")
    val image: String? = null,

    @SerializedName("original_photo")
    val originalPhoto: String? = null,

    @SerializedName("level")
    val level: Int? = null,

    @SerializedName("parent_id")
    val parentId: Int? = null,

    @SerializedName("name")
    val name: String? = null,

    @SerializedName("id")
    val id: Int? = null
)