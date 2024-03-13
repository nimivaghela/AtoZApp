package com.app.atoz.utils

import android.app.Activity
import android.content.Context
import android.content.IntentSender
import android.location.LocationManager
import android.widget.Toast
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.LocationSettingsStatusCodes


private fun createLocationRequest(): LocationRequest {
    val locationRequest = LocationRequest.create()

    locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
    return locationRequest
}

fun showLocationSettingsAlert(context: Context, locationRequestResult: LocationRequestResult?) {
    val builder = LocationSettingsRequest.Builder()
        .addLocationRequest(createLocationRequest())
    val client = LocationServices.getSettingsClient(context)

    //Check if location settings are as per requirement or not.
    client.checkLocationSettings(builder.build())
        .addOnCompleteListener { task ->
            if (task.isSuccessful) {

                locationRequestResult?.onComplete(true)
            } else {
                when ((task.exception as ApiException).statusCode) {
                    LocationSettingsStatusCodes.RESOLUTION_REQUIRED -> {
                        try {
                            // Show the dialog by calling startResolutionForResult(), and check the
                            // result in onActivityResult().
                            val rae = task.exception as ResolvableApiException?
                            rae!!.startResolutionForResult(context as Activity, 101)
                        } catch (sie: IntentSender.SendIntentException) {
                            locationRequestResult?.onComplete(false)
                        }

                    }
                    LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE -> {
                        val errorMessage =
                            "Location settings are inadequate, and cannot be " + "fixed here. Fix in Settings."
                        Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()
                    }
                }

            }
        }
}


fun isLocationServiceEnabled(mContext: Context): Boolean {
    val lm = mContext.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    return lm.isProviderEnabled(LocationManager.GPS_PROVIDER) || lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
}

interface LocationRequestResult {
    fun onComplete(result: Boolean)
}