package com.app.atoz.models

import android.content.SharedPreferences
import com.app.atoz.common.extentions.prefBoolean
import com.app.atoz.common.extentions.prefInt
import com.app.atoz.common.extentions.prefString
import com.app.atoz.models.address.AddressListItem
import com.google.gson.Gson

class UserHolder(preference: SharedPreferences) {
    var isUserAsProvider by preference.prefBoolean()
        private set

    var authToken by preference.prefString("")
        private set

    var userId by preference.prefString("")

    var email by preference.prefString("")

    var firstName by preference.prefString("")

    var lastName by preference.prefString("")

    var phone by preference.prefString("")

    var imageUrl by preference.prefString("")

    var imageThumbUrl by preference.prefString("")

    var address by preference.prefString("")

    var latitude by preference.prefString("")

    var longitude by preference.prefString("")

    var certificateUrl by preference.prefString("")

    var isVerified by preference.prefBoolean(false)

    var facebookId by preference.prefString("")

    var googleId by preference.prefString("")


    var providerStatus by preference.prefString("")
    var currentLatitude by preference.prefString("")
    var currentLongitude by preference.prefString("")
    var deviceToken by preference.prefString("")

    private var userAddressFromProfile by preference.prefString("")

    var timer: Int by preference.prefInt(0)

    fun saveUserAddressFromProfile(userAddress: AddressListItem?) {
        userAddress?.let {
            userAddressFromProfile = Gson().toJson(it)
        }
    }

    fun getUserAddressFromProfile(): AddressListItem? {
        try {
            return Gson().fromJson(userAddressFromProfile, AddressListItem::class.java)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    fun storeDate(
        token: String?,
        isProvider: Boolean,
        userId: String,
        email: String,
        firstName: String,
        lastName: String,
        phone: String,
        imageUrl: String?,
        imageThumbUrl: String?,
        address: String,
        latitude: String,
        longitude: String,
        certificateUrl: String?,
        isVerified: Boolean,
        facebookId: String?,
        googleId: String?,
        providerStatus: String?

    ) {
        authToken = token
        isUserAsProvider = isProvider
        this.userId = userId
        this.email = email
        this.firstName = firstName
        this.lastName = lastName
        this.phone = phone
        this.imageUrl = imageUrl
        this.imageThumbUrl = imageThumbUrl
        this.address = address
        this.latitude = latitude
        this.longitude = longitude
        this.certificateUrl = certificateUrl
        this.isVerified = isVerified
        this.facebookId = facebookId
        this.googleId = googleId
        this.providerStatus = providerStatus
    }

    fun clearData() {
        authToken = ""
        isUserAsProvider = false
        this.userId = ""
        this.email = ""
        this.firstName = ""
        this.lastName = ""
        this.phone = ""
        this.imageUrl = ""
        this.imageThumbUrl = ""
        this.address = ""
        this.latitude = ""
        this.longitude = ""
        this.certificateUrl = ""
        this.isVerified = false
        this.facebookId = ""
        this.googleId = ""
        this.providerStatus = ""
        this.currentLongitude = ""
        this.currentLatitude = ""
        this.deviceToken = ""
    }
}