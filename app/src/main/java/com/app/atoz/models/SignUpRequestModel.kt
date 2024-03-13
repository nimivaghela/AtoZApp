package com.app.atoz.models

import android.os.Parcel
import android.os.Parcelable
import java.io.File

data class SignUpRequestModel(
    var firstName: String?,
    var lastName: String?,
    var email: String?,
    var phoneNumber: String?,
    var password: String?,
    var address: String?,
    var deviceToken: String?,
    var file: File?,
    var latitude: String?,
    var longitude: String?,
    var certificateFile: File?,
    var socialId: String?,
    var socialType: String?,
    var location: String?,
    var cityStateID: String?
) : Parcelable {
    constructor(source: Parcel) : this(
        source.readString(),
        source.readString(),
        source.readString(),
        source.readString(),
        source.readString(),
        source.readString(),
        source.readString(),
        source.readSerializable() as File?,
        source.readString(),
        source.readString(),
        source.readSerializable() as File?,
        source.readString(),
        source.readString(),
        source.readString(),
        source.readString()
    )

    override fun describeContents() = 0

    override fun writeToParcel(dest: Parcel, flags: Int) = with(dest) {
        writeString(firstName)
        writeString(lastName)
        writeString(email)
        writeString(phoneNumber)
        writeString(password)
        writeString(address)
        writeString(deviceToken)
        writeSerializable(file)
        writeString(latitude)
        writeString(longitude)
        writeSerializable(certificateFile)
        writeString(socialId)
        writeString(socialType)
        writeString(location)
        writeString(cityStateID)
    }

    companion object {
        @JvmField
        val CREATOR: Parcelable.Creator<SignUpRequestModel> = object : Parcelable.Creator<SignUpRequestModel> {
            override fun createFromParcel(source: Parcel): SignUpRequestModel = SignUpRequestModel(source)
            override fun newArray(size: Int): Array<SignUpRequestModel?> = arrayOfNulls(size)
        }
    }
}