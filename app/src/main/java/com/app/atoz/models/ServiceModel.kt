package com.app.atoz.models

import android.os.Parcel
import android.os.Parcelable
import com.google.gson.annotations.SerializedName

data class CategorySelectionResponseModel(
    @SerializedName("services") val service: ArrayList<CategoryModel>,
    @SerializedName("user_services") val providerExistService: ArrayList<CategoryModel>?
)

data class CategoryModel(
    @SerializedName("id")
    var categoryId: Int,
    @SerializedName("name")
    var categoryName: String?,
    @SerializedName("parent_id")
    var parentCategoryId: String?,
    @SerializedName("description")
    var categoryDescription: String?,
    @SerializedName("image")
    var categoryImage: String?,
    @SerializedName("original_photo")
    var categoryOriginalPhoto: String?,
    @SerializedName(value = "children", alternate = ["new_children"])
    var serviceList: ArrayList<ServiceModel>?,
    var isExpanded: Boolean = false,
    /**
     * below fields used for subcategory list
     */
    @SerializedName("price")
    var price: String?,
    var isChecked: Boolean = false,

    /**
     * below fields are used in 3 level listing api (provider profile)
     */
    @SerializedName("requested_price")
    var requestedPrice: String? = null,
    @SerializedName("location_id")
    var locationId: String? = null

) : Parcelable {
    constructor(source: Parcel) : this(
        source.readInt(),
        source.readString(),
        source.readString(),
        source.readString(),
        source.readString(),
        source.readString(),
        source.createTypedArrayList(ServiceModel.CREATOR),
        1 == source.readInt(),
        source.readString(),
        1 == source.readInt(),
        source.readString(),
        source.readString()
    )

    override fun describeContents() = 0

    override fun writeToParcel(dest: Parcel, flags: Int) = with(dest) {
        writeInt(categoryId)
        writeString(categoryName)
        writeString(parentCategoryId)
        writeString(categoryDescription)
        writeString(categoryImage)
        writeString(categoryOriginalPhoto)
        writeTypedList(serviceList)
        writeInt((if (isExpanded) 1 else 0))
        writeString(price)
        writeInt((if (isChecked) 1 else 0))
        writeString(requestedPrice)
        writeString(locationId)
    }

    companion object {
        @JvmField
        val CREATOR: Parcelable.Creator<CategoryModel> = object : Parcelable.Creator<CategoryModel> {
            override fun createFromParcel(source: Parcel): CategoryModel = CategoryModel(source)
            override fun newArray(size: Int): Array<CategoryModel?> = arrayOfNulls(size)
        }
    }
}

data class ServiceModel(
    @SerializedName("id")
    var serviceId: Int,
    @SerializedName("name")
    var serviceName: String?,
    @SerializedName("image")
    var serviceImageUrl: String?,
    var selected: Boolean = false
) : Parcelable {
    constructor(source: Parcel) : this(
        source.readInt(),
        source.readString(),
        source.readString(),
        1 == source.readInt()
    )

    override fun describeContents() = 0

    override fun writeToParcel(dest: Parcel, flags: Int) = with(dest) {
        writeInt(serviceId)
        writeString(serviceName)
        writeString(serviceImageUrl)
        writeInt((if (selected) 1 else 0))
    }

    companion object {
        @JvmField
        val CREATOR: Parcelable.Creator<ServiceModel> = object : Parcelable.Creator<ServiceModel> {
            override fun createFromParcel(source: Parcel): ServiceModel = ServiceModel(source)
            override fun newArray(size: Int): Array<ServiceModel?> = arrayOfNulls(size)
        }
    }
}